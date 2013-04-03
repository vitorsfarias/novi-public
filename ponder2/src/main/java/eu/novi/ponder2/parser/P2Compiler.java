package eu.novi.ponder2.parser;

import java.io.IOException;
import java.io.InputStream;

import eu.novi.ponder2.parser.PonderTalkLexer;
import eu.novi.ponder2.parser.PonderTalkParser;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.TreeAdaptor;

import eu.novi.ponder2.exception.Ponder2ArgumentException;

//import eu.novi.ponder2.parser.Ponder2AST;

public class P2Compiler {

	/**
	 * TODO Short description
	 */
	static final TreeAdaptor adaptor = new CommonTreeAdaptor() {

		@Override
		public Object create(Token payload) {
			return new Ponder2AST(payload);
		}
	};

	/**
	 * Recursively prints out the tree. Used for debugging
	 * 
	 * @param t
	 *            the tree to be printed
	 * @param indent
	 *            the current indention level
	 */
	public static void printTree(Tree t, int indent) {
		if (t != null) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < indent; i++)
				sb = sb.append("   ");
			for (int i = 0; i < t.getChildCount(); i++) {
				System.out.println(sb.toString() + t.getChild(i).toString());
				printTree(t.getChild(i), indent + 1);
			}
		}
	}

	/**
	 * generates the XML given a PonderTalk abstract syntax tree
	 * 
	 * @param t
	 *            the AST to generate the XML from
	 * @return a string containing the XML for the AST
	 */
	public static String generateXML(String source, Tree t) {
		StringBuffer result = new StringBuffer();
		result.append("<ponder2");
		result.append(lineInfo(source, t));
		result.append('>');
		for (int i = 0; i < t.getChildCount(); i++) {
			Tree child = t.getChild(i);
			result.append(ASTresolve(source, child));
		}
		result.append("</ponder2>");
		return result.toString();
	}

	/**
	 * resolves an AST element, returning the completed XML for that element
	 * type
	 * 
	 * @param element
	 *            the AST element to be resolved
	 * @return the XML for the resolved element
	 */
	public static StringBuffer ASTresolve(String source, Tree element) {
		StringBuffer result;
		switch (element.getType()) {
		case PonderTalkLexer.ASSIGN:
			result = ASTassign(source, element);
			break;
		case PonderTalkLexer.EXPRESSION:
			result = ASTexpression(source, element);
			break;
		case PonderTalkLexer.UNARYMSG:
			result = ASTunaryMsg(element);
			break;
		case PonderTalkLexer.BINARYMSG:
			result = ASTbinaryMsg(source, element);
			break;
		case PonderTalkLexer.KEYWORDMSG:
			result = ASTkeywordMsg(source, element);
			break;
		case PonderTalkLexer.CASCADE:
			result = ASTcascade(source, element);
			break;
		case PonderTalkLexer.LITERAL:
			result = ASTliteral(source, element);
			break;
		case PonderTalkLexer.EOF:
			result = new StringBuffer();
			break;
		default:
			System.err.println("Element unknown: " + element);
			result = new StringBuffer();
		}
		return result;
	}

	/**
	 * resolves an AST literal, returning the completed XML for that element
	 * type
	 * 
	 * @param element
	 *            the AST element to be resolved
	 * @return the XML for the resolved element
	 */
	public static StringBuffer ASTliteral(String source, Tree element) {
		StringBuffer result = new StringBuffer();
		Tree literal = element.getChild(0);
		switch (literal.getType()) {
		// case Ponder2Lexer.LITERAL:
		// result.append(ASTliteral(literal.getChild(0)));
		// break;
		case PonderTalkLexer.STRING:
			result.append("<string value='");
			result.append(quote(literal.getChild(0).toString()));
			result.append('\'');
			result.append(lineInfo(element));
			result.append("/>");
			break;
		case PonderTalkLexer.CHAR:
			result.append("<char value='");
			result.append(quote(literal.getChild(0).toString()));
			result.append('\'');
			result.append(lineInfo(element));
			result.append("/>");
			break;
		case PonderTalkLexer.BOOLEAN:
			result.append("<boolean value='");
			result.append(literal.getChild(0));
			result.append('\'');
			result.append(lineInfo(element));
			result.append("/>");
			break;
		case PonderTalkLexer.NUMBER:
			result.append("<number value='");
			result.append(literal.getChild(0));
			result.append('\'');
			result.append(lineInfo(element));
			result.append("/>");
			break;
		case PonderTalkLexer.EXPRESSION:
			result.append(ASTexpression(source, element));
			break;
		default:
			System.err.println("P2Compiler literal unknown: " + literal);
			result.append("<error name='syntax error in ASTliteral'");
		}
		return result;
	}

	/**
	 * resolves an AST assignment, returning the completed XML for that element
	 * type
	 * 
	 * @param t
	 *            the AST element to be resolved
	 * @return the XML for the resolved element
	 */
	public static StringBuffer ASTassign(String source, Tree t) {
		StringBuffer result = new StringBuffer();
		String name = t.getChild(0).toString();
		if (name.endsWith(":="))
			name = name.substring(0, name.length() - 2);
		result.append("<assign name='" + name + "'");
		result.append(lineInfo(t));
		result.append('>');
		result.append(ASTresolve(source, t.getChild(1)));
		result.append("</assign>");
		return result;
	}

	/**
	 * resolves an AST expression, returning the completed XML for that element
	 * type
	 * 
	 * @param t
	 *            the AST element to be resolved
	 * @return the XML for the resolved element
	 */
	public static StringBuffer ASTexpression(String source, Tree t) {
		Tree obj = t.getChild(0);
		StringBuffer result = new StringBuffer();
		result.append("<send");
		result.append(lineInfo(t));
		result.append('>');
		// A primary is expected here, something that becomes a LHS
		if (obj.getType() == PonderTalkLexer.EXPRESSION)
			result.append(ASTresolve(source, obj));
		else if (obj.getType() == PonderTalkLexer.BLOCK)
			result.append(ASTblock(source, obj));
		else if (obj.getType() == PonderTalkLexer.LITERAL)
			result.append(ASTliteral(source, obj));
		else if (obj.getType() == PonderTalkLexer.ASSIGN)
			result.append(ASTassign(source, obj));
		else if (obj.getType() == PonderTalkLexer.ARRAY)
			result.append(ASTarray(source, obj));
		else {
			result.append("<use name='" + obj + "'");
			result.append(lineInfo(t));
			result.append("/>");
		}
		for (int i = 1; i < t.getChildCount(); i++) {
			Tree child = t.getChild(i);
			if (child.getType() != PonderTalkLexer.CASCADE) {
				if (i > 1) {
					result.append("</send>");
					result.insert(0, "<send" + lineInfo(t) + ">");
				}
				result.append(ASTresolve(source, child));
			} else {
				result.append(ASTcascade(source, child));
			}
		}
		result.append("</send>");
		return result;
	}

	/**
	 * Resolves an AST array, returning the completed XML for that element type
	 * 
	 * @param t
	 *            the AST element to be resolved
	 * @return the XML for the resolved element
	 */
	public static StringBuffer ASTarray(String source, Tree t) {
		StringBuffer result = new StringBuffer();
		result.append("<array ");
		result.append(lineInfo(t));
		result.append(">");
		int children = t.getChildCount();
		for (int i = 0; i < children; i++) {
			result.append(ASTresolve(source, t.getChild(i)));
		}
		result.append("</array>");
		return result;
	}

	/**
	 * Parse a block. A block always has two children, "bargs" and "bcode" each
	 * with zero or more children of their own.
	 * 
	 */
	public static StringBuffer ASTblock(String source, Tree t) {
		StringBuffer result = new StringBuffer();
		result.append("<block");
		result.append(lineInfo(source, t));
		result.append('>');
		result.append(ASTblockArgs(t.getChild(0)));
		result.append(ASTblockCode(source, t.getChild(1)));
		result.append("</block>");
		return result;
	}

	/**
	 * Parse a set of block arguments
	 */
	public static StringBuffer ASTblockArgs(Tree t) {
		StringBuffer result = new StringBuffer();
		int args = t.getChildCount();
		// if (args == 0)
		// return result;
		result.append("<bargs args='" + args + "'");
		result.append(lineInfo(t));
		result.append('>');
		// Now dump out the args themselves
		for (int i = 0; i < args; i++) {
			Tree arg = t.getChild(i);
			result.append("<barg name='" + arg + "'");
			result.append(lineInfo(arg));
			result.append("/>");
		}
		result.append("</bargs>");
		return result;
	}

	/**
	 * Parse the block code
	 */
	public static StringBuffer ASTblockCode(String source, Tree t) {
		StringBuffer result = new StringBuffer();
		result.append("<ponder2");
		result.append(lineInfo(source, t));
		result.append('>');
		// Now dump out the code statements
		int children = t.getChildCount();
		for (int i = 0; i < children; i++) {
			result.append(ASTresolve(source, t.getChild(i)));
		}
		result.append("</ponder2>");
		return result;
	}

	/**
	 * Parse a unary message
	 */
	public static StringBuffer ASTunaryMsg(Tree t) {
		StringBuffer result = new StringBuffer("<message type='unary' name='"
				+ t.getChild(0) + "'");
		result.append(lineInfo(t));
		result.append("/>");
		return result;
	}

	/**
	 * Parse a binary message
	 */
	public static StringBuffer ASTbinaryMsg(String source, Tree t) {
		StringBuffer result = new StringBuffer();
		StringBuffer msgName = new StringBuffer(quote(t.getChild(0).toString()));
		result.append("<message type='binary' name='" + msgName + "' args='1'");
		result.append(lineInfo(t));
		result.append('>');
		result.append("<arg name='" + msgName + "'");
		result.append(lineInfo(t));
		result.append('>');
		result.append(ASTresolve(source, t.getChild(1)));
		result.append("</arg>");
		result.append("</message>");
		return result;
	}

	/**
	 * Parse a keyword message
	 */
	public static StringBuffer ASTkeywordMsg(String source, Tree t) {
		StringBuffer keywords = new StringBuffer();
		StringBuffer body = new StringBuffer();
		int length = t.getChildCount();
		for (int i = 0; i < length; i++) {
			Tree child = t.getChild(i++);
			StringBuffer argName = new StringBuffer(child.toString());
			// argName.deleteCharAt(argName.length() - 1);
			// if (keywords.length() > 0)
			// keywords.append(' ');
			keywords.append(argName);
			body.append("<arg name='" + argName + "'");
			body.append(lineInfo(t));
			body.append('>');
			body.append(ASTresolve(source, t.getChild(i)));
			body.append("</arg>");
		}
		StringBuffer result = new StringBuffer();
		result.append("<message type='keyword' name='" + keywords + "' args='"
				+ length / 2 + "'");
		result.append(lineInfo(t));
		result.append('>');
		result.append(body);
		result.append("</message>");
		return result;
	}

	/**
	 * Parse a cascade message
	 */
	public static StringBuffer ASTcascade(String source, Tree t) {
		StringBuffer result = new StringBuffer("");
		for (int i = 0; i < t.getChildCount(); i++) {
			result.append(ASTresolve(source, t.getChild(i)));
		}
		return result;
	}

	/**
	 * Return the line and character position of the token in the source
	 */
	public static String lineInfo(Tree t) {
		return " line='" + t.getLine() + ":" + t.getCharPositionInLine() + "'";
	}

	/**
	 * Return the source name, the line and character position of the token in
	 * the source
	 */
	public static String lineInfo(String source, Tree t) {
		return " source='" + source + "'" + lineInfo(t);
	}

	public static String quote(String string) {
		StringBuffer buf = new StringBuffer();
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char ch = string.charAt(i);
			buf.append(quote(ch));
		}
		return buf.toString();
	}

	/**
	 * Quote sensitive XML characters
	 * 
	 * @param ch
	 *            the character to be quoted
	 * @return a string with the character or special quote string
	 */
	public static String quote(char ch) {
		String result;
		if (ch == '>')
			result = "&gt;";
		else if (ch == '<')
			result = "&lt;";
		else if (ch == '&')
			result = "&amp;";
		else if (ch == '\'')
			result = "&apos;";
		else if (ch == '"')
			result = "&quot;";
		else
			result = "" + ch;
		return result;
	}

	/**
	 * Parse and compile a PonderTalk string
	 * 
	 * @param input
	 *            the PonderTalk to be compiled
	 * @return a string containing the XML representation of the PonderTalk
	 * @throws Ponder2ArgumentException
	 */
	public static String parse(String input) throws Ponder2ArgumentException {
		ANTLRStringStream fs;
		fs = new ANTLRStringStream(input);
		return parse("UnknownFile", fs);
	}

	/**
	 * Parse and compile a PonderTalk InputStream
	 * 
	 * @param input
	 *            the PonderTalk InputStream
	 * @return a string containing the XML representation of the PonderTalk
	 * @throws Ponder2ArgumentException
	 */
	public static String parse(String source, InputStream input)
			throws Ponder2ArgumentException {
		ANTLRStringStream fs;
		try {
			fs = new ANTLRInputStream(input);
			return parse(source, fs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("P2Compiler parse(inputstream)");
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * parse and compile a PonderTalk CharStream
	 * 
	 * @param input
	 *            the PonderTalk CharStream
	 * @return a string containing the XML representation of the PonderTalk
	 * @throws Ponder2ArgumentException
	 */
	public static String parse(String source, CharStream input)
			throws Ponder2ArgumentException {
		String result = "";
		PonderTalkLexer lex = new PonderTalkLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lex);

		PonderTalkParser g = new PonderTalkParser(tokens) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.antlr.runtime.BaseRecognizer#reportError(org.antlr.runtime
			 * .RecognitionException)
			 */
			@Override
			public void reportError(RecognitionException arg0) {
				throw new RuntimeException(arg0);
				// super.reportError(arg0);
			}

		};

		try {
			g.setTreeAdaptor(adaptor);
			PonderTalkParser.start_return ret = g.start();
			Ponder2AST tree = (Ponder2AST) ret.getTree();
			if (dumpTree)
				printTree(tree, 8);
			result = generateXML(source, tree);
			if (dumpTree)
				System.out.println(result);
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
		// This is a nasty way of getting out of the middle of the Parser code
		// if an
		// error occurs
		catch (RuntimeException e) {
			Throwable exception = e.getCause();
			// If this is not one of our's then throw it up!
			if (exception instanceof NoViableAltException) {
				NoViableAltException syntaxError = (NoViableAltException) exception;
				CommonToken token = (CommonToken) syntaxError.token;
				// String s = "Syntax error in line " + syntaxError.line +
				// ":\n";
				int linenum = syntaxError.line;
				int got = syntaxError.getUnexpectedType();
				String s;
				if (got < 0)
					s = "Syntax error:  " + source
							+ ": unexpected end of file\n";
				else {
					s = "Syntax error:  got "
							+ PonderTalkParser.tokenNames[got] + " in  "
							+ source + ":" + linenum + ":\n";
					s += getErrorLines(tokens, token);
				}
				throw new Ponder2ArgumentException(s);
			} else if (exception instanceof MismatchedTokenException) {
				MismatchedTokenException mismatch = (MismatchedTokenException) exception;
				int linenum = mismatch.line;
				int expecting = mismatch.expecting;
				int got = mismatch.getUnexpectedType();
				CommonToken token = (CommonToken) mismatch.token;
				String s;
				if (expecting != -1 && got != -1)
					s = "Mismatched token: expected  "
							+ PonderTalkParser.tokenNames[expecting]
							+ " but got " + PonderTalkParser.tokenNames[got];
				else if (got != -1)
					s = "Mismatched token: got "
							+ PonderTalkParser.tokenNames[got];
				else if (expecting != -1)
					s = "Mismatched token: expected  "
							+ PonderTalkParser.tokenNames[expecting]
							+ " but got \"End Of File\"";
				else
					s = "Mismatched token: got \"End Of File\"";
				s += " in  " + source + ":" + linenum + ":\n";
				s += getErrorLines(tokens, token);
				// TODO work back and forwards to enclosing lines and number
				// them
				System.out.println(s);
				throw new Ponder2ArgumentException(s);
			} else {
				System.err.println("Exception thrown: " + e.getMessage());
				throw e;
			}
		}
		return result;
	}

	/**
	 * Return a highlighted line pointing to a token, used for error messages.
	 * 
	 * @param tokens
	 *            the token input stream
	 * @param token
	 *            the token to be highlighted
	 * @return a line with the token pointed out
	 */
	protected static String getErrorLines(CommonTokenStream tokens, Token token) {
		int tokenNumber = token.getTokenIndex();
		int tlinenum = token.getLine();
		Token temp = token;
		int tnum = tokenNumber;
		while (temp.getLine() >= tlinenum - 1 && tnum > 0) {
			temp = tokens.get(--tnum);
		}
		Token lineStart = temp;
		tnum = tokenNumber;
		temp = token;
		int max = tokens.size() - 1;
		while (temp.getLine() <= tlinenum + 1 && tnum < max) {
			temp = tokens.get(++tnum);
		}
		Token lineEnd = temp;

		StringBuffer line = new StringBuffer();
		// KPT TODO EOL correct?
		if (tokenNumber <= 0) {
			line.append("at end of file");
		} else {
			line.append(tokens.toString(lineStart, tokens.get(tokenNumber - 1)));
			if (line.charAt(0) == '\n')
				line.deleteCharAt(0);
			line.append(" -->>>");
			line.append(token.getText());
			line.append("<<<--");
			if (tokenNumber < lineEnd.getTokenIndex())
				line.append(tokens.toString(tokens.get(tokenNumber + 1),
						lineEnd));
		}

		return line.toString();
	}

	/**
	 * If true then dump the XML tree to stdout after compilation
	 */
	public static boolean dumpTree = false;

	/**
	 * Used for testing
	 */
	public static void main(String args[]) throws Exception {
		String file = "/Users/kevin/workspace/Ponder2Language/parser/__Test___input.txt";
		ANTLRFileStream fs = new ANTLRFileStream(file);
		dumpTree = true;
		String xml = parse(file, fs);
		System.out.println(xml);
	}

	/**
	 * Extension to the ANTLR AST so that entries can be printed out easily
	 * 
	 * @author Kevin Twidle
	 */
	public static class Ponder2AST extends CommonTree {

		public String text;

		public Ponder2AST(Token t) {
			super(t);
			text = (t != null ? t.getText() : "[]");
		}

		@Override
		public String toString() {
			// return "'"+text + super.toString()+"'";
			return super.toString();
		}
	}
}