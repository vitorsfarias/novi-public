package eu.novi.ponder2.parser;

// $ANTLR 3.2 Sep 23, 2009 12:02:23 /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g 2009-12-09 16:00:39

import java.io.InputStream;
import java.net.URI;
import java.util.Stack;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;

import eu.novi.ponder2.Util;
//import eu.novi.ponder2.parser.DFA14;
//import eu.novi.ponder2.parser.DFA22;
//import eu.novi.ponder2.parser.SaveStruct;

public class PonderTalkLexer extends Lexer {
	public static final int KEYWORDMSG = 17;
	public static final int BLOCKARG = 29;
	public static final int PATHNAME = 36;
	public static final int ARRAY = 5;
	public static final int BIGDIGITS = 42;
	public static final int T__51 = 51;
	public static final int NUMBER = 22;
	public static final int BARGS = 7;
	public static final int T__47 = 47;
	public static final int BINARYOP = 37;
	public static final int T__50 = 50;
	public static final int DECIMAL = 41;
	public static final int PIPE = 30;
	public static final int BINARYMSG = 16;
	public static final int DOT = 25;
	public static final int BLOCK = 6;
	public static final int EXPRESSION = 10;
	public static final int KEYWORDARG = 18;
	public static final int T__52 = 52;
	public static final int LITERAL = 20;
	public static final int INCLUDE = 45;
	public static final int VARIABLEASSIGN = 28;
	public static final int ASSIGN = 27;
	public static final int T__49 = 49;
	public static final int STRINGLITERAL = 34;
	public static final int HEXDIGITS = 43;
	public static final int DIGIT = 40;
	public static final int T__48 = 48;
	public static final int BINARYCHAR = 38;
	public static final int OBJECT = 11;
	public static final int LETTER = 39;
	public static final int CASCADE = 14;
	public static final int WS = 44;
	public static final int PARENS = 13;
	public static final int IMMEDIATE = 19;
	public static final int KEYWORD = 35;
	public static final int CHAR = 23;
	public static final int STRING = 21;
	public static final int COMMENT = 46;
	public static final int NUMBERLITERAL = 33;
	public static final int MESSAGE = 12;
	public static final int UNARYMSG = 15;
	public static final int BOOLEAN = 24;
	public static final int TEMP = 4;
	public static final int EOF = -1;
	public static final int BOOLEANLITERAL = 31;
	public static final int BCODE = 8;
	public static final int IDENTIFIER = 26;
	public static final int CHARACTERLITERAL = 32;
	public static final int BSTATEMENTS = 9;

	// Remove leading and trailing "s and escapes in the string
	public String stripString(String string) {
		StringBuffer buf = new StringBuffer(string);
		// Remove leading and trailing quotes
		buf.deleteCharAt(0);
		buf.deleteCharAt(buf.length() - 1);
		// Remove any \ from \"
		int index = 0;
		while (true) {
			index = buf.indexOf("\\\"", index);
			if (index == -1)
				break;
			buf.deleteCharAt(index);
		}
		return buf.toString();
	}

	public String readNumber(String number) {
		System.out.println("Number is " + number);
		int index = number.indexOf("r");
		if (index == -1)
			return number;
		String sradix = number.substring(0, index);
		String snumber = number.substring(index + 1);
		int radix = Integer.parseInt(sradix);
		long num = 0;
		for (int i = 0; i < snumber.length(); i++) {
			num = num * radix;
			char ch = snumber.charAt(i);
			int digit;
			if (ch >= '0' && ch <= '9')
				digit = ch - '0';
			else if (ch >= 'a' && ch <= 'z')
				digit = ch - 'a' + 10;
			else
				digit = ch - 'A' + 10;
			num += digit;
		}
		return "" + num;
	}

	class SaveStruct {
		SaveStruct(CharStream input) {
			this.input = input;
			this.marker = input.mark();
		}

		public CharStream input;
		public int marker;
	}

	Stack<SaveStruct> includes = new Stack<SaveStruct>();

	// We should override this method for handling EOF of included file
	public Token nextToken() {
		Token token = super.nextToken();

		if (token == Token.EOF_TOKEN && !includes.empty()) {
			// We've got EOF and have non empty stack.
			SaveStruct ss = includes.pop();
			setCharStream(ss.input);
			input.rewind(ss.marker);
			token = this.nextToken();
		}

		// Skip first token after switching on another input.
		if (((CommonToken) token).getStartIndex() < 0)
			token = this.nextToken();

		return token;
	}

	// delegates
	// delegators

	public PonderTalkLexer() {
		;
	}

