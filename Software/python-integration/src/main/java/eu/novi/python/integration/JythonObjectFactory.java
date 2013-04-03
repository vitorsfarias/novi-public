package eu.novi.python.integration;

import java.io.File;
import java.util.regex.Matcher;

import org.osgi.framework.BundleContext;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * Creates Python Class instances.
 * 
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>, <a href="mailto:pikusa@man.poznan.pl">Piotr Pikusa</a>
 */
public class JythonObjectFactory {
	protected static String rootDirectory = System.getProperty("user.dir");
	
	/**
	 * Instantiates Python class. Use only in blueprint configuration files. 
	 * 
	 * @param interfaceType Java interface that is implemented by Python class
	 * @param moduleName    name of the module the Python class is in
	 * @return              object implementing interfaceType
	 * @throws ClassNotFoundException when cannot instantiate Python class
	 */
	public static Object createObject(String interfaceType, String moduleName, BundleContext context) throws ClassNotFoundException {
		return JythonObjectFactory.createObject(Class.forName(interfaceType), moduleName, context);
	}
	
	/**
	 * Instantiates Python class.
	 * 
	 * @param interfaceType Java interface that is implemented by Python class
	 * @param moduleName    name of the module the Python class is in
	 * @param context       OSGi bundle context
	 * @return              object implementing interfaceType
	 * @throws ClassNotFoundException when cannot instantiate Python class
	 */
	@SuppressWarnings("unchecked")
	public static <T>T createObject(Class<T> interfaceType, String moduleName, BundleContext context) throws ClassNotFoundException {
		final PythonInterpreter interpreter = new PythonInterpreter();
		//interpreter.getSystemState().setClassLoader(interfaceType.getClass().getClassLoader());
		importModule(moduleName, interpreter, getJarPath(context));
		return (T) createObject(moduleName, interpreter).__tojava__(interfaceType);
	}

	private static String getJarPath(BundleContext context) {
		if (new File(getKarafJarPath(context)).exists())
			return getKarafJarPath(context);
		if (new File(getIntegrationTestingJarPath(context)).exists()) {
			System.out.println("bundle under test: " + getIntegrationTestingJarPath(context));
			return getIntegrationTestingJarPath(context);
		}
		
		System.out.println("Cannot find bundles to test (in directory " + new File(".").getAbsolutePath() + ")");
		return null;
	}

	private static String getIntegrationTestingJarPath(BundleContext context) {
		return Matcher.quoteReplacement("felix" + File.separator
			+ "cache" + File.separator + "runner" + File.separator
				+ "bundle" + context.getBundle().getBundleId()
			            + File.separator + "version0.0" + File.separator + "bundle.jar");
	}

	private static String getKarafJarPath(BundleContext context) {
		return Matcher.quoteReplacement(rootDirectory
		    + File.separator + "data" + File.separator + "cache"
		        + File.separator + "bundle" + context.getBundle().getBundleId()
		            + File.separator + "version0.0" + File.separator + "bundle.jar");
	}

	private static void importModule(String moduleName, final PythonInterpreter interpreter, String jarPath) {
		interpreter.exec("import sys");
		interpreter.exec("sys.path.append(\"" + jarPath + "/Lib\")");
		interpreter.exec("from " + moduleName + " import " + getClassName(moduleName));
	}

	private static String getClassName(String moduleName) {
		final String name;
		if (moduleName.indexOf(".") == -1)
			name = moduleName;
		else
			name = moduleName.substring(moduleName.lastIndexOf(".") + 1);
		return name;
	}

	private static PyObject createObject(String moduleName,
			final PythonInterpreter interpreter) {
		return getClass(moduleName, interpreter).__call__();
	}

	private static PyObject getClass(String moduleName,
			final PythonInterpreter interpreter) {
		return interpreter.get(getClassName(moduleName));
	}
}
