/* *
 * Developed  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2019.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2019 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites or repositories,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2019
 */

package cop5556fa19;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import cop5556fa19.Scanner.LexicalException;

import static cop5556fa19.Token.Kind.*;

class ScannerTest {
	
	//I like this to make it easy to print objects and turn this output on and off
	static boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	

	 /**
	  * Example showing how to get input from a Java string literal.
	  * 
	  * In this case, the string is empty.  The only Token that should be returned is an EOF Token.  
	  * 
	  * This test case passes with the provided skeleton, and should also pass in your final implementation.
	  * Note that calling getNext again after having reached the end of the input should just return another EOF Token.
	  * 
	  */
	@Test 
	void test0() throws Exception {
		Reader r = new StringReader("");
		Scanner s = new Scanner(r);
		Token t;
		show(t= s.getNext()); 
		assertEquals(EOF, t.kind);
		show(t= s.getNext());
		assertEquals(EOF, t.kind);
	}

	/**
	 * Example showing how to create a test case to ensure that an exception is thrown when illegal input is given.
	 * 
	 * This "@" character is illegal in the final scanner (except as part of a String literal or comment). So this
	 * test should remain valid in your complete Scanner.
	 */
	@Test
	void test1() throws Exception {
		Reader r = new StringReader("@");
		Scanner s = new Scanner(r);
        assertThrows(LexicalException.class, ()->{
		   s.getNext();
        });
	}
	
	/**
	 * Example showing how to read the input from a file.  Otherwise it is the same as test1.
	 *
	 */
	@Test
	void test2() throws Exception {
		String file = "testInputFiles/test2.input"; 
		Reader r = new BufferedReader(new FileReader(file));
		Scanner s = new Scanner(r);
        assertThrows(LexicalException.class, ()->{
		   s.getNext();
        });
	}
	

	
	/**
	 * Another example.  This test case will fail with the provided code, but should pass in your completed Scanner.
	 * @throws Exception
	 */
	@Test
	void test3() throws Exception {
		Reader r = new StringReader(",,::==");
		Scanner s = new Scanner(r);
		Token t;
		show(t= s.getNext());
		assertEquals(t.kind,COMMA);
		assertEquals(t.text,",");
		show(t = s.getNext());
		assertEquals(t.kind,COMMA);
		assertEquals(t.text,",");
		
		show(t = s.getNext());
		assertEquals(t.kind,COLONCOLON);
		assertEquals(t.text,"::");
		
		show(t = s.getNext());
		assertEquals(t.kind,REL_EQEQ);
		assertEquals(t.text,"==");
	}
	
