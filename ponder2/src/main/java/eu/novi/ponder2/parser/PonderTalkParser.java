package eu.novi.ponder2.parser;

// $ANTLR 3.2 Sep 23, 2009 12:02:23 /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g 2009-12-09 16:00:39

import eu.novi.ponder2.parser.PonderTalkParser;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;

//import eu.novi.ponder2.parser.DFA1;
//import eu.novi.ponder2.parser.DFA10;
//import eu.novi.ponder2.parser.DFA2;
//import eu.novi.ponder2.parser.DFA4;
//import eu.novi.ponder2.parser.DFA8;

public class PonderTalkParser extends Parser {
	public static final String[] tokenNames = new String[] { "<invalid>",
			"<EOR>", "<DOWN>", "<UP>", "TEMP", "ARRAY", "BLOCK", "BARGS",
			"BCODE", "BSTATEMENTS", "EXPRESSION", "OBJECT", "MESSAGE",
			"PARENS", "CASCADE", "UNARYMSG", "BINARYMSG", "KEYWORDMSG",
			"KEYWORDARG", "IMMEDIATE", "LITERAL", "STRING", "NUMBER", "CHAR",
			"BOOLEAN", "DOT", "IDENTIFIER", "ASSIGN", "VARIABLEASSIGN",
			"BLOCKARG", "PIPE", "BOOLEANLITERAL", "CHARACTERLITERAL",
			"NUMBERLITERAL", "STRINGLITERAL", "KEYWORD", "PATHNAME",
			"BINARYOP", "BINARYCHAR", "LETTER", "DIGIT", "DECIMAL",
			"BIGDIGITS", "HEXDIGITS", "WS", "INCLUDE", "COMMENT", "';'", "'('",
			"')'", "'#('", "'['", "']'" };
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

	// delegates
	// delegators

