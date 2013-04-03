/**
 * Copyright 2007 Kevin Twidle, Imperial College, London, England.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 *
 * Contact: Kevin Twidle <kpt@doc.ic.ac.uk>
 *
 * Created on Mar 11, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.apt;

import static com.sun.mirror.util.DeclarationVisitors.NO_OP;
import static com.sun.mirror.util.DeclarationVisitors.getDeclarationScanner;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import eu.novi.ponder2.apt.DocWriter;
import eu.novi.ponder2.apt.DocWriterHtml;
//import eu.novi.ponder2.apt.ManagedObjectVisitor;
import eu.novi.ponder2.apt.Ponder2notify;
import eu.novi.ponder2.apt.Ponder2op;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.util.SimpleDeclarationVisitor;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
@SuppressWarnings("restriction")
public class Ponder2Processor implements AnnotationProcessor {

	private static final String typeBigDecimal = BigDecimal.class
			.getCanonicalName();
	private static final String typeBlock = "eu.novi.ponder2.objects.P2Block";
	private static final String typeBoolean = "boolean";
	private static final String typeClass = Class.class.getCanonicalName();
	private static final String typeDouble = "double";
	private static final String typeFloat = "float";
	private static final String typeInt = "int";
	private static final String typeLong = "long";
	private static final String typeManagedObject = "eu.novi.ponder2.ManagedObject";
	private static final String typeP2Array = "eu.novi.ponder2.objects.P2Array";
	private static final String typeP2Hash = "eu.novi.ponder2.objects.P2Hash";
	private static final String typeP2ObjectAdaptor = "eu.novi.ponder2.P2ObjectAdaptor";
	private static final String typeP2Value = "eu.novi.ponder2.objects.P2Object";
	private static final String typePonder2Exception = "eu.novi.ponder2.exception.Ponder2Exception";
	private static final String typePonder2OperationException = "eu.novi.ponder2.exception.Ponder2OperationException";
	private static final String typeString = String.class.getCanonicalName();
	private static final String typeXML = "com.twicom.qdparser.TaggedElement";

	private final AnnotationProcessorEnvironment env;
	private final Filer filer;

	Ponder2Processor(AnnotationProcessorEnvironment env) {
		this.env = env;
		filer = env.getFiler();
	}

	public void process() {
		for (TypeDeclaration typeDecl : env.getSpecifiedTypeDeclarations()) {
			typeDecl.accept(getDeclarationScanner(new ManagedObjectVisitor(
					filer), NO_OP));
		}
	}

	private static class ManagedObjectVisitor extends SimpleDeclarationVisitor {

		Filer filer;

		public ManagedObjectVisitor(Filer filer) {
			this.filer = filer;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.sun.mirror.util.SimpleDeclarationVisitor#visitClassDeclaration
		 * (com.sun.mirror.declaration.ClassDeclaration)
		 */
		@Override
		public void visitClassDeclaration(ClassDeclaration classDecl) {
			for (InterfaceType iface : classDecl.getSuperinterfaces()) {
				if (!typeManagedObject.equals(iface.toString()))
					continue;
				// We have to create an adaptor for this Class
				// System.out.println("Class: " + classDecl + ". Iface: " +
				// iface);
				try {
					String packageName = classDecl.getPackage()
							.getQualifiedName();
					String className = classDecl.getSimpleName();
					String adaptorName = className + "P2Adaptor";
					String superClass = getSuperClass(classDecl);
					// System.out.println("Super class is " + superClass);
					PrintWriter source = filer.createSourceFile(packageName
							+ "." + adaptorName);
					writeHeader(source, packageName, adaptorName, superClass);
					DocWriter doc = new DocWriterHtml(filer, className);
					doc.writeClass(className, superClass,
							classDecl.getDocComment());
					doc.section(DocWriter.CONSTRUCTOR);
					// Deal with Ponder2 statics
					for (MethodDeclaration decl : classDecl.getMethods()) {
						Ponder2op p2op = decl.getAnnotation(Ponder2op.class);
						if (p2op != null && isStatic(decl)) {
							writeStatic(source, doc, className, p2op.value(),
									decl);
						}
					}
					// Deal with Ponder2 constructors
					for (ConstructorDeclaration decl : classDecl
							.getConstructors()) {
						Ponder2op p2op = decl.getAnnotation(Ponder2op.class);
						if (p2op != null)
							writeConstructor(source, doc, className,
									p2op.value(), decl);
					}
					doc.endSection(DocWriter.CONSTRUCTOR);
					// Deal with Ponder2 operations
					doc.section(DocWriter.OPERATION);
					boolean wildcard = false;
					for (MethodDeclaration decl : classDecl.getMethods()) {
						Ponder2op p2op = decl.getAnnotation(Ponder2op.class);
						if (p2op != null) {
							writeOperation(source, doc, className,
									p2op.value(), decl);
							if (p2op.value().equals(Ponder2op.WILDCARD))
								wildcard = true;
						}
					}
					doc.endSection(DocWriter.OPERATION);
					writeStaticEnd(source, adaptorName);
					// Deal with Ponder2 notifications
					for (MethodDeclaration decl : classDecl.getMethods()) {
						Ponder2notify notify = decl
								.getAnnotation(Ponder2notify.class);
						if (notify != null)
							writeNotification(source, notify.value(),
									className, decl);
					}
					writeFooter(source, wildcard);
					doc.close();
					source.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Nothing else to do for this class
				break;
			}
		}

		/**
		 * returns true if the method has a static flag
		 * 
		 * @param decl
		 * @return
		 */
		boolean isStatic(MethodDeclaration decl) {
			Collection<Modifier> modifiers = decl.getModifiers();
			return modifiers.contains(Modifier.STATIC);
		}

		/**
		 * @param classDecl
		 * @return
		 */
		private String getSuperClass(ClassDeclaration classDecl) {
			// System.out.println("Looking for super class for " + classDecl);
			ClassType superClass = classDecl.getSuperclass();
			if (superClass == null)
				return typeP2ObjectAdaptor;
			classDecl = superClass.getDeclaration();
			for (InterfaceType iface : classDecl.getSuperinterfaces()) {
				// System.out.println("Interface is " + iface);
				if (typeManagedObject.equals(iface.toString()))
					return classDecl + "P2Adaptor";
			}
			return getSuperClass(classDecl);
		}

		private void writeConstructor(PrintWriter source, DocWriter doc,
				String className, String operation, ConstructorDeclaration decl) {
			String params = processParameters(doc, operation,
					decl.getDocComment(), decl.getParameters(), null);
			source.println();
			source.println("    // Create operation '" + operation
					+ "' calls constructor for " + className);
			source.println("    create.put(\"" + operation
					+ "\", new CreateOperation() {");
			source.println("      @Override");
			source.println("      public "
					+ typeManagedObject
					+ " call(P2Object obj, P2Object source, String operation, P2Object... args)");
			source.println("          throws " + typePonder2Exception + " {");
			source.println("        return new " + className + "(" + params
					+ ");");
			source.println("      }");
			source.println("    });");
		}

		private void writeStatic(PrintWriter source, DocWriter doc,
				String className, String operation, MethodDeclaration decl) {
			String methodName = decl.getSimpleName();
			String returnType = decl.getReturnType().toString();
			String params = processParameters(doc, operation,
					decl.getDocComment(), decl.getParameters(), decl
							.getReturnType().toString());
			source.println();
			source.println("    // Static operation '" + operation
					+ "' calls static method " + methodName);
			source.println("    staticop.put(\"" + operation
					+ "\", new StaticOperation() {");
			source.println("      @Override");
			source.println("      public P2Object call(P2Object source, String operation, P2Object... args)");
			source.println("          throws " + typePonder2Exception + " {");
			boolean voidReturn = returnType.equals("void");
			if (!voidReturn)
				source.println("        " + returnType + " value = ");
			source.println("             " + className + "." + methodName + "("
					+ params + ");");
			if (voidReturn)
				source.println("        return P2Null.NULL;");
			else if (returnType.startsWith("eu.novi.ponder2.objects."))
				source.println("        return value;");
			else {
				// Do we need to convert the return value to a P2Object?
				String returnStatement = "        return P2Object.create(value);";
				try {
					if (instanceOf("class " + typeP2Value,
							Class.forName(returnType)))
						returnStatement = "        return value;";
				} catch (ClassNotFoundException e) {
				} catch (NoClassDefFoundError e) {
				}
				source.println(returnStatement);
			}

			source.println("      }");
			source.println("    });");
		}

		private void writeOperation(PrintWriter source, DocWriter doc,
				String className, String operation, MethodDeclaration decl) {
			String methodName = decl.getSimpleName();
			String returnType = decl.getReturnType().toString();
			boolean isStatic = isStatic(decl);
			String params = processParameters(doc, operation,
					decl.getDocComment(), decl.getParameters(), decl
							.getReturnType().toString());
			source.println();
			source.print("    // Operation '" + operation + "' calls ");
			if (isStatic)
				source.print("static method ");
			source.println(methodName);
			source.println("    operation.put(\"" + operation
					+ "\", new InstanceOperation() {");
			source.println("      @Override");
			source.println("      public P2Object call(P2Object thisObj, "
					+ typeManagedObject
					+ " obj, P2Object source, String operation, P2Object... args)");
			source.println("          throws " + typePonder2Exception + " {");
			if (operation.equals(Ponder2op.WILDCARD)) {
				source.print("         return ");
				// TODO The following line does not seem to detect static
				// methods
				if (isStatic(decl))
					source.print(className);
				else
					source.print("((" + className + ")obj)");
				source.println("." + methodName + "(source, operation, args);");
			} else {
				boolean voidReturn = returnType.equals("void");
				if (!voidReturn)
					source.println("        " + returnType + " value = ");
				source.print("             ");
				if (isStatic)
					source.print(className);
				else
					source.print("((" + className + ")obj)");
				source.println("." + methodName + "(" + params + ");");
				if (voidReturn)
					source.println("        return thisObj;");
				else if (returnType.startsWith("eu.novi.ponder2.objects."))
					source.println("        return value;");
				else {
					// Do we need to convert the return value to a P2Object?
					String returnStatement = "        return P2Object.create(value);";
					try {
						if (instanceOf("class " + typeP2Value,
								Class.forName(returnType)))
							returnStatement = "        return value;";
					} catch (ClassNotFoundException e) {
					} catch (NoClassDefFoundError e) {
					}
					source.println(returnStatement);
				}
			}
			source.println("      }");
			source.println("    });");
		}

		/**
		 * @param typeP2Value
		 * @param name
		 * @return
		 */
		private boolean instanceOf(String typeP2Value, Class<?> name) {
			if (name == null)
				return false;
			if (typeP2Value.equals(name.toString()))
				return true;
			return instanceOf(typeP2Value, name.getSuperclass());
		}

		private String processParameters(DocWriter doc, String operation,
				String comment, Collection<ParameterDeclaration> parameters,
				String returnType) {
			StringBuffer paramString = new StringBuffer();
			int argIndex = 0;
			List<String> parameterList = new Vector<String>();
			boolean first = true;
			for (ParameterDeclaration parameter : parameters) {
				String type = parameter.getType().toString();
				String name = parameter.getSimpleName();
				if (first) {
					first = false;
					if (type.equals(typeP2Value) && name.equals("source")) {
						paramString.append("source");
						continue;
					}
					if (type.equals(typeP2Value) && name.equals("myP2Object")) {
						paramString.append("obj");
						continue;
					}
				} else {
					paramString.append(", ");
				}
				// System.out.println("Parameter is " + type + " " + name);
				parameterList.add(name);
				if (type.equals(typeString)) {
					paramString.append("args[" + argIndex + "].asString()");
				} else if (type.equals(typeBigDecimal)) {
					paramString.append("args[" + argIndex + "].asNumber()");
				} else if (type.equals(typeInt)) {
					paramString.append("args[" + argIndex + "].asInteger()");
				} else if (type.equals(typeLong)) {
					paramString.append("args[" + argIndex + "].asLong()");
				} else if (type.equals(typeFloat)) {
					paramString.append("args[" + argIndex + "].asFloat()");
				} else if (type.equals(typeDouble)) {
					paramString.append("args[" + argIndex + "].asDouble()");
				} else if (type.equals(typeBoolean)) {
					paramString.append("args[" + argIndex + "].asBoolean()");
				} else if (type.equals(typeBlock)) {
					paramString.append("args[" + argIndex + "].asBlock()");
				} else if (type.equals(typeP2Array)) {
					paramString.append("args[" + argIndex + "].asP2Array()");
				} else if (type.startsWith(typeClass)) {
					paramString.append("args[" + argIndex + "].asClass()");
				} else if (type.equals(typeP2Hash)) {
					paramString.append("args[" + argIndex + "].asHash()");
				} else if (type.equals(typeXML)) {
					paramString.append("args[" + argIndex + "].asXML()");
				} else if (type.equals(typeP2Value)) {
					paramString.append("args[" + argIndex + "]");
				} else if (type.equals(typeP2Value + "[]")) {
					paramString.append("args");
				} else {
					System.err
							.println("Ponder2Processor: Unknown type " + type);
					paramString
							.append("Compilation Error: Cannot convert type "
									+ type);
				}
				argIndex++;
			}

			int expected = argsIn(operation);
			if (expected >= 0 && expected != argIndex) {
				System.err.println("Wrong number of args '" + operation
						+ "' expected " + expected + " but found " + argIndex);
				paramString
						.append(" --- Compilation Error: Argument mismatch: '"
								+ operation + "' expected " + expected
								+ " but found " + argIndex);
			}

			doc.writeOperation(operation, comment, parameterList, returnType);

			return paramString.toString();
		}

		private int argsIn(String operation) {
			if (!Character.isLetter(operation.charAt(0)))
				return 1;
			if (operation.equals(Ponder2op.WILDCARD))
				return -1;
			int count = 0;
			byte[] bytes = operation.getBytes();
			for (byte b : bytes) {
				if (b == ':')
					count++;
			}
			return count;
		}

		private void writeHeader(PrintWriter source, String packageName,
				String className, String superClassName) {
			// System.out.println("Class name is " + className);
			if (packageName != null && packageName.length() > 0)
				source.println("package " + packageName + ";");
			source.println();
			source.println("import java.util.HashMap;");
			source.println("import java.util.Map;");
			source.println();
			source.println("import " + typeP2Value + ";");
			source.println("import " + typePonder2Exception + ";");
			source.println();
			source.println("/**");
			source.println(" * Adaptor object for managed object");
			source.println(" * ");
			source.println(" * @author Auto generated by annotation processor tool");
			source.println(" */");
			source.println("public class " + className + " extends "
					+ superClassName + " {");
			source.println();
			source.println("  /**");
			source.println("   * The map of static operations");
			source.println("   */");
			source.println("  private final static Map<String, StaticOperation> staticop;");
			source.println("  /**");
			source.println("   * The map of create operations to constructors");
			source.println("   */");
			source.println("  private final static Map<String, CreateOperation> create;");
			source.println("  /**");
			source.println("   * The map of instance operations to methods");
			source.println("   */");
			source.println("  private final static Map<String, InstanceOperation> operation;");
			source.println();
			source.println("  // Create the call tables when the class is loaded");
			source.println("  static {");
			source.println("    staticop = new HashMap<String, StaticOperation>();");
			source.println("    create = new HashMap<String, CreateOperation>();");
			source.println("    operation = new HashMap<String, InstanceOperation>();");
		}

		private void writeStaticEnd(PrintWriter source, String adaptorName) {
			source.println("  }");
			source.println();
			source.println("  public " + adaptorName + "() {");
			source.println("  }");
			source.println();
			source.println("  public " + adaptorName
					+ "(P2Object source, String operation, P2Object... args)");
			source.println("      throws Ponder2Exception {");
			source.println("     super(source, operation, args);");
			source.println("  }");
			source.println();
		}

		private void writeNotification(PrintWriter source, String notify,
				String className, MethodDeclaration decl) {
			String methodName = decl.getSimpleName();
			// TypeMirror returnType = decl.getReturnType();
			// String params = getParameters(decl.getParameters());
			if (notify.equals("oid")) {
				source.println("  @Override");
				source.println("  protected void notifyOID(OID oid) {");
				source.println("    ((" + className + ")objImpl)." + methodName
						+ "(oid);");
				source.println("    super.notifyOID(oid);");
				source.println("  }");
			}
		}

		private void writeFooter(PrintWriter source, boolean wildcard) {
			source.println();
			source.println("  @Override");
			source.println("  public void getCreateOrStaticOperation(CreateOrStaticOperation opInfo) throws "
					+ typePonder2OperationException + " {");
			source.println("    boolean found = opInfo.findOp(create,staticop);");
			source.println("    if (!found) super.getCreateOrStaticOperation(opInfo);");
			source.println("  }");
			source.println();
			source.println();
			source.println("  @Override");
			source.println("  public CreateOperation getCreateOperation(String opName) throws "
					+ typePonder2OperationException + " {");
			source.println("    CreateOperation op = create.get(opName);");
			source.println("    return op != null ? op : super.getCreateOperation(opName);");
			source.println("  }");
			source.println();
			source.println("  @Override");
			source.println("  public InstanceOperation getInstanceOperation(String opName) throws "
					+ typePonder2OperationException + " {");
			source.println("    InstanceOperation op = operation.get(opName);");
			source.print("    return op != null ? op : ");
			if (wildcard)
				source.println("operation.get(\"" + Ponder2op.WILDCARD + "\");");
			else
				source.println("super.getInstanceOperation(opName);");
			source.println("  }");
			source.println();
			source.println("}");
			source.println();
		}
	}

}