	@Test 
	void test4() throws Exception {
		Reader r = new StringReader("00");
		Scanner s = new Scanner(r);
		Token t;
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext());
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(EOF, t.kind);
	}

	@Test 
	void test5() throws Exception {
		Reader r = new StringReader("0100 143\n65");
		Scanner s = new Scanner(r);
		Token t;
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext());
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext());
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(EOF, t.kind);
	}
	
	@Test 
	void test6() throws Exception {
		Reader r = new StringReader("0123abc andbreak and\nbreak "
				+ "do else elseif end false for function goto if "
				+ "in local nil not or repeat return then true  \n "
				+ "until while");
		Scanner s = new Scanner(r);
		Token t;
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext());
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext());
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_and, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_break, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_do, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_else, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_elseif, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_end, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_false, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_for, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_function, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_goto, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_if, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_in, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_local, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_nil, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_not, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_or, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_repeat, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_return, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_then, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_true, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_until, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_while, t.kind);
		show(t= s.getNext()); 
		assertEquals(EOF, t.kind);
	}
	
	@Test 
	void test7() throws Exception {
		Reader r = new StringReader("11//45+06/8-76*4\n"
				+ "%^#&~|>><<<//>===~=<=>==");
		Scanner s = new Scanner(r);
		Token t;
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext());
		assertEquals(OP_DIVDIV, t.kind);
		show(t= s.getNext());
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(OP_PLUS, t.kind);
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(OP_DIV, t.kind);
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(OP_MINUS, t.kind);
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(OP_TIMES, t.kind);
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(OP_MOD, t.kind);
		show(t= s.getNext()); 
		assertEquals(OP_POW, t.kind);
		show(t= s.getNext()); 
		assertEquals(OP_HASH, t.kind);
		show(t= s.getNext()); 
		assertEquals(BIT_AMP, t.kind);
		show(t= s.getNext()); 
		assertEquals(BIT_XOR, t.kind);
		show(t= s.getNext()); 
		assertEquals(BIT_OR, t.kind);
		show(t= s.getNext()); 
		assertEquals(BIT_SHIFTR, t.kind);
		show(t= s.getNext()); 
		assertEquals(BIT_SHIFTL, t.kind);
		show(t= s.getNext()); 
		assertEquals(REL_LT, t.kind);
		show(t= s.getNext()); 
		assertEquals(OP_DIVDIV, t.kind);
		show(t= s.getNext()); 
		assertEquals(REL_GE, t.kind);
		show(t= s.getNext()); 
		assertEquals(REL_EQEQ, t.kind);
		show(t= s.getNext()); 
		assertEquals(REL_NOTEQ, t.kind);
		show(t= s.getNext()); 
		assertEquals(REL_LE, t.kind);
		show(t= s.getNext()); 
		assertEquals(REL_GE, t.kind);
		show(t= s.getNext()); 
		assertEquals(ASSIGN, t.kind);
		show(t= s.getNext()); 
		assertEquals(EOF, t.kind);
	}
	
	@Test 
	void test8() throws Exception {
		Reader r = new StringReader("][(}){:::;.,...../\r\n.");
		Scanner s = new Scanner(r);
		Token t;
		show(t= s.getNext()); 
		assertEquals(RSQUARE, t.kind);
		show(t= s.getNext()); 
		assertEquals(LSQUARE, t.kind);
		show(t= s.getNext()); 
		assertEquals(LPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(RCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(RPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(LCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(COLONCOLON, t.kind);
		show(t= s.getNext()); 
		assertEquals(COLON, t.kind);
		show(t= s.getNext()); 
		assertEquals(SEMI, t.kind);
		show(t= s.getNext()); 
		assertEquals(DOT, t.kind);
		show(t= s.getNext()); 
		assertEquals(COMMA, t.kind);
		show(t= s.getNext()); 
		assertEquals(DOTDOTDOT, t.kind);
		show(t= s.getNext()); 
		assertEquals(DOTDOT, t.kind);
		show(t= s.getNext()); 
		assertEquals(OP_DIV, t.kind);
		show(t= s.getNext()); 
		assertEquals(DOT, t.kind);
		show(t= s.getNext()); 
		assertEquals(EOF, t.kind);
	}
	
	@Test 
	void test9() throws Exception {
		Reader r = new StringReader("2147483647");
		Scanner s = new Scanner(r);
		Token t;
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(EOF, t.kind);
	}

	@Test
	void test10() throws Exception {
		Reader r = new StringReader("2147483648");
		Scanner s = new Scanner(r);
        assertThrows(LexicalException.class, ()->{
		   s.getNext();
        });
	}
	
	@Test
	void test11() throws Exception {
		Reader r = new StringReader("--asdlkjsdlkja\ntest 1-40--1\r\n--5");
		Scanner s = new Scanner(r);
		Token t;
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(OP_MINUS, t.kind);
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(EOF, t.kind);
	}
	
	@Test
	void test12() throws Exception {
		String str = "\"fdsjhdf79sd7fdujk dfs8uifds\\a\\b\\f\\n\\r\\t\\v\\\"\"";
		Reader r = new StringReader(str);
		Scanner s = new Scanner(r);
		Token t;
		show(t= s.getNext()); 
		assertEquals(STRINGLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(EOF, t.kind);
	}
	
	@Test
	void test13() throws Exception {
		String str = "\'fdsjhdf79sd7fdujk dfs8uifds\\a\\b\\f\\n\\r\\t\\v\\\"\'";
		Reader r = new StringReader(str);
		Scanner s = new Scanner(r);
		Token t;
		show(t= s.getNext()); 
		assertEquals(STRINGLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(EOF, t.kind);
	}
	
	@Test
	void test14() throws Exception {
		String str = "\' \\ \'";
		Reader r = new StringReader(str);
		Scanner s = new Scanner(r);
		Token t;
        assertThrows(LexicalException.class, ()->{
		   s.getNext();
        });
	}
	
	@Test
	void test15() throws Exception {
		String str = "\' \" \'";
		Reader r = new StringReader(str);
		Scanner s = new Scanner(r);
		Token t;
        assertThrows(LexicalException.class, ()->{
		   s.getNext();
        });
	}
	
	@Test
	void test16() throws Exception {
		String str = "\" \' \"";
		Reader r = new StringReader(str);
		Scanner s = new Scanner(r);
		Token t;
        assertThrows(LexicalException.class, ()->{
		   s.getNext();
        });
	}
	
	@Test
	void test17() throws Exception {
		String file = "testInputFiles/test3.input"; 
		Reader r = new BufferedReader(new FileReader(file));
		Scanner s = new Scanner(r);
		Token t;
		show(t= s.getNext()); 
		assertEquals(KW_function, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(LPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(RPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(COLON, t.kind);
		show(t= s.getNext()); 
		assertEquals(LCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(ASSIGN, t.kind);
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(SEMI, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(ASSIGN, t.kind);
		show(t= s.getNext()); 
		assertEquals(STRINGLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(SEMI, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(ASSIGN, t.kind);
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(SEMI, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_for, t.kind);
		show(t= s.getNext()); 
		assertEquals(LPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(SEMI, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(REL_LT, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(SEMI, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(ASSIGN, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(OP_PLUS, t.kind);
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(RPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(COLON, t.kind);
		show(t= s.getNext()); 
		assertEquals(LCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_if, t.kind);
		show(t= s.getNext()); 
		assertEquals(LPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(REL_LT, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(RPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_then, t.kind);
		show(t= s.getNext()); 
		assertEquals(COLON, t.kind);
		show(t= s.getNext()); 
		assertEquals(LCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_while, t.kind);
		show(t= s.getNext()); 
		assertEquals(LPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(REL_GT, t.kind);
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_or, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(REL_LT, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(RPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(COLON, t.kind);
		show(t= s.getNext()); 
		assertEquals(LCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_break, t.kind);
		show(t= s.getNext()); 
		assertEquals(SEMI, t.kind);
		show(t= s.getNext()); 
		assertEquals(RCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(RCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_elseif, t.kind);
		show(t= s.getNext()); 
		assertEquals(LPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(OP_TIMES, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(REL_GE, t.kind);
		show(t= s.getNext()); 
		assertEquals(INTLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(RPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_then, t.kind);
		show(t= s.getNext()); 
		assertEquals(COLON, t.kind);
		show(t= s.getNext()); 
		assertEquals(LCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_break, t.kind);
		show(t= s.getNext()); 
		assertEquals(SEMI, t.kind);
		show(t= s.getNext()); 
		assertEquals(RCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_else, t.kind);
		show(t= s.getNext()); 
		assertEquals(COLON, t.kind);
		show(t= s.getNext()); 
		assertEquals(LCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(ASSIGN, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(SEMI, t.kind);
		show(t= s.getNext()); 
		assertEquals(RCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_end, t.kind);
		show(t= s.getNext()); 
		assertEquals(RCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(NAME, t.kind);
		show(t= s.getNext()); 
		assertEquals(LPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(STRINGLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(OP_PLUS, t.kind);
		show(t= s.getNext()); 
		assertEquals(STRINGLIT, t.kind);
		show(t= s.getNext()); 
		assertEquals(RPAREN, t.kind);
		show(t= s.getNext()); 
		assertEquals(SEMI, t.kind);
		show(t= s.getNext()); 
		assertEquals(KW_end, t.kind);
		show(t= s.getNext()); 
		assertEquals(RCURLY, t.kind);
		show(t= s.getNext()); 
		assertEquals(EOF, t.kind);
	}
}