	public PonderTalkLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}

	public PonderTalkLexer(CharStream input, RecognizerSharedState state) {
		super(input, state);

	}

	public String getGrammarFileName() {
		return "/Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g";
	}

	// $ANTLR start "T__47"
	public final void mT__47() throws RecognitionException {
		try {
			int _type = T__47;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:75:7:
			// ( ';' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:75:9:
			// ';'
			{
				match(';');
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "T__47"

	// $ANTLR start "T__48"
	public final void mT__48() throws RecognitionException {
		try {
			int _type = T__48;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:76:7:
			// ( '(' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:76:9:
			// '('
			{
				match('(');
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "T__48"

	// $ANTLR start "T__49"
	public final void mT__49() throws RecognitionException {
		try {
			int _type = T__49;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:77:7:
			// ( ')' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:77:9:
			// ')'
			{
				match(')');
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "T__49"

	// $ANTLR start "T__50"
	public final void mT__50() throws RecognitionException {
		try {
			int _type = T__50;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:78:7:
			// ( '#(' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:78:9:
			// '#('
			{
				match("#(");
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "T__50"

	// $ANTLR start "T__51"
	public final void mT__51() throws RecognitionException {
		try {
			int _type = T__51;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:79:7:
			// ( '[' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:79:9:
			// '['
			{
				match('[');
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "T__51"

	// $ANTLR start "T__52"
	public final void mT__52() throws RecognitionException {
		try {
			int _type = T__52;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:80:7:
			// ( ']' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:80:9:
			// ']'
			{
				match(']');
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "T__52"

	// $ANTLR start "BINARYOP"
	public final void mBINARYOP() throws RecognitionException {
		try {
			int _type = BINARYOP;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:174:9:
			// ( ( BINARYCHAR )+ )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:174:11:
			// ( BINARYCHAR )+
			{
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:174:11:
				// ( BINARYCHAR )+
				int cnt1 = 0;
				loop1: do {
					int alt1 = 2;
					int LA1_0 = input.LA(1);

					if ((LA1_0 == '!' || (LA1_0 >= '%' && LA1_0 <= '&')
							|| (LA1_0 >= '*' && LA1_0 <= '-') || LA1_0 == '/'
							|| (LA1_0 >= '<' && LA1_0 <= '@') || LA1_0 == '\\'
							|| LA1_0 == '^' || LA1_0 == '~')) {
						alt1 = 1;
					}

					switch (alt1) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:174:11:
					// BINARYCHAR
					{
						mBINARYCHAR();
						if (state.failed)
							return;

					}
						break;

					default:
						if (cnt1 >= 1)
							break loop1;
						if (state.backtracking > 0) {
							state.failed = true;
							return;
						}
						EarlyExitException eee = new EarlyExitException(1,
								input);
						throw eee;
					}
					cnt1++;
				} while (true);

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "BINARYOP"

	// $ANTLR start "BOOLEANLITERAL"
	public final void mBOOLEANLITERAL() throws RecognitionException {
		try {
			int _type = BOOLEANLITERAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:176:16:
			// ( 'true' | 'false' )
			int alt2 = 2;
			int LA2_0 = input.LA(1);

			if ((LA2_0 == 't')) {
				alt2 = 1;
			} else if ((LA2_0 == 'f')) {
				alt2 = 2;
			} else {
				if (state.backtracking > 0) {
					state.failed = true;
					return;
				}
				NoViableAltException nvae = new NoViableAltException("", 2, 0,
						input);

				throw nvae;
			}
			switch (alt2) {
			case 1:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:176:17:
			// 'true'
			{
				match("true");
				if (state.failed)
					return;

			}
				break;
			case 2:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:176:26:
			// 'false'
			{
				match("false");
				if (state.failed)
					return;

			}
				break;

			}
			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "BOOLEANLITERAL"

	// $ANTLR start "VARIABLEASSIGN"
	public final void mVARIABLEASSIGN() throws RecognitionException {
		try {
			int _type = VARIABLEASSIGN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			CommonToken id = null;

			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:179:2:
			// (id= IDENTIFIER ASSIGN )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:179:4:
			// id= IDENTIFIER ASSIGN
			{
				int idStart94 = getCharIndex();
				mIDENTIFIER();
				if (state.failed)
					return;
				id = new CommonToken(input, Token.INVALID_TOKEN_TYPE,
						Token.DEFAULT_CHANNEL, idStart94, getCharIndex() - 1);
				mASSIGN();
				if (state.failed)
					return;
				if (state.backtracking == 0) {
					setText(id.getText());
				}

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "VARIABLEASSIGN"

	// $ANTLR start "ASSIGN"
	public final void mASSIGN() throws RecognitionException {
		try {
			int _type = ASSIGN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:181:8:
			// ( ':=' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:181:10:
			// ':='
			{
				match(":=");
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "ASSIGN"

	// $ANTLR start "DOT"
	public final void mDOT() throws RecognitionException {
		try {
			int _type = DOT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:183:5:
			// ( '.' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:183:7:
			// '.'
			{
				match('.');
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "DOT"

	// $ANTLR start "BLOCKARG"
	public final void mBLOCKARG() throws RecognitionException {
		try {
			int _type = BLOCKARG;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			CommonToken id = null;

			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:185:9:
			// ( ':' id= IDENTIFIER )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:185:11:
			// ':' id= IDENTIFIER
			{
				match(':');
				if (state.failed)
					return;
				int idStart131 = getCharIndex();
				mIDENTIFIER();
				if (state.failed)
					return;
				id = new CommonToken(input, Token.INVALID_TOKEN_TYPE,
						Token.DEFAULT_CHANNEL, idStart131, getCharIndex() - 1);
				if (state.backtracking == 0) {
					setText(id.getText());
				}

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "BLOCKARG"

	// $ANTLR start "KEYWORD"
	public final void mKEYWORD() throws RecognitionException {
		try {
			int _type = KEYWORD;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:187:9:
			// ( IDENTIFIER ':' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:187:11:
			// IDENTIFIER ':'
			{
				mIDENTIFIER();
				if (state.failed)
					return;
				match(':');
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "KEYWORD"

	// $ANTLR start "PATHNAME"
	public final void mPATHNAME() throws RecognitionException {
		try {
			int _type = PATHNAME;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:189:9:
			// ( IDENTIFIER ( '/' ( LETTER | DIGIT | '_' )+ )+ )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:189:11:
			// IDENTIFIER ( '/' ( LETTER | DIGIT | '_' )+ )+
			{
				mIDENTIFIER();
				if (state.failed)
					return;
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:189:22:
				// ( '/' ( LETTER | DIGIT | '_' )+ )+
				int cnt4 = 0;
				loop4: do {
					int alt4 = 2;
					int LA4_0 = input.LA(1);

					if ((LA4_0 == '/')) {
						alt4 = 1;
					}

					switch (alt4) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:189:24:
					// '/' ( LETTER | DIGIT | '_' )+
					{
						match('/');
						if (state.failed)
							return;
						// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:189:28:
						// ( LETTER | DIGIT | '_' )+
						int cnt3 = 0;
						loop3: do {
							int alt3 = 2;
							int LA3_0 = input.LA(1);

							if (((LA3_0 >= '0' && LA3_0 <= '9')
									|| (LA3_0 >= 'A' && LA3_0 <= 'Z')
									|| LA3_0 == '_' || (LA3_0 >= 'a' && LA3_0 <= 'z'))) {
								alt3 = 1;
							}

							switch (alt3) {
							case 1:
							// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:
							{
								if ((input.LA(1) >= '0' && input.LA(1) <= '9')
										|| (input.LA(1) >= 'A' && input.LA(1) <= 'Z')
										|| input.LA(1) == '_'
										|| (input.LA(1) >= 'a' && input.LA(1) <= 'z')) {
									input.consume();
									state.failed = false;
								} else {
									if (state.backtracking > 0) {
										state.failed = true;
										return;
									}
									MismatchedSetException mse = new MismatchedSetException(
											null, input);
									recover(mse);
									throw mse;
								}

							}
								break;

							default:
								if (cnt3 >= 1)
									break loop3;
								if (state.backtracking > 0) {
									state.failed = true;
									return;
								}
								EarlyExitException eee = new EarlyExitException(
										3, input);
								throw eee;
							}
							cnt3++;
						} while (true);

					}
						break;

					default:
						if (cnt4 >= 1)
							break loop4;
						if (state.backtracking > 0) {
							state.failed = true;
							return;
						}
						EarlyExitException eee = new EarlyExitException(4,
								input);
						throw eee;
					}
					cnt4++;
				} while (true);

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "PATHNAME"

	// $ANTLR start "IDENTIFIER"
	public final void mIDENTIFIER() throws RecognitionException {
		try {
			int _type = IDENTIFIER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:192:2:
			// ( LETTER ( LETTER | DIGIT | '_' )* )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:192:4:
			// LETTER ( LETTER | DIGIT | '_' )*
			{
				mLETTER();
				if (state.failed)
					return;
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:192:11:
				// ( LETTER | DIGIT | '_' )*
				loop5: do {
					int alt5 = 2;
					int LA5_0 = input.LA(1);

					if (((LA5_0 >= '0' && LA5_0 <= '9')
							|| (LA5_0 >= 'A' && LA5_0 <= 'Z') || LA5_0 == '_' || (LA5_0 >= 'a' && LA5_0 <= 'z'))) {
						alt5 = 1;
					}

					switch (alt5) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:
					{
						if ((input.LA(1) >= '0' && input.LA(1) <= '9')
								|| (input.LA(1) >= 'A' && input.LA(1) <= 'Z')
								|| input.LA(1) == '_'
								|| (input.LA(1) >= 'a' && input.LA(1) <= 'z')) {
							input.consume();
							state.failed = false;
						} else {
							if (state.backtracking > 0) {
								state.failed = true;
								return;
							}
							MismatchedSetException mse = new MismatchedSetException(
									null, input);
							recover(mse);
							throw mse;
						}

					}
						break;

					default:
						break loop5;
					}
				} while (true);

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "IDENTIFIER"

	// $ANTLR start "CHARACTERLITERAL"
	public final void mCHARACTERLITERAL() throws RecognitionException {
		try {
			int _type = CHARACTERLITERAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:194:18:
			// ( '$' . )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:195:4:
			// '$' .
			{
				match('$');
				if (state.failed)
					return;
				matchAny();
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "CHARACTERLITERAL"

	// $ANTLR start "STRINGLITERAL"
	public final void mSTRINGLITERAL() throws RecognitionException {
		try {
			int _type = STRINGLITERAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:198:2:
			// ( '\"' ( '\\\\\"' | (~ '\"' ) )* '\"' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:198:4:
			// '\"' ( '\\\\\"' | (~ '\"' ) )* '\"'
			{
				match('\"');
				if (state.failed)
					return;
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:198:8:
				// ( '\\\\\"' | (~ '\"' ) )*
				loop6: do {
					int alt6 = 3;
					int LA6_0 = input.LA(1);

					if ((LA6_0 == '\\')) {
						int LA6_2 = input.LA(2);

						if ((LA6_2 == '\"')) {
							int LA6_4 = input.LA(3);

							if (((LA6_4 >= '\u0000' && LA6_4 <= '\uFFFF'))) {
								alt6 = 1;
							}

							else {
								alt6 = 2;
							}

						} else if (((LA6_2 >= '\u0000' && LA6_2 <= '!') || (LA6_2 >= '#' && LA6_2 <= '\uFFFF'))) {
							alt6 = 2;
						}

					} else if (((LA6_0 >= '\u0000' && LA6_0 <= '!')
							|| (LA6_0 >= '#' && LA6_0 <= '[') || (LA6_0 >= ']' && LA6_0 <= '\uFFFF'))) {
						alt6 = 2;
					}

					switch (alt6) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:198:9:
					// '\\\\\"'
					{
						match("\\\"");
						if (state.failed)
							return;

					}
						break;
					case 2:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:198:15:
					// (~ '\"' )
					{
						// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:198:15:
						// (~ '\"' )
						// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:198:16:
						// ~ '\"'
						{
							if ((input.LA(1) >= '\u0000' && input.LA(1) <= '!')
									|| (input.LA(1) >= '#' && input.LA(1) <= '\uFFFF')) {
								input.consume();
								state.failed = false;
							} else {
								if (state.backtracking > 0) {
									state.failed = true;
									return;
								}
								MismatchedSetException mse = new MismatchedSetException(
										null, input);
								recover(mse);
								throw mse;
							}

						}

					}
						break;

					default:
						break loop6;
					}
				} while (true);

				match('\"');
				if (state.failed)
					return;
				if (state.backtracking == 0) {
					setText(stripString(getText()));
				}

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "STRINGLITERAL"

	// $ANTLR start "NUMBERLITERAL"
	public final void mNUMBERLITERAL() throws RecognitionException {
		try {
			int _type = NUMBERLITERAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			CommonToken d = null;

			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:202:2:
			// ( ( '-' )? ( DIGIT )+ DECIMAL ( ( 'e' | 'E' ) ( '-' )? ( DIGIT )+
			// )? | ( DIGIT )+ 'r' ( '-' )? BIGDIGITS | '0x' d= HEXDIGITS )
			int alt14 = 3;
			alt14 = dfa14.predict(input);
			switch (alt14) {
			case 1:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:202:4:
			// ( '-' )? ( DIGIT )+ DECIMAL ( ( 'e' | 'E' ) ( '-' )? ( DIGIT )+
			// )?
			{
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:202:4:
				// ( '-' )?
				int alt7 = 2;
				int LA7_0 = input.LA(1);

				if ((LA7_0 == '-')) {
					alt7 = 1;
				}
				switch (alt7) {
				case 1:
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:202:4:
				// '-'
				{
					match('-');
					if (state.failed)
						return;

				}
					break;

				}

				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:202:9:
				// ( DIGIT )+
				int cnt8 = 0;
				loop8: do {
					int alt8 = 2;
					int LA8_0 = input.LA(1);

					if (((LA8_0 >= '0' && LA8_0 <= '9'))) {
						alt8 = 1;
					}

					switch (alt8) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:202:9:
					// DIGIT
					{
						mDIGIT();
						if (state.failed)
							return;

					}
						break;

					default:
						if (cnt8 >= 1)
							break loop8;
						if (state.backtracking > 0) {
							state.failed = true;
							return;
						}
						EarlyExitException eee = new EarlyExitException(8,
								input);
						throw eee;
					}
					cnt8++;
				} while (true);

				mDECIMAL();
				if (state.failed)
					return;
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:202:24:
				// ( ( 'e' | 'E' ) ( '-' )? ( DIGIT )+ )?
				int alt11 = 2;
				int LA11_0 = input.LA(1);

				if ((LA11_0 == 'E' || LA11_0 == 'e')) {
					alt11 = 1;
				}
				switch (alt11) {
				case 1:
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:202:25:
				// ( 'e' | 'E' ) ( '-' )? ( DIGIT )+
				{
					if (input.LA(1) == 'E' || input.LA(1) == 'e') {
						input.consume();
						state.failed = false;
					} else {
						if (state.backtracking > 0) {
							state.failed = true;
							return;
						}
						MismatchedSetException mse = new MismatchedSetException(
								null, input);
						recover(mse);
						throw mse;
					}

					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:202:35:
					// ( '-' )?
					int alt9 = 2;
					int LA9_0 = input.LA(1);

					if ((LA9_0 == '-')) {
						alt9 = 1;
					}
					switch (alt9) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:202:35:
					// '-'
					{
						match('-');
						if (state.failed)
							return;

					}
						break;

					}

					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:202:40:
					// ( DIGIT )+
					int cnt10 = 0;
					loop10: do {
						int alt10 = 2;
						int LA10_0 = input.LA(1);

						if (((LA10_0 >= '0' && LA10_0 <= '9'))) {
							alt10 = 1;
						}

						switch (alt10) {
						case 1:
						// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:202:40:
						// DIGIT
						{
							mDIGIT();
							if (state.failed)
								return;

						}
							break;

						default:
							if (cnt10 >= 1)
								break loop10;
							if (state.backtracking > 0) {
								state.failed = true;
								return;
							}
							EarlyExitException eee = new EarlyExitException(10,
									input);
							throw eee;
						}
						cnt10++;
					} while (true);

				}
					break;

				}

			}
				break;
			case 2:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:203:4:
			// ( DIGIT )+ 'r' ( '-' )? BIGDIGITS
			{
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:203:4:
				// ( DIGIT )+
				int cnt12 = 0;
				loop12: do {
					int alt12 = 2;
					int LA12_0 = input.LA(1);

					if (((LA12_0 >= '0' && LA12_0 <= '9'))) {
						alt12 = 1;
					}

					switch (alt12) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:203:4:
					// DIGIT
					{
						mDIGIT();
						if (state.failed)
							return;

					}
						break;

					default:
						if (cnt12 >= 1)
							break loop12;
						if (state.backtracking > 0) {
							state.failed = true;
							return;
						}
						EarlyExitException eee = new EarlyExitException(12,
								input);
						throw eee;
					}
					cnt12++;
				} while (true);

				match('r');
				if (state.failed)
					return;
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:203:15:
				// ( '-' )?
				int alt13 = 2;
				int LA13_0 = input.LA(1);

				if ((LA13_0 == '-')) {
					alt13 = 1;
				}
				switch (alt13) {
				case 1:
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:203:15:
				// '-'
				{
					match('-');
					if (state.failed)
						return;

				}
					break;

				}

				mBIGDIGITS();
				if (state.failed)
					return;
				if (state.backtracking == 0) {
					setText(readNumber(getText()));
				}

			}
				break;
			case 3:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:204:4:
			// '0x' d= HEXDIGITS
			{
				match("0x");
				if (state.failed)
					return;

				int dStart296 = getCharIndex();
				mHEXDIGITS();
				if (state.failed)
					return;
				d = new CommonToken(input, Token.INVALID_TOKEN_TYPE,
						Token.DEFAULT_CHANNEL, dStart296, getCharIndex() - 1);
				if (state.backtracking == 0) {
					setText(readNumber("16r" + d.getText()));
				}

			}
				break;

			}
			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "NUMBERLITERAL"

	// $ANTLR start "PIPE"
	public final void mPIPE() throws RecognitionException {
		try {
			int _type = PIPE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:206:6:
			// ( '|' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:206:8:
			// '|'
			{
				match('|');
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "PIPE"

	// $ANTLR start "BINARYCHAR"
	public final void mBINARYCHAR() throws RecognitionException {
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:209:2:
			// ( '!' | '%' | '&' | '*' | '+' | '/' | '-' | '<' | '=' | '>' | '?'
			// | '@' | '\\\\' | '~' | ',' | '^' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:
			{
				if (input.LA(1) == '!'
						|| (input.LA(1) >= '%' && input.LA(1) <= '&')
						|| (input.LA(1) >= '*' && input.LA(1) <= '-')
						|| input.LA(1) == '/'
						|| (input.LA(1) >= '<' && input.LA(1) <= '@')
						|| input.LA(1) == '\\' || input.LA(1) == '^'
						|| input.LA(1) == '~') {
					input.consume();
					state.failed = false;
				} else {
					if (state.backtracking > 0) {
						state.failed = true;
						return;
					}
					MismatchedSetException mse = new MismatchedSetException(
							null, input);
					recover(mse);
					throw mse;
				}

			}

		} finally {
		}
	}

	// $ANTLR end "BINARYCHAR"

	// $ANTLR start "LETTER"
	public final void mLETTER() throws RecognitionException {
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:212:2:
			// ( 'a' .. 'z' | 'A' .. 'Z' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:
			{
				if ((input.LA(1) >= 'A' && input.LA(1) <= 'Z')
						|| (input.LA(1) >= 'a' && input.LA(1) <= 'z')) {
					input.consume();
					state.failed = false;
				} else {
					if (state.backtracking > 0) {
						state.failed = true;
						return;
					}
					MismatchedSetException mse = new MismatchedSetException(
							null, input);
					recover(mse);
					throw mse;
				}

			}

		} finally {
		}
	}

	// $ANTLR end "LETTER"

	// $ANTLR start "DIGIT"
	public final void mDIGIT() throws RecognitionException {
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:215:2:
			// ( '0' .. '9' )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:215:4:
			// '0' .. '9'
			{
				matchRange('0', '9');
				if (state.failed)
					return;

			}

		} finally {
		}
	}

	// $ANTLR end "DIGIT"

	// $ANTLR start "HEXDIGITS"
	public final void mHEXDIGITS() throws RecognitionException {
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:218:2:
			// ( ( DIGIT | 'A' .. 'F' | 'a' .. 'f' )+ )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:218:4:
			// ( DIGIT | 'A' .. 'F' | 'a' .. 'f' )+
			{
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:218:4:
				// ( DIGIT | 'A' .. 'F' | 'a' .. 'f' )+
				int cnt15 = 0;
				loop15: do {
					int alt15 = 2;
					int LA15_0 = input.LA(1);

					if (((LA15_0 >= '0' && LA15_0 <= '9')
							|| (LA15_0 >= 'A' && LA15_0 <= 'F') || (LA15_0 >= 'a' && LA15_0 <= 'f'))) {
						alt15 = 1;
					}

					switch (alt15) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:
					{
						if ((input.LA(1) >= '0' && input.LA(1) <= '9')
								|| (input.LA(1) >= 'A' && input.LA(1) <= 'F')
								|| (input.LA(1) >= 'a' && input.LA(1) <= 'f')) {
							input.consume();
							state.failed = false;
						} else {
							if (state.backtracking > 0) {
								state.failed = true;
								return;
							}
							MismatchedSetException mse = new MismatchedSetException(
									null, input);
							recover(mse);
							throw mse;
						}

					}
						break;

					default:
						if (cnt15 >= 1)
							break loop15;
						if (state.backtracking > 0) {
							state.failed = true;
							return;
						}
						EarlyExitException eee = new EarlyExitException(15,
								input);
						throw eee;
					}
					cnt15++;
				} while (true);

			}

		} finally {
		}
	}

	// $ANTLR end "HEXDIGITS"

	// $ANTLR start "BIGDIGITS"
	public final void mBIGDIGITS() throws RecognitionException {
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:221:2:
			// ( ( DIGIT | 'A' .. 'Z' )+ )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:221:4:
			// ( DIGIT | 'A' .. 'Z' )+
			{
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:221:4:
				// ( DIGIT | 'A' .. 'Z' )+
				int cnt16 = 0;
				loop16: do {
					int alt16 = 2;
					int LA16_0 = input.LA(1);

					if (((LA16_0 >= '0' && LA16_0 <= '9') || (LA16_0 >= 'A' && LA16_0 <= 'Z'))) {
						alt16 = 1;
					}

					switch (alt16) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:
					{
						if ((input.LA(1) >= '0' && input.LA(1) <= '9')
								|| (input.LA(1) >= 'A' && input.LA(1) <= 'Z')) {
							input.consume();
							state.failed = false;
						} else {
							if (state.backtracking > 0) {
								state.failed = true;
								return;
							}
							MismatchedSetException mse = new MismatchedSetException(
									null, input);
							recover(mse);
							throw mse;
						}

					}
						break;

					default:
						if (cnt16 >= 1)
							break loop16;
						if (state.backtracking > 0) {
							state.failed = true;
							return;
						}
						EarlyExitException eee = new EarlyExitException(16,
								input);
						throw eee;
					}
					cnt16++;
				} while (true);

			}

		} finally {
		}
	}

	// $ANTLR end "BIGDIGITS"

	// $ANTLR start "DECIMAL"
	public final void mDECIMAL() throws RecognitionException {
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:224:2:
			// ( ( DOT DIGIT )=> ( DOT ( DIGIT )+ ) | )
			int alt18 = 2;
			int LA18_0 = input.LA(1);

			if ((LA18_0 == '.') && (synpred1_PonderTalk())) {
				alt18 = 1;
			} else {
				alt18 = 2;
			}
			switch (alt18) {
			case 1:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:224:4:
			// ( DOT DIGIT )=> ( DOT ( DIGIT )+ )
			{
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:224:19:
				// ( DOT ( DIGIT )+ )
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:224:20:
				// DOT ( DIGIT )+
				{
					mDOT();
					if (state.failed)
						return;
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:224:24:
					// ( DIGIT )+
					int cnt17 = 0;
					loop17: do {
						int alt17 = 2;
						int LA17_0 = input.LA(1);

						if (((LA17_0 >= '0' && LA17_0 <= '9'))) {
							alt17 = 1;
						}

						switch (alt17) {
						case 1:
						// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:224:24:
						// DIGIT
						{
							mDIGIT();
							if (state.failed)
								return;

						}
							break;

						default:
							if (cnt17 >= 1)
								break loop17;
							if (state.backtracking > 0) {
								state.failed = true;
								return;
							}
							EarlyExitException eee = new EarlyExitException(17,
									input);
							throw eee;
						}
						cnt17++;
					} while (true);

				}

			}
				break;
			case 2:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:225:2:
			{
			}
				break;

			}
		} finally {
		}
	}

	// $ANTLR end "DECIMAL"

	// $ANTLR start "INCLUDE"
	public final void mINCLUDE() throws RecognitionException {
		try {
			int _type = INCLUDE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			CommonToken f = null;

			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:226:9:
			// ( 'include' ( WS )? f= STRINGLITERAL )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:226:11:
			// 'include' ( WS )? f= STRINGLITERAL
			{
				match("include");
				if (state.failed)
					return;

				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:226:21:
				// ( WS )?
				int alt19 = 2;
				int LA19_0 = input.LA(1);

				if (((LA19_0 >= '\t' && LA19_0 <= '\n') || LA19_0 == '\r' || LA19_0 == ' ')) {
					alt19 = 1;
				}
				switch (alt19) {
				case 1:
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:226:22:
				// WS
				{
					mWS();
					if (state.failed)
						return;

				}
					break;

				}

				int fStart488 = getCharIndex();
				mSTRINGLITERAL();
				if (state.failed)
					return;
				f = new CommonToken(input, Token.INVALID_TOKEN_TYPE,
						Token.DEFAULT_CHANNEL, fStart488, getCharIndex() - 1);
				if (state.backtracking == 0) {

					String name = f.getText();
					name = name.substring(1, name.length() - 1);
					try {
						// save current lexer's state
						SaveStruct ss = new SaveStruct(input);
						includes.push(ss);

						// switch on new input stream
						// setCharStream(new ANTLRFileStream(name));
						InputStream is = Util.getInputStream(new URI(name));
						setCharStream(new ANTLRInputStream(is));
						reset();

					} catch (Exception fnf) {
						throw new Error("Cannot open file " + name);
					}

				}

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "INCLUDE"

	// $ANTLR start "COMMENT"
	public final void mCOMMENT() throws RecognitionException {
		try {
			int _type = COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:243:9:
			// ( '//' ( . )* ( '\\n' | '\\r' ) )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:243:11:
			// '//' ( . )* ( '\\n' | '\\r' )
			{
				match("//");
				if (state.failed)
					return;

				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:243:16:
				// ( . )*
				loop20: do {
					int alt20 = 2;
					int LA20_0 = input.LA(1);

					if ((LA20_0 == '\n' || LA20_0 == '\r')) {
						alt20 = 2;
					} else if (((LA20_0 >= '\u0000' && LA20_0 <= '\t')
							|| (LA20_0 >= '\u000B' && LA20_0 <= '\f') || (LA20_0 >= '\u000E' && LA20_0 <= '\uFFFF'))) {
						alt20 = 1;
					}

					switch (alt20) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:243:16:
					// .
					{
						matchAny();
						if (state.failed)
							return;

					}
						break;

					default:
						break loop20;
					}
				} while (true);

				if (input.LA(1) == '\n' || input.LA(1) == '\r') {
					input.consume();
					state.failed = false;
				} else {
					if (state.backtracking > 0) {
						state.failed = true;
						return;
					}
					MismatchedSetException mse = new MismatchedSetException(
							null, input);
					recover(mse);
					throw mse;
				}

				if (state.backtracking == 0) {
					_channel = HIDDEN;
				}

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "COMMENT"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:245:9:
			// ( ( ' ' | '\\t' | '\\r' '\\n' | '\\n' )+ )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:246:5:
			// ( ' ' | '\\t' | '\\r' '\\n' | '\\n' )+
			{
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:246:5:
				// ( ' ' | '\\t' | '\\r' '\\n' | '\\n' )+
				int cnt21 = 0;
				loop21: do {
					int alt21 = 5;
					switch (input.LA(1)) {
					case ' ': {
						alt21 = 1;
					}
						break;
					case '\t': {
						alt21 = 2;
					}
						break;
					case '\r': {
						alt21 = 3;
					}
						break;
					case '\n': {
						alt21 = 4;
					}
						break;

					}

					switch (alt21) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:246:6:
					// ' '
					{
						match(' ');
						if (state.failed)
							return;

					}
						break;
					case 2:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:247:7:
					// '\\t'
					{
						match('\t');
						if (state.failed)
							return;

					}
						break;
					case 3:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:248:7:
					// '\\r' '\\n'
					{
						match('\r');
						if (state.failed)
							return;
						match('\n');
						if (state.failed)
							return;

					}
						break;
					case 4:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:249:7:
					// '\\n'
					{
						match('\n');
						if (state.failed)
							return;

					}
						break;

					default:
						if (cnt21 >= 1)
							break loop21;
						if (state.backtracking > 0) {
							state.failed = true;
							return;
						}
						EarlyExitException eee = new EarlyExitException(21,
								input);
						throw eee;
					}
					cnt21++;
				} while (true);

				if (state.backtracking == 0) {
					_channel = HIDDEN;
				}

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
		}
	}

	// $ANTLR end "WS"

	public void mTokens() throws RecognitionException {
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:8:
		// ( T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | BINARYOP |
		// BOOLEANLITERAL | VARIABLEASSIGN | ASSIGN | DOT | BLOCKARG | KEYWORD |
		// PATHNAME | IDENTIFIER | CHARACTERLITERAL | STRINGLITERAL |
		// NUMBERLITERAL | PIPE | INCLUDE | COMMENT | WS )
		int alt22 = 22;
		alt22 = dfa22.predict(input);
		switch (alt22) {
		case 1:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:10:
		// T__47
		{
			mT__47();
			if (state.failed)
				return;

		}
			break;
		case 2:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:16:
		// T__48
		{
			mT__48();
			if (state.failed)
				return;

		}
			break;
		case 3:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:22:
		// T__49
		{
			mT__49();
			if (state.failed)
				return;

		}
			break;
		case 4:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:28:
		// T__50
		{
			mT__50();
			if (state.failed)
				return;

		}
			break;
		case 5:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:34:
		// T__51
		{
			mT__51();
			if (state.failed)
				return;

		}
			break;
		case 6:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:40:
		// T__52
		{
			mT__52();
			if (state.failed)
				return;

		}
			break;
		case 7:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:46:
		// BINARYOP
		{
			mBINARYOP();
			if (state.failed)
				return;

		}
			break;
		case 8:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:55:
		// BOOLEANLITERAL
		{
			mBOOLEANLITERAL();
			if (state.failed)
				return;

		}
			break;
		case 9:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:70:
		// VARIABLEASSIGN
		{
			mVARIABLEASSIGN();
			if (state.failed)
				return;

		}
			break;
		case 10:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:85:
		// ASSIGN
		{
			mASSIGN();
			if (state.failed)
				return;

		}
			break;
		case 11:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:92:
		// DOT
		{
			mDOT();
			if (state.failed)
				return;

		}
			break;
		case 12:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:96:
		// BLOCKARG
		{
			mBLOCKARG();
			if (state.failed)
				return;

		}
			break;
		case 13:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:105:
		// KEYWORD
		{
			mKEYWORD();
			if (state.failed)
				return;

		}
			break;
		case 14:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:113:
		// PATHNAME
		{
			mPATHNAME();
			if (state.failed)
				return;

		}
			break;
		case 15:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:122:
		// IDENTIFIER
		{
			mIDENTIFIER();
			if (state.failed)
				return;

		}
			break;
		case 16:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:133:
		// CHARACTERLITERAL
		{
			mCHARACTERLITERAL();
			if (state.failed)
				return;

		}
			break;
		case 17:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:150:
		// STRINGLITERAL
		{
			mSTRINGLITERAL();
			if (state.failed)
				return;

		}
			break;
		case 18:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:164:
		// NUMBERLITERAL
		{
			mNUMBERLITERAL();
			if (state.failed)
				return;

		}
			break;
		case 19:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:178:
		// PIPE
		{
			mPIPE();
			if (state.failed)
				return;

		}
			break;
		case 20:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:183:
		// INCLUDE
		{
			mINCLUDE();
			if (state.failed)
				return;

		}
			break;
		case 21:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:191:
		// COMMENT
		{
			mCOMMENT();
			if (state.failed)
				return;

		}
			break;
		case 22:
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:1:199:
		// WS
		{
			mWS();
			if (state.failed)
				return;

		}
			break;

		}

	}

	// $ANTLR start synpred1_PonderTalk
	public final void synpred1_PonderTalk_fragment()
			throws RecognitionException {
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:224:4:
		// ( DOT DIGIT )
		// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:224:5:
		// DOT DIGIT
		{
			mDOT();
			if (state.failed)
				return;
			mDIGIT();
			if (state.failed)
				return;

		}
	}

	// $ANTLR end synpred1_PonderTalk

	public final boolean synpred1_PonderTalk() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred1_PonderTalk_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: " + re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed = false;
		return success;
	}

	protected DFA14 dfa14 = new DFA14(this);
	protected DFA22 dfa22 = new DFA22(this);
	static final String DFA14_eotS = "\2\uffff\2\1\2\uffff";
	static final String DFA14_eofS = "\6\uffff";
	static final String DFA14_minS = "\1\55\1\uffff\2\60\2\uffff";
	static final String DFA14_maxS = "\1\71\1\uffff\1\170\1\162\2\uffff";
	static final String DFA14_acceptS = "\1\uffff\1\1\2\uffff\1\3\1\2";
	static final String DFA14_specialS = "\6\uffff}>";
	static final String[] DFA14_transitionS = { "\1\1\2\uffff\1\2\11\3", "",
			"\12\3\70\uffff\1\5\5\uffff\1\4", "\12\3\70\uffff\1\5", "", "" };

	static final short[] DFA14_eot = DFA.unpackEncodedString(DFA14_eotS);
	static final short[] DFA14_eof = DFA.unpackEncodedString(DFA14_eofS);
	static final char[] DFA14_min = DFA
			.unpackEncodedStringToUnsignedChars(DFA14_minS);
	static final char[] DFA14_max = DFA
			.unpackEncodedStringToUnsignedChars(DFA14_maxS);
	static final short[] DFA14_accept = DFA.unpackEncodedString(DFA14_acceptS);
	static final short[] DFA14_special = DFA
			.unpackEncodedString(DFA14_specialS);
	static final short[][] DFA14_transition;

	static {
		int numStates = DFA14_transitionS.length;
		DFA14_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA14_transition[i] = DFA.unpackEncodedString(DFA14_transitionS[i]);
		}
	}

	class DFA14 extends DFA {

		public DFA14(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 14;
			this.eot = DFA14_eot;
			this.eof = DFA14_eof;
			this.min = DFA14_min;
			this.max = DFA14_max;
			this.accept = DFA14_accept;
			this.special = DFA14_special;
			this.transition = DFA14_transition;
		}

		public String getDescription() {
			return "201:1: NUMBERLITERAL : ( ( '-' )? ( DIGIT )+ DECIMAL ( ( 'e' | 'E' ) ( '-' )? ( DIGIT )+ )? | ( DIGIT )+ 'r' ( '-' )? BIGDIGITS | '0x' d= HEXDIGITS );";
		}
	}

	static final String DFA22_eotS = "\7\uffff\1\23\3\26\4\uffff\1\23\2\uffff\1\26\2\uffff\1\26\1\uffff"
			+ "\1\26\1\uffff\1\41\2\26\2\uffff\1\23\1\26\2\uffff\2\26\1\uffff\1"
			+ "\23\1\51\2\26\1\uffff\1\51\3\26\1\uffff";
	static final String DFA22_eofS = "\57\uffff";
	static final String DFA22_minS = "\1\11\6\uffff\1\60\3\57\1\75\3\uffff\1\57\2\uffff\1\57\2\uffff\1"
			+ "\57\1\uffff\1\57\1\uffff\1\75\2\57\2\uffff\1\0\1\57\2\uffff\2\57"
			+ "\1\uffff\1\0\3\57\1\uffff\3\57\1\11\1\uffff";
	static final String DFA22_maxS = "\1\176\6\uffff\1\71\4\172\3\uffff\1\57\2\uffff\1\172\2\uffff\1\172"
			+ "\1\uffff\1\172\1\uffff\1\75\2\172\2\uffff\1\uffff\1\172\2\uffff"
			+ "\2\172\1\uffff\1\uffff\3\172\1\uffff\4\172\1\uffff";
	static final String DFA22_acceptS = "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\5\uffff\1\13\1\20\1\21\1\uffff"
			+ "\1\22\1\23\1\uffff\1\7\1\26\1\uffff\1\17\1\uffff\1\16\3\uffff\1"
			+ "\12\1\14\2\uffff\1\11\1\15\2\uffff\1\25\4\uffff\1\10\4\uffff\1\24";
	static final String DFA22_specialS = "\36\uffff\1\0\6\uffff\1\1\11\uffff}>";
	static final String[] DFA22_transitionS = {
			"\2\24\2\uffff\1\24\22\uffff\1\24\1\23\1\16\1\4\1\15\2\23\1\uffff"
					+ "\1\2\1\3\3\23\1\7\1\14\1\17\12\20\1\13\1\1\5\23\32\22\1\5\1"
					+ "\23\1\6\1\23\2\uffff\5\22\1\11\2\22\1\12\12\22\1\10\6\22\1\uffff"
					+ "\1\21\1\uffff\1\23",
			"",
			"",
			"",
			"",
			"",
			"",
			"\12\20",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\21\27\1"
					+ "\25\10\27",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\1\32\31"
					+ "\27",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\15\27\1"
					+ "\33\14\27",
			"\1\34\3\uffff\32\35\6\uffff\32\35",
			"",
			"",
			"",
			"\1\36",
			"",
			"",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\32\27",
			"",
			"",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\24\27\1"
					+ "\37\5\27",
			"",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\32\27",
			"",
			"\1\40",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\13\27\1"
					+ "\42\16\27",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\2\27\1"
					+ "\43\27\27",
			"",
			"",
			"\41\44\1\45\3\44\2\45\3\44\4\45\1\44\1\45\14\44\5\45\33\44"
					+ "\1\45\1\44\1\45\37\44\1\45\uff81\44",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\4\27\1"
					+ "\46\25\27",
			"",
			"",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\22\27\1"
					+ "\47\7\27",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\13\27\1"
					+ "\50\16\27",
			"",
			"\41\44\1\45\3\44\2\45\3\44\4\45\1\44\1\45\14\44\5\45\33\44"
					+ "\1\45\1\44\1\45\37\44\1\45\uff81\44",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\32\27",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\4\27\1"
					+ "\52\25\27",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\24\27\1"
					+ "\53\5\27",
			"",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\32\27",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\3\27\1"
					+ "\54\26\27",
			"\1\30\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\4\27\1"
					+ "\55\25\27",
			"\2\56\2\uffff\1\56\22\uffff\1\56\1\uffff\1\56\14\uffff\1\30"
					+ "\12\27\1\31\6\uffff\32\27\4\uffff\1\27\1\uffff\32\27",
			"" };

	static final short[] DFA22_eot = DFA.unpackEncodedString(DFA22_eotS);
	static final short[] DFA22_eof = DFA.unpackEncodedString(DFA22_eofS);
	static final char[] DFA22_min = DFA
			.unpackEncodedStringToUnsignedChars(DFA22_minS);
	static final char[] DFA22_max = DFA
			.unpackEncodedStringToUnsignedChars(DFA22_maxS);
	static final short[] DFA22_accept = DFA.unpackEncodedString(DFA22_acceptS);
	static final short[] DFA22_special = DFA
			.unpackEncodedString(DFA22_specialS);
	static final short[][] DFA22_transition;

	static {
		int numStates = DFA22_transitionS.length;
		DFA22_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA22_transition[i] = DFA.unpackEncodedString(DFA22_transitionS[i]);
		}
	}

	class DFA22 extends DFA {

		public DFA22(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 22;
			this.eot = DFA22_eot;
			this.eof = DFA22_eof;
			this.min = DFA22_min;
			this.max = DFA22_max;
			this.accept = DFA22_accept;
			this.special = DFA22_special;
			this.transition = DFA22_transition;
		}

		public String getDescription() {
			return "1:1: Tokens : ( T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | BINARYOP | BOOLEANLITERAL | VARIABLEASSIGN | ASSIGN | DOT | BLOCKARG | KEYWORD | PATHNAME | IDENTIFIER | CHARACTERLITERAL | STRINGLITERAL | NUMBERLITERAL | PIPE | INCLUDE | COMMENT | WS );";
		}

		public int specialStateTransition(int s, IntStream _input)
				throws NoViableAltException {
			IntStream input = _input;
			int _s = s;
			switch (s) {
			case 0:
				int LA22_30 = input.LA(1);

				s = -1;
				if (((LA22_30 >= '\u0000' && LA22_30 <= ' ')
						|| (LA22_30 >= '\"' && LA22_30 <= '$')
						|| (LA22_30 >= '\'' && LA22_30 <= ')')
						|| LA22_30 == '.' || (LA22_30 >= '0' && LA22_30 <= ';')
						|| (LA22_30 >= 'A' && LA22_30 <= '[') || LA22_30 == ']'
						|| (LA22_30 >= '_' && LA22_30 <= '}') || (LA22_30 >= '\u007F' && LA22_30 <= '\uFFFF'))) {
					s = 36;
				}

				else if ((LA22_30 == '!' || (LA22_30 >= '%' && LA22_30 <= '&')
						|| (LA22_30 >= '*' && LA22_30 <= '-') || LA22_30 == '/'
						|| (LA22_30 >= '<' && LA22_30 <= '@')
						|| LA22_30 == '\\' || LA22_30 == '^' || LA22_30 == '~')) {
					s = 37;
				}

				else
					s = 19;

				if (s >= 0)
					return s;
				break;
			case 1:
				int LA22_37 = input.LA(1);

				s = -1;
				if (((LA22_37 >= '\u0000' && LA22_37 <= ' ')
						|| (LA22_37 >= '\"' && LA22_37 <= '$')
						|| (LA22_37 >= '\'' && LA22_37 <= ')')
						|| LA22_37 == '.' || (LA22_37 >= '0' && LA22_37 <= ';')
						|| (LA22_37 >= 'A' && LA22_37 <= '[') || LA22_37 == ']'
						|| (LA22_37 >= '_' && LA22_37 <= '}') || (LA22_37 >= '\u007F' && LA22_37 <= '\uFFFF'))) {
					s = 36;
				}

				else if ((LA22_37 == '!' || (LA22_37 >= '%' && LA22_37 <= '&')
						|| (LA22_37 >= '*' && LA22_37 <= '-') || LA22_37 == '/'
						|| (LA22_37 >= '<' && LA22_37 <= '@')
						|| LA22_37 == '\\' || LA22_37 == '^' || LA22_37 == '~')) {
					s = 37;
				}

				else
					s = 19;

				if (s >= 0)
					return s;
				break;
			}
			if (state.backtracking > 0) {
				state.failed = true;
				return -1;
			}
			NoViableAltException nvae = new NoViableAltException(
					getDescription(), 22, _s, input);
			error(nvae);
			throw nvae;
		}
	}

}