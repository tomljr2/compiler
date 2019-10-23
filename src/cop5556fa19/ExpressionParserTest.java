/* *
 * Developed  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2019.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2019 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2019
 */


package cop5556fa19;

import static cop5556fa19.Token.Kind.*;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

import cop5556fa19.AST.Exp;
import cop5556fa19.AST.ExpBinary;
import cop5556fa19.AST.ExpFalse;
import cop5556fa19.AST.ExpFunction;
import cop5556fa19.AST.ExpInt;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpNil;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTable;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpUnary;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Expressions;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.ParList;
import cop5556fa19.Parser.SyntaxException;

class ExpressionParserTest {

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	
	
	// creates a scanner, parser, and parses the input.  
	Exp parseAndShow(String input) throws Exception {
		show("parser input:\n" + input); // Display the input
		Reader r = new StringReader(input);
		Scanner scanner = new Scanner(r); // Create a Scanner and initialize it
		Parser parser = new Parser(scanner);  // Create a parser
		Exp e = parser.exp(); // Parse and expression
		show("e=" + e);  //Show the resulting AST
		return e;
	}
	


	@Test
	void testIdent0() throws Exception {
		String input = "x";
		Exp e = parseAndShow(input);
		assertEquals(ExpName.class, e.getClass());
		assertEquals("x", ((ExpName) e).name);
	}

	@Test
	void testIdent1() throws Exception {
		String input = "(x)";
		Exp e = parseAndShow(input);
		assertEquals(ExpName.class, e.getClass());
		assertEquals("x", ((ExpName) e).name);
	}
	
	@Test
	void testIdent2() throws Exception {
		String input = "nil";
		Exp e = parseAndShow(input);
		assertEquals(ExpNil.class, e.getClass());
	}
	
	@Test
	void testIntLit0() throws Exception {
		String input = "0";
		Exp e = parseAndShow(input);
		assertEquals(ExpInt.class, e.getClass());
	}
	
	@Test
	void testIntLit1() throws Exception {
		String input = "123";
		Exp e = parseAndShow(input);
		assertEquals(ExpInt.class, e.getClass());
	}

	@Test
	void testString0() throws Exception {
		String input = "\"string\"";
		Exp e = parseAndShow(input);
		assertEquals(ExpString.class, e.getClass());
		assertEquals("string", ((ExpString) e).v);
	}

	@Test
	void testString1() throws Exception {
		String input = "\'string test 1\'";
		Exp e = parseAndShow(input);
		assertEquals(ExpString.class, e.getClass());
		assertEquals("string test 1", ((ExpString) e).v);
	}

	@Test
	void testBoolean0() throws Exception {
		String input = "true";
		Exp e = parseAndShow(input);
		assertEquals(ExpTrue.class, e.getClass());
	}

	@Test
	void testBoolean1() throws Exception {
		String input = "false";
		Exp e = parseAndShow(input);
		assertEquals(ExpFalse.class, e.getClass());
	}

	@Test
	void testFunc0() throws Exception {
		String input = "function()\nend";
		Exp e = parseAndShow(input);
		assertEquals(ExpFunction.class, e.getClass());
	}

	@Test
	void testFunc1() throws Exception {
		String input = "function(test)\nend";
		Exp e = parseAndShow(input);
		assertEquals(ExpFunction.class, e.getClass());
	}
	
	@Test
	void testTC0() throws Exception {
		String input = "{}";
		Exp e = parseAndShow(input);
		assertEquals(ExpTable.class, e.getClass());
	}
	
	@Test
	void testTC1() throws Exception {
		String input = "{[function(test)\nend]=true, "
				+ "x=5; nil}";
		Exp e = parseAndShow(input);
		assertEquals(ExpTable.class, e.getClass());
	}
	
	@Test
	void testPrefix0() throws Exception {
		String input = "test";
		Exp e = parseAndShow(input);
		assertEquals(ExpName.class, e.getClass());
	}
	
	@Test
	void testPrefix1() throws Exception {
		String input = "(true)";
		Exp e = parseAndShow(input);
		assertEquals("true", e.firstToken.text);
	}

	@Test
	void testBinary0() throws Exception {
		String input = "1 + 2";
		Exp e = parseAndShow(input);
		Exp expected = Expressions.makeBinary(1,OP_PLUS,2);
		show("expected="+expected);
		assertEquals(expected,e);
	}
	
	@Test
	void testUnary0() throws Exception {
		String input = "-2";
		Exp e = parseAndShow(input);
		Exp expected = Expressions.makeExpUnary(OP_MINUS, 2);
		show("expected="+expected);
		assertEquals(expected,e);
	}
	
	@Test
	void testUnary1() throws Exception {
		String input = "-*2\n";
		assertThrows(SyntaxException.class, () -> {
		Exp e = parseAndShow(input);
		});	
	}
	

	
	@Test
	void testRightAssoc() throws Exception {
		String input = "\"concat\" .. \"is\"..\"right associative\"";
		Exp e = parseAndShow(input);
		Exp expected = Expressions.makeBinary(
				Expressions.makeExpString("concat")
				, DOTDOT
				, Expressions.makeBinary("is",DOTDOT,"right associative"));
		show("expected=" + expected);
		assertEquals(expected,e);
	}
	
	@Test
	void testLeftAssoc() throws Exception {
		String input = "\"minus\" - \"is\" - \"left associative\"";
		Exp e = parseAndShow(input);
		Exp expected = Expressions.makeBinary(
				Expressions.makeBinary(
						Expressions.makeExpString("minus")
				, OP_MINUS
				, Expressions.makeExpString("is")), OP_MINUS, 
				Expressions.makeExpString("left associative"));
		show("expected=" + expected);
		assertEquals(expected,e);
		
	}
	
	@Test
	void test0() throws Exception {
		String input = "function (aa, b) end >> function(test, l, ...) end ";
		Exp e = parseAndShow(input);
		
	}
	
	@Test
	void test1() throws Exception {
		String input = "function (aa, b) end >> function(test, l, ...) end & function(...) end";
		Exp e = parseAndShow(input);
		
	}
	
	@Test
	void test2() throws Exception {
		String input = "{}";
		Exp e = parseAndShow(input);
		
	}
	
	@Test
	void test3() throws Exception {
		String input = "{3, a}";
		Exp e = parseAndShow(input);
		
	}
	
	@Test
	void test4() throws Exception {
		String input = "{[x + y] = xx * yy,}";
		Exp e = parseAndShow(input);
		
	}

	@Test
	void test5() throws Exception {
		String input = "function (a,b) end";
		Exp e = parseAndShow(input);
		
	}

	@Test
	void test6() throws Exception {
		String input = "function (...) end";
		Exp e = parseAndShow(input);
		
	}

	@Test
	void test7() throws Exception {
		String input = "function (xy,zy, ...) end";
		Exp e = parseAndShow(input);
		
	}

	@Test
	void test8() throws Exception {
		String input = "a[b][c][d]";
		String input2 = "a.b.c.d";
		String input3 = "a[b].c[d]";
		Exp e = parseAndShow(input);
		Exp e2 = parseAndShow(input2);
		Exp e3 = parseAndShow(input3);
		assertEquals(e,e2);
		assertEquals(e2,e3);
		
	}

	@Test
	void test9() throws Exception {
		String input = "f{x}(y,z)\"hi\"";
		Exp e = parseAndShow(input);
		
	}

	@Test
	void test10() throws Exception {
		String input = "v:name(x,y)";
		String input2 = "v.name(v,x,y)";
		Exp e = parseAndShow(input);
		Exp e2 = parseAndShow(input);
		assertEquals(e,e2);
	}
}