	public PonderTalkParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}

	public PonderTalkParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);

	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}

	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}

	public String[] getTokenNames() {
		return PonderTalkParser.tokenNames;
	}

	public String getGrammarFileName() {
		return "/Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g";
	}

	public static class start_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "start"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:107:1:
	// start : ( sentences )? EOF ;
	public final PonderTalkParser.start_return start()
			throws RecognitionException {
		PonderTalkParser.start_return retval = new PonderTalkParser.start_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token EOF2 = null;
		PonderTalkParser.sentences_return sentences1 = null;

		Object EOF2_tree = null;

		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:107:7:
			// ( ( sentences )? EOF )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:107:9:
			// ( sentences )? EOF
			{
				root_0 = (Object) adaptor.nil();

				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:107:9:
				// ( sentences )?
				int alt1 = 2;
				alt1 = dfa1.predict(input);
				switch (alt1) {
				case 1:
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:107:9:
				// sentences
				{
					pushFollow(FOLLOW_sentences_in_start286);
					sentences1 = sentences();

					state._fsp--;

					adaptor.addChild(root_0, sentences1.getTree());

				}
					break;

				}

				EOF2 = (Token) match(input, EOF, FOLLOW_EOF_in_start289);
				EOF2_tree = (Object) adaptor.create(EOF2);
				adaptor.addChild(root_0, EOF2_tree);

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "start"

	public static class sentences_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "sentences"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:109:1:
	// sentences : sentence ( DOT sentence )* ( DOT )? -> ( sentence )+ ;
	public final PonderTalkParser.sentences_return sentences()
			throws RecognitionException {
		PonderTalkParser.sentences_return retval = new PonderTalkParser.sentences_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token DOT4 = null;
		Token DOT6 = null;
		PonderTalkParser.sentence_return sentence3 = null;

		PonderTalkParser.sentence_return sentence5 = null;

		Object DOT4_tree = null;
		Object DOT6_tree = null;
		RewriteRuleTokenStream stream_DOT = new RewriteRuleTokenStream(adaptor,
				"token DOT");
		RewriteRuleSubtreeStream stream_sentence = new RewriteRuleSubtreeStream(
				adaptor, "rule sentence");
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:110:2:
			// ( sentence ( DOT sentence )* ( DOT )? -> ( sentence )+ )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:110:4:
			// sentence ( DOT sentence )* ( DOT )?
			{
				pushFollow(FOLLOW_sentence_in_sentences299);
				sentence3 = sentence();

				state._fsp--;

				stream_sentence.add(sentence3.getTree());
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:110:13:
				// ( DOT sentence )*
				loop2: do {
					int alt2 = 2;
					alt2 = dfa2.predict(input);
					switch (alt2) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:110:14:
					// DOT sentence
					{
						DOT4 = (Token) match(input, DOT,
								FOLLOW_DOT_in_sentences302);
						stream_DOT.add(DOT4);

						pushFollow(FOLLOW_sentence_in_sentences304);
						sentence5 = sentence();

						state._fsp--;

						stream_sentence.add(sentence5.getTree());

					}
						break;

					default:
						break loop2;
					}
				} while (true);

				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:110:29:
				// ( DOT )?
				int alt3 = 2;
				int LA3_0 = input.LA(1);

				if ((LA3_0 == DOT)) {
					alt3 = 1;
				}
				switch (alt3) {
				case 1:
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:110:29:
				// DOT
				{
					DOT6 = (Token) match(input, DOT, FOLLOW_DOT_in_sentences308);
					stream_DOT.add(DOT6);

				}
					break;

				}

				// AST REWRITE
				// elements: sentence
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 111:3: -> ( sentence )+
				{
					if (!(stream_sentence.hasNext())) {
						throw new RewriteEarlyExitException();
					}
					while (stream_sentence.hasNext()) {
						adaptor.addChild(root_0, stream_sentence.nextTree());

					}
					stream_sentence.reset();

				}

				retval.tree = root_0;
			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "sentences"

	public static class sentence_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "sentence"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:115:1:
	// sentence : ( objectMessage | IDENTIFIER ASSIGN sentence -> ^( ASSIGN
	// IDENTIFIER sentence ) | VARIABLEASSIGN sentence -> ^( ASSIGN
	// VARIABLEASSIGN sentence ) );
	public final PonderTalkParser.sentence_return sentence()
			throws RecognitionException {
		PonderTalkParser.sentence_return retval = new PonderTalkParser.sentence_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token IDENTIFIER8 = null;
		Token ASSIGN9 = null;
		Token VARIABLEASSIGN11 = null;
		PonderTalkParser.objectMessage_return objectMessage7 = null;

		PonderTalkParser.sentence_return sentence10 = null;

		PonderTalkParser.sentence_return sentence12 = null;

		Object IDENTIFIER8_tree = null;
		Object ASSIGN9_tree = null;
		Object VARIABLEASSIGN11_tree = null;
		RewriteRuleTokenStream stream_VARIABLEASSIGN = new RewriteRuleTokenStream(
				adaptor, "token VARIABLEASSIGN");
		RewriteRuleTokenStream stream_IDENTIFIER = new RewriteRuleTokenStream(
				adaptor, "token IDENTIFIER");
		RewriteRuleTokenStream stream_ASSIGN = new RewriteRuleTokenStream(
				adaptor, "token ASSIGN");
		RewriteRuleSubtreeStream stream_sentence = new RewriteRuleSubtreeStream(
				adaptor, "rule sentence");
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:115:9:
			// ( objectMessage | IDENTIFIER ASSIGN sentence -> ^( ASSIGN
			// IDENTIFIER sentence ) | VARIABLEASSIGN sentence -> ^( ASSIGN
			// VARIABLEASSIGN sentence ) )
			int alt4 = 3;
			alt4 = dfa4.predict(input);
			switch (alt4) {
			case 1:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:115:11:
			// objectMessage
			{
				root_0 = (Object) adaptor.nil();

				pushFollow(FOLLOW_objectMessage_in_sentence326);
				objectMessage7 = objectMessage();

				state._fsp--;

				adaptor.addChild(root_0, objectMessage7.getTree());

			}
				break;
			case 2:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:116:4:
			// IDENTIFIER ASSIGN sentence
			{
				IDENTIFIER8 = (Token) match(input, IDENTIFIER,
						FOLLOW_IDENTIFIER_in_sentence331);
				stream_IDENTIFIER.add(IDENTIFIER8);

				ASSIGN9 = (Token) match(input, ASSIGN,
						FOLLOW_ASSIGN_in_sentence333);
				stream_ASSIGN.add(ASSIGN9);

				pushFollow(FOLLOW_sentence_in_sentence335);
				sentence10 = sentence();

				state._fsp--;

				stream_sentence.add(sentence10.getTree());

				// AST REWRITE
				// elements: sentence, IDENTIFIER, ASSIGN
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 116:31: -> ^( ASSIGN IDENTIFIER sentence )
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:116:34:
					// ^( ASSIGN IDENTIFIER sentence )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor.becomeRoot(
								stream_ASSIGN.nextNode(), root_1);

						adaptor.addChild(root_1, stream_IDENTIFIER.nextNode());
						adaptor.addChild(root_1, stream_sentence.nextTree());

						adaptor.addChild(root_0, root_1);
					}

				}

				retval.tree = root_0;
			}
				break;
			case 3:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:117:4:
			// VARIABLEASSIGN sentence
			{
				VARIABLEASSIGN11 = (Token) match(input, VARIABLEASSIGN,
						FOLLOW_VARIABLEASSIGN_in_sentence350);
				stream_VARIABLEASSIGN.add(VARIABLEASSIGN11);

				pushFollow(FOLLOW_sentence_in_sentence352);
				sentence12 = sentence();

				state._fsp--;

				stream_sentence.add(sentence12.getTree());

				// AST REWRITE
				// elements: VARIABLEASSIGN, sentence
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 117:28: -> ^( ASSIGN VARIABLEASSIGN sentence )
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:117:31:
					// ^( ASSIGN VARIABLEASSIGN sentence )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor.becomeRoot(
								(Object) adaptor.create(ASSIGN, "ASSIGN"),
								root_1);

						adaptor.addChild(root_1,
								stream_VARIABLEASSIGN.nextNode());
						adaptor.addChild(root_1, stream_sentence.nextTree());

						adaptor.addChild(root_0, root_1);
					}

				}

				retval.tree = root_0;
			}
				break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "sentence"

	public static class objectMessage_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "objectMessage"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:119:1:
	// objectMessage : object ( messageExpression ( ';' messageExpression )* )?
	// -> ^( EXPRESSION object ( messageExpression ( ^( CASCADE
	// messageExpression ) )* )? ) ;
	public final PonderTalkParser.objectMessage_return objectMessage()
			throws RecognitionException {
		PonderTalkParser.objectMessage_return retval = new PonderTalkParser.objectMessage_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal15 = null;
		PonderTalkParser.object_return object13 = null;

		PonderTalkParser.messageExpression_return messageExpression14 = null;

		PonderTalkParser.messageExpression_return messageExpression16 = null;

		Object char_literal15_tree = null;
		RewriteRuleTokenStream stream_47 = new RewriteRuleTokenStream(adaptor,
				"token 47");
		RewriteRuleSubtreeStream stream_object = new RewriteRuleSubtreeStream(
				adaptor, "rule object");
		RewriteRuleSubtreeStream stream_messageExpression = new RewriteRuleSubtreeStream(
				adaptor, "rule messageExpression");
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:120:2:
			// ( object ( messageExpression ( ';' messageExpression )* )? -> ^(
			// EXPRESSION object ( messageExpression ( ^( CASCADE
			// messageExpression ) )* )? ) )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:120:4:
			// object ( messageExpression ( ';' messageExpression )* )?
			{
				pushFollow(FOLLOW_object_in_objectMessage372);
				object13 = object();

				state._fsp--;

				stream_object.add(object13.getTree());
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:120:11:
				// ( messageExpression ( ';' messageExpression )* )?
				int alt6 = 2;
				int LA6_0 = input.LA(1);

				if ((LA6_0 == IDENTIFIER || LA6_0 == PIPE || LA6_0 == KEYWORD || LA6_0 == BINARYOP)) {
					alt6 = 1;
				}
				switch (alt6) {
				case 1:
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:120:12:
				// messageExpression ( ';' messageExpression )*
				{
					pushFollow(FOLLOW_messageExpression_in_objectMessage375);
					messageExpression14 = messageExpression();

					state._fsp--;

					stream_messageExpression.add(messageExpression14.getTree());
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:120:30:
					// ( ';' messageExpression )*
					loop5: do {
						int alt5 = 2;
						int LA5_0 = input.LA(1);

						if ((LA5_0 == 47)) {
							alt5 = 1;
						}

						switch (alt5) {
						case 1:
						// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:120:32:
						// ';' messageExpression
						{
							char_literal15 = (Token) match(input, 47,
									FOLLOW_47_in_objectMessage379);
							stream_47.add(char_literal15);

							pushFollow(FOLLOW_messageExpression_in_objectMessage381);
							messageExpression16 = messageExpression();

							state._fsp--;

							stream_messageExpression.add(messageExpression16
									.getTree());

						}
							break;

						default:
							break loop5;
						}
					} while (true);

				}
					break;

				}

				// AST REWRITE
				// elements: object, messageExpression, messageExpression
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 121:3: -> ^( EXPRESSION object ( messageExpression ( ^(
				// CASCADE messageExpression ) )* )? )
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:121:6:
					// ^( EXPRESSION object ( messageExpression ( ^( CASCADE
					// messageExpression ) )* )? )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor.becomeRoot((Object) adaptor
								.create(EXPRESSION, "EXPRESSION"), root_1);

						adaptor.addChild(root_1, stream_object.nextTree());
						// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:121:26:
						// ( messageExpression ( ^( CASCADE messageExpression )
						// )* )?
						if (stream_messageExpression.hasNext()
								|| stream_messageExpression.hasNext()) {
							adaptor.addChild(root_1,
									stream_messageExpression.nextTree());
							// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:121:46:
							// ( ^( CASCADE messageExpression ) )*
							while (stream_messageExpression.hasNext()) {
								// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:121:47:
								// ^( CASCADE messageExpression )
								{
									Object root_2 = (Object) adaptor.nil();
									root_2 = (Object) adaptor.becomeRoot(
											(Object) adaptor.create(CASCADE,
													"CASCADE"), root_2);

									adaptor.addChild(root_2,
											stream_messageExpression.nextTree());

									adaptor.addChild(root_1, root_2);
								}

							}
							stream_messageExpression.reset();

						}
						stream_messageExpression.reset();
						stream_messageExpression.reset();

						adaptor.addChild(root_0, root_1);
					}

				}

				retval.tree = root_0;
			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "objectMessage"

	public static class object_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "object"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:123:1:
	// object : ( array | block | literal -> ^( LITERAL literal ) | pathname |
	// '(' sentence ')' -> sentence );
	public final PonderTalkParser.object_return object()
			throws RecognitionException {
		PonderTalkParser.object_return retval = new PonderTalkParser.object_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal21 = null;
		Token char_literal23 = null;
		PonderTalkParser.array_return array17 = null;

		PonderTalkParser.block_return block18 = null;

		PonderTalkParser.literal_return literal19 = null;

		PonderTalkParser.pathname_return pathname20 = null;

		PonderTalkParser.sentence_return sentence22 = null;

		Object char_literal21_tree = null;
		Object char_literal23_tree = null;
		RewriteRuleTokenStream stream_48 = new RewriteRuleTokenStream(adaptor,
				"token 48");
		RewriteRuleTokenStream stream_49 = new RewriteRuleTokenStream(adaptor,
				"token 49");
		RewriteRuleSubtreeStream stream_sentence = new RewriteRuleSubtreeStream(
				adaptor, "rule sentence");
		RewriteRuleSubtreeStream stream_literal = new RewriteRuleSubtreeStream(
				adaptor, "rule literal");
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:123:8:
			// ( array | block | literal -> ^( LITERAL literal ) | pathname |
			// '(' sentence ')' -> sentence )
			int alt7 = 5;
			switch (input.LA(1)) {
			case 50: {
				alt7 = 1;
			}
				break;
			case 51: {
				alt7 = 2;
			}
				break;
			case BOOLEANLITERAL:
			case CHARACTERLITERAL:
			case NUMBERLITERAL:
			case STRINGLITERAL: {
				alt7 = 3;
			}
				break;
			case IDENTIFIER:
			case PATHNAME: {
				alt7 = 4;
			}
				break;
			case 48: {
				alt7 = 5;
			}
				break;
			default:
				NoViableAltException nvae = new NoViableAltException("", 7, 0,
						input);

				throw nvae;
			}

			switch (alt7) {
			case 1:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:123:10:
			// array
			{
				root_0 = (Object) adaptor.nil();

				pushFollow(FOLLOW_array_in_object421);
				array17 = array();

				state._fsp--;

				adaptor.addChild(root_0, array17.getTree());

			}
				break;
			case 2:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:124:4:
			// block
			{
				root_0 = (Object) adaptor.nil();

				pushFollow(FOLLOW_block_in_object426);
				block18 = block();

				state._fsp--;

				adaptor.addChild(root_0, block18.getTree());

			}
				break;
			case 3:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:125:4:
			// literal
			{
				pushFollow(FOLLOW_literal_in_object431);
				literal19 = literal();

				state._fsp--;

				stream_literal.add(literal19.getTree());

				// AST REWRITE
				// elements: literal
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 125:12: -> ^( LITERAL literal )
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:125:15:
					// ^( LITERAL literal )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor.becomeRoot(
								(Object) adaptor.create(LITERAL, "LITERAL"),
								root_1);

						adaptor.addChild(root_1, stream_literal.nextTree());

						adaptor.addChild(root_0, root_1);
					}

				}

				retval.tree = root_0;
			}
				break;
			case 4:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:126:4:
			// pathname
			{
				root_0 = (Object) adaptor.nil();

				pushFollow(FOLLOW_pathname_in_object444);
				pathname20 = pathname();

				state._fsp--;

				adaptor.addChild(root_0, pathname20.getTree());

			}
				break;
			case 5:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:127:4:
			// '(' sentence ')'
			{
				char_literal21 = (Token) match(input, 48,
						FOLLOW_48_in_object449);
				stream_48.add(char_literal21);

				pushFollow(FOLLOW_sentence_in_object451);
				sentence22 = sentence();

				state._fsp--;

				stream_sentence.add(sentence22.getTree());
				char_literal23 = (Token) match(input, 49,
						FOLLOW_49_in_object453);
				stream_49.add(char_literal23);

				// AST REWRITE
				// elements: sentence
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 127:21: -> sentence
				{
					adaptor.addChild(root_0, stream_sentence.nextTree());

				}

				retval.tree = root_0;
			}
				break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "object"

	public static class array_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "array"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:129:1:
	// array : '#(' ( object )* ')' -> ^( ARRAY ( ^( EXPRESSION object ) )* ) ;
	public final PonderTalkParser.array_return array()
			throws RecognitionException {
		PonderTalkParser.array_return retval = new PonderTalkParser.array_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal24 = null;
		Token char_literal26 = null;
		PonderTalkParser.object_return object25 = null;

		Object string_literal24_tree = null;
		Object char_literal26_tree = null;
		RewriteRuleTokenStream stream_50 = new RewriteRuleTokenStream(adaptor,
				"token 50");
		RewriteRuleTokenStream stream_49 = new RewriteRuleTokenStream(adaptor,
				"token 49");
		RewriteRuleSubtreeStream stream_object = new RewriteRuleSubtreeStream(
				adaptor, "rule object");
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:129:8:
			// ( '#(' ( object )* ')' -> ^( ARRAY ( ^( EXPRESSION object ) )* )
			// )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:129:11:
			// '#(' ( object )* ')'
			{
				string_literal24 = (Token) match(input, 50,
						FOLLOW_50_in_array468);
				stream_50.add(string_literal24);

				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:129:16:
				// ( object )*
				loop8: do {
					int alt8 = 2;
					alt8 = dfa8.predict(input);
					switch (alt8) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:129:16:
					// object
					{
						pushFollow(FOLLOW_object_in_array470);
						object25 = object();

						state._fsp--;

						stream_object.add(object25.getTree());

					}
						break;

					default:
						break loop8;
					}
				} while (true);

				char_literal26 = (Token) match(input, 49, FOLLOW_49_in_array473);
				stream_49.add(char_literal26);

				// AST REWRITE
				// elements: object
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 130:5: -> ^( ARRAY ( ^( EXPRESSION object ) )* )
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:130:8:
					// ^( ARRAY ( ^( EXPRESSION object ) )* )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor
								.becomeRoot(
										(Object) adaptor.create(ARRAY, "ARRAY"),
										root_1);

						// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:130:16:
						// ( ^( EXPRESSION object ) )*
						while (stream_object.hasNext()) {
							// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:130:16:
							// ^( EXPRESSION object )
							{
								Object root_2 = (Object) adaptor.nil();
								root_2 = (Object) adaptor.becomeRoot(
										(Object) adaptor.create(EXPRESSION,
												"EXPRESSION"), root_2);

								adaptor.addChild(root_2,
										stream_object.nextTree());

								adaptor.addChild(root_1, root_2);
							}

						}
						stream_object.reset();

						adaptor.addChild(root_0, root_1);
					}

				}

				retval.tree = root_0;
			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "array"

	public static class block_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "block"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:132:1:
	// block : '[' ( ( BLOCKARG )+ PIPE )? sentences ']' -> ^( BLOCK ^( BARGS (
	// BLOCKARG )* ) ^( BCODE sentences ) ) ;
	public final PonderTalkParser.block_return block()
			throws RecognitionException {
		PonderTalkParser.block_return retval = new PonderTalkParser.block_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal27 = null;
		Token BLOCKARG28 = null;
		Token PIPE29 = null;
		Token char_literal31 = null;
		PonderTalkParser.sentences_return sentences30 = null;

		Object char_literal27_tree = null;
		Object BLOCKARG28_tree = null;
		Object PIPE29_tree = null;
		Object char_literal31_tree = null;
		RewriteRuleTokenStream stream_BLOCKARG = new RewriteRuleTokenStream(
				adaptor, "token BLOCKARG");
		RewriteRuleTokenStream stream_52 = new RewriteRuleTokenStream(adaptor,
				"token 52");
		RewriteRuleTokenStream stream_PIPE = new RewriteRuleTokenStream(
				adaptor, "token PIPE");
		RewriteRuleTokenStream stream_51 = new RewriteRuleTokenStream(adaptor,
				"token 51");
		RewriteRuleSubtreeStream stream_sentences = new RewriteRuleSubtreeStream(
				adaptor, "rule sentences");
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:132:8:
			// ( '[' ( ( BLOCKARG )+ PIPE )? sentences ']' -> ^( BLOCK ^( BARGS
			// ( BLOCKARG )* ) ^( BCODE sentences ) ) )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:132:10:
			// '[' ( ( BLOCKARG )+ PIPE )? sentences ']'
			{
				char_literal27 = (Token) match(input, 51, FOLLOW_51_in_block501);
				stream_51.add(char_literal27);

				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:132:14:
				// ( ( BLOCKARG )+ PIPE )?
				int alt10 = 2;
				alt10 = dfa10.predict(input);
				switch (alt10) {
				case 1:
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:132:16:
				// ( BLOCKARG )+ PIPE
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:132:16:
					// ( BLOCKARG )+
					int cnt9 = 0;
					loop9: do {
						int alt9 = 2;
						int LA9_0 = input.LA(1);

						if ((LA9_0 == BLOCKARG)) {
							alt9 = 1;
						}

						switch (alt9) {
						case 1:
						// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:132:16:
						// BLOCKARG
						{
							BLOCKARG28 = (Token) match(input, BLOCKARG,
									FOLLOW_BLOCKARG_in_block505);
							stream_BLOCKARG.add(BLOCKARG28);

						}
							break;

						default:
							if (cnt9 >= 1)
								break loop9;
							EarlyExitException eee = new EarlyExitException(9,
									input);
							throw eee;
						}
						cnt9++;
					} while (true);

					PIPE29 = (Token) match(input, PIPE, FOLLOW_PIPE_in_block508);
					stream_PIPE.add(PIPE29);

				}
					break;

				}

				pushFollow(FOLLOW_sentences_in_block513);
				sentences30 = sentences();

				state._fsp--;

				stream_sentences.add(sentences30.getTree());
				char_literal31 = (Token) match(input, 52, FOLLOW_52_in_block515);
				stream_52.add(char_literal31);

				// AST REWRITE
				// elements: BLOCKARG, sentences
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 133:5: -> ^( BLOCK ^( BARGS ( BLOCKARG )* ) ^( BCODE
				// sentences ) )
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:133:8:
					// ^( BLOCK ^( BARGS ( BLOCKARG )* ) ^( BCODE sentences ) )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor
								.becomeRoot(
										(Object) adaptor.create(BLOCK, "BLOCK"),
										root_1);

						// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:133:16:
						// ^( BARGS ( BLOCKARG )* )
						{
							Object root_2 = (Object) adaptor.nil();
							root_2 = (Object) adaptor.becomeRoot(
									(Object) adaptor.create(BARGS, "BARGS"),
									root_2);

							// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:133:24:
							// ( BLOCKARG )*
							while (stream_BLOCKARG.hasNext()) {
								adaptor.addChild(root_2,
										stream_BLOCKARG.nextNode());

							}
							stream_BLOCKARG.reset();

							adaptor.addChild(root_1, root_2);
						}
						// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:133:35:
						// ^( BCODE sentences )
						{
							Object root_2 = (Object) adaptor.nil();
							root_2 = (Object) adaptor.becomeRoot(
									(Object) adaptor.create(BCODE, "BCODE"),
									root_2);

							adaptor.addChild(root_2,
									stream_sentences.nextTree());

							adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_0, root_1);
					}

				}

				retval.tree = root_0;
			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "block"

	public static class literal_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "literal"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:135:1:
	// literal : ( BOOLEANLITERAL -> ^( BOOLEAN BOOLEANLITERAL ) |
	// CHARACTERLITERAL -> ^( CHAR CHARACTERLITERAL ) | NUMBERLITERAL -> ^(
	// NUMBER NUMBERLITERAL ) | STRINGLITERAL -> ^( STRING STRINGLITERAL ) );
	public final PonderTalkParser.literal_return literal()
			throws RecognitionException {
		PonderTalkParser.literal_return retval = new PonderTalkParser.literal_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token BOOLEANLITERAL32 = null;
		Token CHARACTERLITERAL33 = null;
		Token NUMBERLITERAL34 = null;
		Token STRINGLITERAL35 = null;

		Object BOOLEANLITERAL32_tree = null;
		Object CHARACTERLITERAL33_tree = null;
		Object NUMBERLITERAL34_tree = null;
		Object STRINGLITERAL35_tree = null;
		RewriteRuleTokenStream stream_STRINGLITERAL = new RewriteRuleTokenStream(
				adaptor, "token STRINGLITERAL");
		RewriteRuleTokenStream stream_NUMBERLITERAL = new RewriteRuleTokenStream(
				adaptor, "token NUMBERLITERAL");
		RewriteRuleTokenStream stream_CHARACTERLITERAL = new RewriteRuleTokenStream(
				adaptor, "token CHARACTERLITERAL");
		RewriteRuleTokenStream stream_BOOLEANLITERAL = new RewriteRuleTokenStream(
				adaptor, "token BOOLEANLITERAL");

		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:135:9:
			// ( BOOLEANLITERAL -> ^( BOOLEAN BOOLEANLITERAL ) |
			// CHARACTERLITERAL -> ^( CHAR CHARACTERLITERAL ) | NUMBERLITERAL ->
			// ^( NUMBER NUMBERLITERAL ) | STRINGLITERAL -> ^( STRING
			// STRINGLITERAL ) )
			int alt11 = 4;
			switch (input.LA(1)) {
			case BOOLEANLITERAL: {
				alt11 = 1;
			}
				break;
			case CHARACTERLITERAL: {
				alt11 = 2;
			}
				break;
			case NUMBERLITERAL: {
				alt11 = 3;
			}
				break;
			case STRINGLITERAL: {
				alt11 = 4;
			}
				break;
			default:
				NoViableAltException nvae = new NoViableAltException("", 11, 0,
						input);

				throw nvae;
			}

			switch (alt11) {
			case 1:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:136:5:
			// BOOLEANLITERAL
			{
				BOOLEANLITERAL32 = (Token) match(input, BOOLEANLITERAL,
						FOLLOW_BOOLEANLITERAL_in_literal551);
				stream_BOOLEANLITERAL.add(BOOLEANLITERAL32);

				// AST REWRITE
				// elements: BOOLEANLITERAL
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 136:20: -> ^( BOOLEAN BOOLEANLITERAL )
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:136:23:
					// ^( BOOLEAN BOOLEANLITERAL )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor.becomeRoot(
								(Object) adaptor.create(BOOLEAN, "BOOLEAN"),
								root_1);

						adaptor.addChild(root_1,
								stream_BOOLEANLITERAL.nextNode());

						adaptor.addChild(root_0, root_1);
					}

				}

				retval.tree = root_0;
			}
				break;
			case 2:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:137:5:
			// CHARACTERLITERAL
			{
				CHARACTERLITERAL33 = (Token) match(input, CHARACTERLITERAL,
						FOLLOW_CHARACTERLITERAL_in_literal565);
				stream_CHARACTERLITERAL.add(CHARACTERLITERAL33);

				// AST REWRITE
				// elements: CHARACTERLITERAL
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 137:22: -> ^( CHAR CHARACTERLITERAL )
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:137:25:
					// ^( CHAR CHARACTERLITERAL )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor.becomeRoot(
								(Object) adaptor.create(CHAR, "CHAR"), root_1);

						adaptor.addChild(root_1,
								stream_CHARACTERLITERAL.nextNode());

						adaptor.addChild(root_0, root_1);
					}

				}

				retval.tree = root_0;
			}
				break;
			case 3:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:138:5:
			// NUMBERLITERAL
			{
				NUMBERLITERAL34 = (Token) match(input, NUMBERLITERAL,
						FOLLOW_NUMBERLITERAL_in_literal579);
				stream_NUMBERLITERAL.add(NUMBERLITERAL34);

				// AST REWRITE
				// elements: NUMBERLITERAL
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 138:19: -> ^( NUMBER NUMBERLITERAL )
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:138:22:
					// ^( NUMBER NUMBERLITERAL )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor.becomeRoot(
								(Object) adaptor.create(NUMBER, "NUMBER"),
								root_1);

						adaptor.addChild(root_1,
								stream_NUMBERLITERAL.nextNode());

						adaptor.addChild(root_0, root_1);
					}

				}

				retval.tree = root_0;
			}
				break;
			case 4:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:139:5:
			// STRINGLITERAL
			{
				STRINGLITERAL35 = (Token) match(input, STRINGLITERAL,
						FOLLOW_STRINGLITERAL_in_literal593);
				stream_STRINGLITERAL.add(STRINGLITERAL35);

				// AST REWRITE
				// elements: STRINGLITERAL
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 139:19: -> ^( STRING STRINGLITERAL )
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:139:22:
					// ^( STRING STRINGLITERAL )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor.becomeRoot(
								(Object) adaptor.create(STRING, "STRING"),
								root_1);

						adaptor.addChild(root_1,
								stream_STRINGLITERAL.nextNode());

						adaptor.addChild(root_0, root_1);
					}

				}

				retval.tree = root_0;
			}
				break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "literal"

	public static class messageExpression_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "messageExpression"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:141:1:
	// messageExpression : ( unaryExpression | binaryExpression |
	// keywordExpression );
	public final PonderTalkParser.messageExpression_return messageExpression()
			throws RecognitionException {
		PonderTalkParser.messageExpression_return retval = new PonderTalkParser.messageExpression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		PonderTalkParser.unaryExpression_return unaryExpression36 = null;

		PonderTalkParser.binaryExpression_return binaryExpression37 = null;

		PonderTalkParser.keywordExpression_return keywordExpression38 = null;

		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:142:2:
			// ( unaryExpression | binaryExpression | keywordExpression )
			int alt12 = 3;
			switch (input.LA(1)) {
			case IDENTIFIER: {
				alt12 = 1;
			}
				break;
			case PIPE:
			case BINARYOP: {
				alt12 = 2;
			}
				break;
			case KEYWORD: {
				alt12 = 3;
			}
				break;
			default:
				NoViableAltException nvae = new NoViableAltException("", 12, 0,
						input);

				throw nvae;
			}

			switch (alt12) {
			case 1:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:142:4:
			// unaryExpression
			{
				root_0 = (Object) adaptor.nil();

				pushFollow(FOLLOW_unaryExpression_in_messageExpression611);
				unaryExpression36 = unaryExpression();

				state._fsp--;

				adaptor.addChild(root_0, unaryExpression36.getTree());

			}
				break;
			case 2:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:142:22:
			// binaryExpression
			{
				root_0 = (Object) adaptor.nil();

				pushFollow(FOLLOW_binaryExpression_in_messageExpression615);
				binaryExpression37 = binaryExpression();

				state._fsp--;

				adaptor.addChild(root_0, binaryExpression37.getTree());

			}
				break;
			case 3:
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:142:41:
			// keywordExpression
			{
				root_0 = (Object) adaptor.nil();

				pushFollow(FOLLOW_keywordExpression_in_messageExpression619);
				keywordExpression38 = keywordExpression();

				state._fsp--;

				adaptor.addChild(root_0, keywordExpression38.getTree());

			}
				break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "messageExpression"

	public static class unaryExpression_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "unaryExpression"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:144:1:
	// unaryExpression : ( unaryMessage )+ ( binaryExpression |
	// keywordExpression )? ;
	public final PonderTalkParser.unaryExpression_return unaryExpression()
			throws RecognitionException {
		PonderTalkParser.unaryExpression_return retval = new PonderTalkParser.unaryExpression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		PonderTalkParser.unaryMessage_return unaryMessage39 = null;

		PonderTalkParser.binaryExpression_return binaryExpression40 = null;

		PonderTalkParser.keywordExpression_return keywordExpression41 = null;

		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:145:2:
			// ( ( unaryMessage )+ ( binaryExpression | keywordExpression )? )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:145:4:
			// ( unaryMessage )+ ( binaryExpression | keywordExpression )?
			{
				root_0 = (Object) adaptor.nil();

				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:145:4:
				// ( unaryMessage )+
				int cnt13 = 0;
				loop13: do {
					int alt13 = 2;
					int LA13_0 = input.LA(1);

					if ((LA13_0 == IDENTIFIER)) {
						alt13 = 1;
					}

					switch (alt13) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:145:4:
					// unaryMessage
					{
						pushFollow(FOLLOW_unaryMessage_in_unaryExpression629);
						unaryMessage39 = unaryMessage();

						state._fsp--;

						adaptor.addChild(root_0, unaryMessage39.getTree());

					}
						break;

					default:
						if (cnt13 >= 1)
							break loop13;
						EarlyExitException eee = new EarlyExitException(13,
								input);
						throw eee;
					}
					cnt13++;
				} while (true);

				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:145:18:
				// ( binaryExpression | keywordExpression )?
				int alt14 = 3;
				int LA14_0 = input.LA(1);

				if ((LA14_0 == PIPE || LA14_0 == BINARYOP)) {
					alt14 = 1;
				} else if ((LA14_0 == KEYWORD)) {
					alt14 = 2;
				}
				switch (alt14) {
				case 1:
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:145:19:
				// binaryExpression
				{
					pushFollow(FOLLOW_binaryExpression_in_unaryExpression633);
					binaryExpression40 = binaryExpression();

					state._fsp--;

					adaptor.addChild(root_0, binaryExpression40.getTree());

				}
					break;
				case 2:
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:145:38:
				// keywordExpression
				{
					pushFollow(FOLLOW_keywordExpression_in_unaryExpression637);
					keywordExpression41 = keywordExpression();

					state._fsp--;

					adaptor.addChild(root_0, keywordExpression41.getTree());

				}
					break;

				}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "unaryExpression"

	public static class unaryMessage_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "unaryMessage"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:147:1:
	// unaryMessage : IDENTIFIER -> ^( UNARYMSG IDENTIFIER ) ;
	public final PonderTalkParser.unaryMessage_return unaryMessage()
			throws RecognitionException {
		PonderTalkParser.unaryMessage_return retval = new PonderTalkParser.unaryMessage_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token IDENTIFIER42 = null;

		Object IDENTIFIER42_tree = null;
		RewriteRuleTokenStream stream_IDENTIFIER = new RewriteRuleTokenStream(
				adaptor, "token IDENTIFIER");

		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:148:2:
			// ( IDENTIFIER -> ^( UNARYMSG IDENTIFIER ) )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:148:4:
			// IDENTIFIER
			{
				IDENTIFIER42 = (Token) match(input, IDENTIFIER,
						FOLLOW_IDENTIFIER_in_unaryMessage649);
				stream_IDENTIFIER.add(IDENTIFIER42);

				// AST REWRITE
				// elements: IDENTIFIER
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 148:15: -> ^( UNARYMSG IDENTIFIER )
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:148:18:
					// ^( UNARYMSG IDENTIFIER )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor.becomeRoot(
								(Object) adaptor.create(UNARYMSG, "UNARYMSG"),
								root_1);

						adaptor.addChild(root_1, stream_IDENTIFIER.nextNode());

						adaptor.addChild(root_0, root_1);
					}

				}

				retval.tree = root_0;
			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "unaryMessage"

	public static class binaryExpression_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "binaryExpression"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:150:1:
	// binaryExpression : binaryMessage ( keywordExpression )? -> binaryMessage
	// ( keywordExpression )? ;
	public final PonderTalkParser.binaryExpression_return binaryExpression()
			throws RecognitionException {
		PonderTalkParser.binaryExpression_return retval = new PonderTalkParser.binaryExpression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		PonderTalkParser.binaryMessage_return binaryMessage43 = null;

		PonderTalkParser.keywordExpression_return keywordExpression44 = null;

		RewriteRuleSubtreeStream stream_keywordExpression = new RewriteRuleSubtreeStream(
				adaptor, "rule keywordExpression");
		RewriteRuleSubtreeStream stream_binaryMessage = new RewriteRuleSubtreeStream(
				adaptor, "rule binaryMessage");
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:151:2:
			// ( binaryMessage ( keywordExpression )? -> binaryMessage (
			// keywordExpression )? )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:151:4:
			// binaryMessage ( keywordExpression )?
			{
				pushFollow(FOLLOW_binaryMessage_in_binaryExpression667);
				binaryMessage43 = binaryMessage();

				state._fsp--;

				stream_binaryMessage.add(binaryMessage43.getTree());
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:151:18:
				// ( keywordExpression )?
				int alt15 = 2;
				int LA15_0 = input.LA(1);

				if ((LA15_0 == KEYWORD)) {
					alt15 = 1;
				}
				switch (alt15) {
				case 1:
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:151:18:
				// keywordExpression
				{
					pushFollow(FOLLOW_keywordExpression_in_binaryExpression669);
					keywordExpression44 = keywordExpression();

					state._fsp--;

					stream_keywordExpression.add(keywordExpression44.getTree());

				}
					break;

				}

				// AST REWRITE
				// elements: keywordExpression, binaryMessage
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 152:3: -> binaryMessage ( keywordExpression )?
				{
					adaptor.addChild(root_0, stream_binaryMessage.nextTree());
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:152:20:
					// ( keywordExpression )?
					if (stream_keywordExpression.hasNext()) {
						adaptor.addChild(root_0,
								stream_keywordExpression.nextTree());

					}
					stream_keywordExpression.reset();

				}

				retval.tree = root_0;
			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "binaryExpression"

	public static class keywordExpression_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "keywordExpression"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:154:1:
	// keywordExpression : ( keywordMessagePart )+ -> ^( KEYWORDMSG (
	// keywordMessagePart )+ ) ;
	public final PonderTalkParser.keywordExpression_return keywordExpression()
			throws RecognitionException {
		PonderTalkParser.keywordExpression_return retval = new PonderTalkParser.keywordExpression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		PonderTalkParser.keywordMessagePart_return keywordMessagePart45 = null;

		RewriteRuleSubtreeStream stream_keywordMessagePart = new RewriteRuleSubtreeStream(
				adaptor, "rule keywordMessagePart");
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:155:2:
			// ( ( keywordMessagePart )+ -> ^( KEYWORDMSG ( keywordMessagePart
			// )+ ) )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:155:4:
			// ( keywordMessagePart )+
			{
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:155:4:
				// ( keywordMessagePart )+
				int cnt16 = 0;
				loop16: do {
					int alt16 = 2;
					int LA16_0 = input.LA(1);

					if ((LA16_0 == KEYWORD)) {
						alt16 = 1;
					}

					switch (alt16) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:155:4:
					// keywordMessagePart
					{
						pushFollow(FOLLOW_keywordMessagePart_in_keywordExpression689);
						keywordMessagePart45 = keywordMessagePart();

						state._fsp--;

						stream_keywordMessagePart.add(keywordMessagePart45
								.getTree());

					}
						break;

					default:
						if (cnt16 >= 1)
							break loop16;
						EarlyExitException eee = new EarlyExitException(16,
								input);
						throw eee;
					}
					cnt16++;
				} while (true);

				// AST REWRITE
				// elements: keywordMessagePart
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 156:3: -> ^( KEYWORDMSG ( keywordMessagePart )+ )
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:156:6:
					// ^( KEYWORDMSG ( keywordMessagePart )+ )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor.becomeRoot((Object) adaptor
								.create(KEYWORDMSG, "KEYWORDMSG"), root_1);

						if (!(stream_keywordMessagePart.hasNext())) {
							throw new RewriteEarlyExitException();
						}
						while (stream_keywordMessagePart.hasNext()) {
							adaptor.addChild(root_1,
									stream_keywordMessagePart.nextTree());

						}
						stream_keywordMessagePart.reset();

						adaptor.addChild(root_0, root_1);
					}

				}

				retval.tree = root_0;
			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "keywordExpression"

	public static class keywordMessagePart_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "keywordMessagePart"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:158:1:
	// keywordMessagePart : KEYWORD unaryObjectMessage ( binaryMessage )? -> ^(
	// KEYWORD ) ^( EXPRESSION unaryObjectMessage ( binaryMessage )? ) ;
	public final PonderTalkParser.keywordMessagePart_return keywordMessagePart()
			throws RecognitionException {
		PonderTalkParser.keywordMessagePart_return retval = new PonderTalkParser.keywordMessagePart_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token KEYWORD46 = null;
		PonderTalkParser.unaryObjectMessage_return unaryObjectMessage47 = null;

		PonderTalkParser.binaryMessage_return binaryMessage48 = null;

		Object KEYWORD46_tree = null;
		RewriteRuleTokenStream stream_KEYWORD = new RewriteRuleTokenStream(
				adaptor, "token KEYWORD");
		RewriteRuleSubtreeStream stream_unaryObjectMessage = new RewriteRuleSubtreeStream(
				adaptor, "rule unaryObjectMessage");
		RewriteRuleSubtreeStream stream_binaryMessage = new RewriteRuleSubtreeStream(
				adaptor, "rule binaryMessage");
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:159:2:
			// ( KEYWORD unaryObjectMessage ( binaryMessage )? -> ^( KEYWORD )
			// ^( EXPRESSION unaryObjectMessage ( binaryMessage )? ) )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:159:4:
			// KEYWORD unaryObjectMessage ( binaryMessage )?
			{
				KEYWORD46 = (Token) match(input, KEYWORD,
						FOLLOW_KEYWORD_in_keywordMessagePart711);
				stream_KEYWORD.add(KEYWORD46);

				pushFollow(FOLLOW_unaryObjectMessage_in_keywordMessagePart713);
				unaryObjectMessage47 = unaryObjectMessage();

				state._fsp--;

				stream_unaryObjectMessage.add(unaryObjectMessage47.getTree());
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:159:31:
				// ( binaryMessage )?
				int alt17 = 2;
				int LA17_0 = input.LA(1);

				if ((LA17_0 == PIPE || LA17_0 == BINARYOP)) {
					alt17 = 1;
				}
				switch (alt17) {
				case 1:
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:159:31:
				// binaryMessage
				{
					pushFollow(FOLLOW_binaryMessage_in_keywordMessagePart715);
					binaryMessage48 = binaryMessage();

					state._fsp--;

					stream_binaryMessage.add(binaryMessage48.getTree());

				}
					break;

				}

				// AST REWRITE
				// elements: binaryMessage, unaryObjectMessage, KEYWORD
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 160:3: -> ^( KEYWORD ) ^( EXPRESSION unaryObjectMessage (
				// binaryMessage )? )
				{
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:160:6:
					// ^( KEYWORD )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor.becomeRoot(
								stream_KEYWORD.nextNode(), root_1);

						adaptor.addChild(root_0, root_1);
					}
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:160:17:
					// ^( EXPRESSION unaryObjectMessage ( binaryMessage )? )
					{
						Object root_1 = (Object) adaptor.nil();
						root_1 = (Object) adaptor.becomeRoot((Object) adaptor
								.create(EXPRESSION, "EXPRESSION"), root_1);

						adaptor.addChild(root_1,
								stream_unaryObjectMessage.nextTree());
						// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:160:49:
						// ( binaryMessage )?
						if (stream_binaryMessage.hasNext()) {
							adaptor.addChild(root_1,
									stream_binaryMessage.nextTree());

						}
						stream_binaryMessage.reset();

						adaptor.addChild(root_0, root_1);
					}

				}

				retval.tree = root_0;
			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "keywordMessagePart"

	public static class binaryMessage_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "binaryMessage"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:162:1:
	// binaryMessage : ( binaryOp unaryObjectMessage )+ -> ( ^( BINARYMSG
	// binaryOp ^( EXPRESSION unaryObjectMessage ) ) )+ ;
	public final PonderTalkParser.binaryMessage_return binaryMessage()
			throws RecognitionException {
		PonderTalkParser.binaryMessage_return retval = new PonderTalkParser.binaryMessage_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		PonderTalkParser.binaryOp_return binaryOp49 = null;

		PonderTalkParser.unaryObjectMessage_return unaryObjectMessage50 = null;

		RewriteRuleSubtreeStream stream_binaryOp = new RewriteRuleSubtreeStream(
				adaptor, "rule binaryOp");
		RewriteRuleSubtreeStream stream_unaryObjectMessage = new RewriteRuleSubtreeStream(
				adaptor, "rule unaryObjectMessage");
		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:163:2:
			// ( ( binaryOp unaryObjectMessage )+ -> ( ^( BINARYMSG binaryOp ^(
			// EXPRESSION unaryObjectMessage ) ) )+ )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:163:4:
			// ( binaryOp unaryObjectMessage )+
			{
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:163:4:
				// ( binaryOp unaryObjectMessage )+
				int cnt18 = 0;
				loop18: do {
					int alt18 = 2;
					int LA18_0 = input.LA(1);

					if ((LA18_0 == PIPE || LA18_0 == BINARYOP)) {
						alt18 = 1;
					}

					switch (alt18) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:163:5:
					// binaryOp unaryObjectMessage
					{
						pushFollow(FOLLOW_binaryOp_in_binaryMessage744);
						binaryOp49 = binaryOp();

						state._fsp--;

						stream_binaryOp.add(binaryOp49.getTree());
						pushFollow(FOLLOW_unaryObjectMessage_in_binaryMessage746);
						unaryObjectMessage50 = unaryObjectMessage();

						state._fsp--;

						stream_unaryObjectMessage.add(unaryObjectMessage50
								.getTree());

					}
						break;

					default:
						if (cnt18 >= 1)
							break loop18;
						EarlyExitException eee = new EarlyExitException(18,
								input);
						throw eee;
					}
					cnt18++;
				} while (true);

				// AST REWRITE
				// elements: binaryOp, unaryObjectMessage
				// token labels:
				// rule labels: retval
				// token list labels:
				// rule list labels:
				// wildcard labels:
				retval.tree = root_0;
				RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(
						adaptor, "rule retval", retval != null ? retval.tree
								: null);

				root_0 = (Object) adaptor.nil();
				// 164:3: -> ( ^( BINARYMSG binaryOp ^( EXPRESSION
				// unaryObjectMessage ) ) )+
				{
					if (!(stream_binaryOp.hasNext() || stream_unaryObjectMessage
							.hasNext())) {
						throw new RewriteEarlyExitException();
					}
					while (stream_binaryOp.hasNext()
							|| stream_unaryObjectMessage.hasNext()) {
						// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:164:6:
						// ^( BINARYMSG binaryOp ^( EXPRESSION
						// unaryObjectMessage ) )
						{
							Object root_1 = (Object) adaptor.nil();
							root_1 = (Object) adaptor.becomeRoot(
									(Object) adaptor.create(BINARYMSG,
											"BINARYMSG"), root_1);

							adaptor.addChild(root_1, stream_binaryOp.nextTree());
							// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:164:28:
							// ^( EXPRESSION unaryObjectMessage )
							{
								Object root_2 = (Object) adaptor.nil();
								root_2 = (Object) adaptor.becomeRoot(
										(Object) adaptor.create(EXPRESSION,
												"EXPRESSION"), root_2);

								adaptor.addChild(root_2,
										stream_unaryObjectMessage.nextTree());

								adaptor.addChild(root_1, root_2);
							}

							adaptor.addChild(root_0, root_1);
						}

					}
					stream_binaryOp.reset();
					stream_unaryObjectMessage.reset();

				}

				retval.tree = root_0;
			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "binaryMessage"

	public static class unaryObjectMessage_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "unaryObjectMessage"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:166:1:
	// unaryObjectMessage : object ( unaryMessage )* ;
	public final PonderTalkParser.unaryObjectMessage_return unaryObjectMessage()
			throws RecognitionException {
		PonderTalkParser.unaryObjectMessage_return retval = new PonderTalkParser.unaryObjectMessage_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		PonderTalkParser.object_return object51 = null;

		PonderTalkParser.unaryMessage_return unaryMessage52 = null;

		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:167:2:
			// ( object ( unaryMessage )* )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:167:4:
			// object ( unaryMessage )*
			{
				root_0 = (Object) adaptor.nil();

				pushFollow(FOLLOW_object_in_unaryObjectMessage776);
				object51 = object();

				state._fsp--;

				adaptor.addChild(root_0, object51.getTree());
				// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:167:11:
				// ( unaryMessage )*
				loop19: do {
					int alt19 = 2;
					int LA19_0 = input.LA(1);

					if ((LA19_0 == IDENTIFIER)) {
						alt19 = 1;
					}

					switch (alt19) {
					case 1:
					// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:167:11:
					// unaryMessage
					{
						pushFollow(FOLLOW_unaryMessage_in_unaryObjectMessage778);
						unaryMessage52 = unaryMessage();

						state._fsp--;

						adaptor.addChild(root_0, unaryMessage52.getTree());

					}
						break;

					default:
						break loop19;
					}
				} while (true);

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "unaryObjectMessage"

	public static class pathname_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "pathname"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:169:1:
	// pathname : ( PATHNAME | IDENTIFIER );
	public final PonderTalkParser.pathname_return pathname()
			throws RecognitionException {
		PonderTalkParser.pathname_return retval = new PonderTalkParser.pathname_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set53 = null;

		Object set53_tree = null;

		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:170:2:
			// ( PATHNAME | IDENTIFIER )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:
			{
				root_0 = (Object) adaptor.nil();

				set53 = (Token) input.LT(1);
				if (input.LA(1) == IDENTIFIER || input.LA(1) == PATHNAME) {
					input.consume();
					adaptor.addChild(root_0, (Object) adaptor.create(set53));
					state.errorRecovery = false;
				} else {
					MismatchedSetException mse = new MismatchedSetException(
							null, input);
					throw mse;
				}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "pathname"

	public static class binaryOp_return extends ParserRuleReturnScope {
		Object tree;

		public Object getTree() {
			return tree;
		}
	};

	// $ANTLR start "binaryOp"
	// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:172:1:
	// binaryOp : ( PIPE | BINARYOP );
	public final PonderTalkParser.binaryOp_return binaryOp()
			throws RecognitionException {
		PonderTalkParser.binaryOp_return retval = new PonderTalkParser.binaryOp_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set54 = null;

		Object set54_tree = null;

		try {
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:172:9:
			// ( PIPE | BINARYOP )
			// /Users/kevin/Programming/workspace/Eclipse/Ponder2/parser/PonderTalk.g:
			{
				root_0 = (Object) adaptor.nil();

				set54 = (Token) input.LT(1);
				if (input.LA(1) == PIPE || input.LA(1) == BINARYOP) {
					input.consume();
					adaptor.addChild(root_0, (Object) adaptor.create(set54));
					state.errorRecovery = false;
				} else {
					MismatchedSetException mse = new MismatchedSetException(
							null, input);
					throw mse;
				}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object) adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		} catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
			retval.tree = (Object) adaptor.errorNode(input, retval.start,
					input.LT(-1), re);

		} finally {
		}
		return retval;
	}

	// $ANTLR end "binaryOp"

	// Delegated rules

	protected DFA1 dfa1 = new DFA1(this);
	protected DFA2 dfa2 = new DFA2(this);
	protected DFA4 dfa4 = new DFA4(this);
	protected DFA8 dfa8 = new DFA8(this);
	protected DFA10 dfa10 = new DFA10(this);
	static final String DFA1_eotS = "\14\uffff";
	static final String DFA1_eofS = "\1\13\13\uffff";
	static final String DFA1_minS = "\1\32\13\uffff";
	static final String DFA1_maxS = "\1\63\13\uffff";
	static final String DFA1_acceptS = "\1\uffff\1\1\11\uffff\1\2";
	static final String DFA1_specialS = "\14\uffff}>";
	static final String[] DFA1_transitionS = {
			"\1\1\1\uffff\1\1\2\uffff\4\1\1\uffff\1\1\13\uffff\1\1\1\uffff"
					+ "\2\1", "", "", "", "", "", "", "", "", "", "", "" };

	static final short[] DFA1_eot = DFA.unpackEncodedString(DFA1_eotS);
	static final short[] DFA1_eof = DFA.unpackEncodedString(DFA1_eofS);
	static final char[] DFA1_min = DFA
			.unpackEncodedStringToUnsignedChars(DFA1_minS);
	static final char[] DFA1_max = DFA
			.unpackEncodedStringToUnsignedChars(DFA1_maxS);
	static final short[] DFA1_accept = DFA.unpackEncodedString(DFA1_acceptS);
	static final short[] DFA1_special = DFA.unpackEncodedString(DFA1_specialS);
	static final short[][] DFA1_transition;

	static {
		int numStates = DFA1_transitionS.length;
		DFA1_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA1_transition[i] = DFA.unpackEncodedString(DFA1_transitionS[i]);
		}
	}

	class DFA1 extends DFA {

		public DFA1(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 1;
			this.eot = DFA1_eot;
			this.eof = DFA1_eof;
			this.min = DFA1_min;
			this.max = DFA1_max;
			this.accept = DFA1_accept;
			this.special = DFA1_special;
			this.transition = DFA1_transition;
		}

		public String getDescription() {
			return "107:9: ( sentences )?";
		}
	}

	static final String DFA2_eotS = "\20\uffff";
	static final String DFA2_eofS = "\2\2\16\uffff";
	static final String DFA2_minS = "\1\31\1\32\16\uffff";
	static final String DFA2_maxS = "\2\64\16\uffff";
	static final String DFA2_acceptS = "\2\uffff\1\2\3\uffff\1\1\11\uffff";
	static final String DFA2_specialS = "\20\uffff}>";
	static final String[] DFA2_transitionS = {
			"\1\1\32\uffff\1\2",
			"\1\6\1\uffff\1\6\2\uffff\4\6\1\uffff\1\6\13\uffff\1\6\1\uffff"
					+ "\2\6\1\2", "", "", "", "", "", "", "", "", "", "", "",
			"", "", "" };

	static final short[] DFA2_eot = DFA.unpackEncodedString(DFA2_eotS);
	static final short[] DFA2_eof = DFA.unpackEncodedString(DFA2_eofS);
	static final char[] DFA2_min = DFA
			.unpackEncodedStringToUnsignedChars(DFA2_minS);
	static final char[] DFA2_max = DFA
			.unpackEncodedStringToUnsignedChars(DFA2_maxS);
	static final short[] DFA2_accept = DFA.unpackEncodedString(DFA2_acceptS);
	static final short[] DFA2_special = DFA.unpackEncodedString(DFA2_specialS);
	static final short[][] DFA2_transition;

	static {
		int numStates = DFA2_transitionS.length;
		DFA2_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA2_transition[i] = DFA.unpackEncodedString(DFA2_transitionS[i]);
		}
	}

	class DFA2 extends DFA {

		public DFA2(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 2;
			this.eot = DFA2_eot;
			this.eof = DFA2_eof;
			this.min = DFA2_min;
			this.max = DFA2_max;
			this.accept = DFA2_accept;
			this.special = DFA2_special;
			this.transition = DFA2_transition;
		}

		public String getDescription() {
			return "()* loopback of 110:13: ( DOT sentence )*";
		}
	}

	static final String DFA4_eotS = "\23\uffff";
	static final String DFA4_eofS = "\7\uffff\1\1\13\uffff";
	static final String DFA4_minS = "\1\32\6\uffff\1\31\13\uffff";
	static final String DFA4_maxS = "\1\63\6\uffff\1\64\13\uffff";
	static final String DFA4_acceptS = "\1\uffff\1\1\10\uffff\1\3\1\2\7\uffff";
	static final String DFA4_specialS = "\23\uffff}>";
	static final String[] DFA4_transitionS = {
			"\1\7\1\uffff\1\12\2\uffff\4\1\1\uffff\1\1\13\uffff\1\1\1\uffff"
					+ "\2\1",
			"",
			"",
			"",
			"",
			"",
			"",
			"\2\1\1\13\2\uffff\1\1\4\uffff\1\1\1\uffff\1\1\13\uffff\1\1"
					+ "\2\uffff\1\1", "", "", "", "", "", "", "", "", "", "",
			"" };

	static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
	static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
	static final char[] DFA4_min = DFA
			.unpackEncodedStringToUnsignedChars(DFA4_minS);
	static final char[] DFA4_max = DFA
			.unpackEncodedStringToUnsignedChars(DFA4_maxS);
	static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
	static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
	static final short[][] DFA4_transition;

	static {
		int numStates = DFA4_transitionS.length;
		DFA4_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
		}
	}

	class DFA4 extends DFA {

		public DFA4(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 4;
			this.eot = DFA4_eot;
			this.eof = DFA4_eof;
			this.min = DFA4_min;
			this.max = DFA4_max;
			this.accept = DFA4_accept;
			this.special = DFA4_special;
			this.transition = DFA4_transition;
		}

		public String getDescription() {
			return "115:1: sentence : ( objectMessage | IDENTIFIER ASSIGN sentence -> ^( ASSIGN IDENTIFIER sentence ) | VARIABLEASSIGN sentence -> ^( ASSIGN VARIABLEASSIGN sentence ) );";
		}
	}

	static final String DFA8_eotS = "\12\uffff";
	static final String DFA8_eofS = "\12\uffff";
	static final String DFA8_minS = "\1\32\11\uffff";
	static final String DFA8_maxS = "\1\63\11\uffff";
	static final String DFA8_acceptS = "\1\uffff\1\2\1\1\7\uffff";
	static final String DFA8_specialS = "\12\uffff}>";
	static final String[] DFA8_transitionS = {
			"\1\2\4\uffff\4\2\1\uffff\1\2\13\uffff\1\2\1\1\2\2", "", "", "",
			"", "", "", "", "", "" };

	static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
	static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
	static final char[] DFA8_min = DFA
			.unpackEncodedStringToUnsignedChars(DFA8_minS);
	static final char[] DFA8_max = DFA
			.unpackEncodedStringToUnsignedChars(DFA8_maxS);
	static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
	static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
	static final short[][] DFA8_transition;

	static {
		int numStates = DFA8_transitionS.length;
		DFA8_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
		}
	}

	class DFA8 extends DFA {

		public DFA8(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 8;
			this.eot = DFA8_eot;
			this.eof = DFA8_eof;
			this.min = DFA8_min;
			this.max = DFA8_max;
			this.accept = DFA8_accept;
			this.special = DFA8_special;
			this.transition = DFA8_transition;
		}

		public String getDescription() {
			return "()* loopback of 129:16: ( object )*";
		}
	}

	static final String DFA10_eotS = "\14\uffff";
	static final String DFA10_eofS = "\14\uffff";
	static final String DFA10_minS = "\1\32\13\uffff";
	static final String DFA10_maxS = "\1\63\13\uffff";
	static final String DFA10_acceptS = "\1\uffff\1\1\1\2\11\uffff";
	static final String DFA10_specialS = "\14\uffff}>";
	static final String[] DFA10_transitionS = {
			"\1\2\1\uffff\1\2\1\1\1\uffff\4\2\1\uffff\1\2\13\uffff\1\2\1"
					+ "\uffff\2\2", "", "", "", "", "", "", "", "", "", "", "" };

	static final short[] DFA10_eot = DFA.unpackEncodedString(DFA10_eotS);
	static final short[] DFA10_eof = DFA.unpackEncodedString(DFA10_eofS);
	static final char[] DFA10_min = DFA
			.unpackEncodedStringToUnsignedChars(DFA10_minS);
	static final char[] DFA10_max = DFA
			.unpackEncodedStringToUnsignedChars(DFA10_maxS);
	static final short[] DFA10_accept = DFA.unpackEncodedString(DFA10_acceptS);
	static final short[] DFA10_special = DFA
			.unpackEncodedString(DFA10_specialS);
	static final short[][] DFA10_transition;

	static {
		int numStates = DFA10_transitionS.length;
		DFA10_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA10_transition[i] = DFA.unpackEncodedString(DFA10_transitionS[i]);
		}
	}

	class DFA10 extends DFA {

		public DFA10(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 10;
			this.eot = DFA10_eot;
			this.eof = DFA10_eof;
			this.min = DFA10_min;
			this.max = DFA10_max;
			this.accept = DFA10_accept;
			this.special = DFA10_special;
			this.transition = DFA10_transition;
		}

		public String getDescription() {
			return "132:14: ( ( BLOCKARG )+ PIPE )?";
		}
	}

	public static final BitSet FOLLOW_sentences_in_start286 = new BitSet(
			new long[] { 0x0000000000000000L });
	public static final BitSet FOLLOW_EOF_in_start289 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_sentence_in_sentences299 = new BitSet(
			new long[] { 0x0000000002000002L });
	public static final BitSet FOLLOW_DOT_in_sentences302 = new BitSet(
			new long[] { 0x000D001794000000L });
	public static final BitSet FOLLOW_sentence_in_sentences304 = new BitSet(
			new long[] { 0x0000000002000002L });
	public static final BitSet FOLLOW_DOT_in_sentences308 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_objectMessage_in_sentence326 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_IDENTIFIER_in_sentence331 = new BitSet(
			new long[] { 0x0000000008000000L });
	public static final BitSet FOLLOW_ASSIGN_in_sentence333 = new BitSet(
			new long[] { 0x000D001794000000L });
	public static final BitSet FOLLOW_sentence_in_sentence335 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_VARIABLEASSIGN_in_sentence350 = new BitSet(
			new long[] { 0x000D001794000000L });
	public static final BitSet FOLLOW_sentence_in_sentence352 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_object_in_objectMessage372 = new BitSet(
			new long[] { 0x0000002844000002L });
	public static final BitSet FOLLOW_messageExpression_in_objectMessage375 = new BitSet(
			new long[] { 0x0000800000000002L });
	public static final BitSet FOLLOW_47_in_objectMessage379 = new BitSet(
			new long[] { 0x0000002844000000L });
	public static final BitSet FOLLOW_messageExpression_in_objectMessage381 = new BitSet(
			new long[] { 0x0000800000000002L });
	public static final BitSet FOLLOW_array_in_object421 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_block_in_object426 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_literal_in_object431 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_pathname_in_object444 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_48_in_object449 = new BitSet(
			new long[] { 0x000D001794000000L });
	public static final BitSet FOLLOW_sentence_in_object451 = new BitSet(
			new long[] { 0x0002000000000000L });
	public static final BitSet FOLLOW_49_in_object453 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_50_in_array468 = new BitSet(
			new long[] { 0x000F001784000000L });
	public static final BitSet FOLLOW_object_in_array470 = new BitSet(
			new long[] { 0x000F001784000000L });
	public static final BitSet FOLLOW_49_in_array473 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_51_in_block501 = new BitSet(
			new long[] { 0x000D0017B4000000L });
	public static final BitSet FOLLOW_BLOCKARG_in_block505 = new BitSet(
			new long[] { 0x0000000060000000L });
	public static final BitSet FOLLOW_PIPE_in_block508 = new BitSet(
			new long[] { 0x000D0017B4000000L });
	public static final BitSet FOLLOW_sentences_in_block513 = new BitSet(
			new long[] { 0x0010000000000000L });
	public static final BitSet FOLLOW_52_in_block515 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_BOOLEANLITERAL_in_literal551 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_CHARACTERLITERAL_in_literal565 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_NUMBERLITERAL_in_literal579 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_STRINGLITERAL_in_literal593 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_unaryExpression_in_messageExpression611 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_binaryExpression_in_messageExpression615 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_keywordExpression_in_messageExpression619 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_unaryMessage_in_unaryExpression629 = new BitSet(
			new long[] { 0x0000002844000002L });
	public static final BitSet FOLLOW_binaryExpression_in_unaryExpression633 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_keywordExpression_in_unaryExpression637 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_IDENTIFIER_in_unaryMessage649 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_binaryMessage_in_binaryExpression667 = new BitSet(
			new long[] { 0x0000002844000002L });
	public static final BitSet FOLLOW_keywordExpression_in_binaryExpression669 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_keywordMessagePart_in_keywordExpression689 = new BitSet(
			new long[] { 0x0000002844000002L });
	public static final BitSet FOLLOW_KEYWORD_in_keywordMessagePart711 = new BitSet(
			new long[] { 0x000D001784000000L });
	public static final BitSet FOLLOW_unaryObjectMessage_in_keywordMessagePart713 = new BitSet(
			new long[] { 0x0000002040000002L });
	public static final BitSet FOLLOW_binaryMessage_in_keywordMessagePart715 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_binaryOp_in_binaryMessage744 = new BitSet(
			new long[] { 0x000D001784000000L });
	public static final BitSet FOLLOW_unaryObjectMessage_in_binaryMessage746 = new BitSet(
			new long[] { 0x0000002040000002L });
	public static final BitSet FOLLOW_object_in_unaryObjectMessage776 = new BitSet(
			new long[] { 0x0000000004000002L });
	public static final BitSet FOLLOW_unaryMessage_in_unaryObjectMessage778 = new BitSet(
			new long[] { 0x0000000004000002L });
	public static final BitSet FOLLOW_set_in_pathname0 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_set_in_binaryOp0 = new BitSet(
			new long[] { 0x0000000000000002L });

}