package cop5556fa19;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cop5556fa19.Parser;
import cop5556fa19.Parser.SyntaxException;
import cop5556fa19.AST.ASTNode;
import cop5556fa19.AST.Block;
import cop5556fa19.AST.Chunk;
import cop5556fa19.AST.Exp;
import cop5556fa19.AST.ExpBinary;
import cop5556fa19.AST.ExpFalse;
import cop5556fa19.AST.ExpFunction;
import cop5556fa19.AST.ExpFunctionCall;
import cop5556fa19.AST.ExpInt;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpNil;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTable;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Expressions;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.ParList;
import cop5556fa19.AST.Stat;
import cop5556fa19.AST.StatAssign;
import cop5556fa19.AST.StatBreak;
import cop5556fa19.AST.StatDo;
import cop5556fa19.AST.StatGoto;
import cop5556fa19.AST.StatLabel;
import cop5556fa19.Scanner;
import cop5556fa19.Token;

import static cop5556fa19.Token.Kind.*;

class ParserTest {

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
//	static final boolean doPrint = false;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	// creates a scanner, parser, and parses the input by calling exp().  
	Exp parseExpAndShow(String input) throws Exception {
		show("parser input:\n" + input); // Display the input
		Reader r = new StringReader(input);
		Scanner scanner = new Scanner(r); // Create a Scanner and initialize it
		Parser parser = new Parser(scanner);
		Exp e = parser.exp();
		show("e=" + e);
		return e;
	}	
	
	
	// creates a scanner, parser, and parses the input by calling block()  
	Block parseBlockAndShow(String input) throws Exception {
		show("parser input:\n" + input); // Display the input
		Reader r = new StringReader(input);
		Scanner scanner = new Scanner(r); // Create a Scanner and initialize it
		Parser parser = new Parser(scanner);
		Method method = Parser.class.getDeclaredMethod("block");
		method.setAccessible(true);
		Block b = (Block) method.invoke(parser);
		show("b=" + b);
		return b;
	}	
	
	
	//creates a scanner, parser, and parses the input by calling parse()
	//this corresponds to the actual use case of the parser
	Chunk parseAndShow(String input) throws Exception {
		show("parser input:\n" + input); // Display the input
		Reader r = new StringReader(input);
		Scanner scanner = new Scanner(r); // Create a Scanner and initialize it
		Parser parser = new Parser(scanner);
		Chunk c = parser.parse();
		show("c="+c);
		return c;
	}
	
	@Test
	void testEmpty1() throws Exception {
		String input = "";
		Block b = parseBlockAndShow(input);
		Block expected = Expressions.makeBlock();
		assertEquals(expected, b);
	}
	
	@Test
	void testEmpty2() throws Exception {
		String input = "";
		ASTNode n = parseAndShow(input);
		Block b = Expressions.makeBlock();
		Chunk expected = new Chunk(b.firstToken,b);
		assertEquals(expected,n);
	}
	
	@Test
	void testAssign1() throws Exception {
		String input = "a=b";
		Block b = parseBlockAndShow(input);		
		List<Exp> lhs = Expressions.makeExpList(Expressions.makeExpNameGlobal("a"));
		List<Exp> rhs = Expressions.makeExpList(Expressions.makeExpNameGlobal("b"));
		StatAssign s = Expressions.makeStatAssign(lhs,rhs);
		Block expected = Expressions.makeBlock(s);
		assertEquals(expected,b);
	}
	
	@Test
	void test0() throws Exception {
		String input = "a[b][c][d] = 5";
		Block b = parseBlockAndShow(input);		
	}
	
	@Test
	void testAssignChunk1() throws Exception {
		String input = "a=b";
		ASTNode c = parseAndShow(input);		
		List<Exp> lhs = Expressions.makeExpList(Expressions.makeExpNameGlobal("a"));
		List<Exp> rhs = Expressions.makeExpList(Expressions.makeExpNameGlobal("b"));
		StatAssign s = Expressions.makeStatAssign(lhs,rhs);
		Block b = Expressions.makeBlock(s);
		Chunk expected = new Chunk(b.firstToken,b);
		assertEquals(expected,c);
	}
	

	@Test
	void testMultiAssign1() throws Exception {
		String input = "a,c=8,9";
		Block b = parseBlockAndShow(input);		
		List<Exp> lhs = Expressions.makeExpList(
					Expressions.makeExpNameGlobal("a")
					,Expressions.makeExpNameGlobal("c"));
		Exp e1 = Expressions.makeExpInt(8);
		Exp e2 = Expressions.makeExpInt(9);
		List<Exp> rhs = Expressions.makeExpList(e1,e2);
		StatAssign s = Expressions.makeStatAssign(lhs,rhs);
		Block expected = Expressions.makeBlock(s);
		assertEquals(expected,b);		
	}
	

	

	@Test
	void testMultiAssign3() throws Exception {
		String input = "a,c=8,f(x)";
		Block b = parseBlockAndShow(input);		
		List<Exp> lhs = Expressions.makeExpList(
					Expressions.makeExpNameGlobal("a")
					,Expressions.makeExpNameGlobal("c"));
		Exp e1 = Expressions.makeExpInt(8);
		List<Exp> args = new ArrayList<>();
		args.add(Expressions.makeExpNameGlobal("x"));
		Exp e2 = Expressions.makeExpFunCall(Expressions.makeExpNameGlobal("f"),args, null);
		List<Exp> rhs = Expressions.makeExpList(e1,e2);
		StatAssign s = Expressions.makeStatAssign(lhs,rhs);
		Block expected = Expressions.makeBlock(s);
		assertEquals(expected,b);			
	}
	

	
	@Test
	void testAssignToTable() throws Exception {
		String input = "g.a.b = 3";
		Block bl = parseBlockAndShow(input);
		ExpName g = Expressions.makeExpNameGlobal("g");
		ExpString a = Expressions.makeExpString("a");
		Exp gtable = Expressions.makeExpTableLookup(g,a);
		ExpString b = Expressions.makeExpString("b");
		Exp v = Expressions.makeExpTableLookup(gtable, b);
		Exp three = Expressions.makeExpInt(3);		
		Stat s = Expressions.makeStatAssign(Expressions.makeExpList(v), Expressions.makeExpList(three));;
		Block expected = Expressions.makeBlock(s);
		assertEquals(expected,bl);
	}
	
	@Test
	void testAssignTableToVar() throws Exception {
		String input = "x = g.a.b";
		Block bl = parseBlockAndShow(input);
		ExpName g = Expressions.makeExpNameGlobal("g");
		ExpString a = Expressions.makeExpString("a");
		Exp gtable = Expressions.makeExpTableLookup(g,a);
		ExpString b = Expressions.makeExpString("b");
		Exp e = Expressions.makeExpTableLookup(gtable, b);
		Exp v = Expressions.makeExpNameGlobal("x");		
		Stat s = Expressions.makeStatAssign(Expressions.makeExpList(v), Expressions.makeExpList(e));;
		Block expected = Expressions.makeBlock(s);
		assertEquals(expected,bl);
	}
	

	
	@Test
	void testmultistatements6() throws Exception {
		String input = "x = g.a.b ; ::mylabel:: do  y = 2 goto mylabel f=a(0,200) end break"; //same as testmultistatements0 except ;
		ASTNode c = parseAndShow(input);
		ExpName g = Expressions.makeExpNameGlobal("g");
		ExpString a = Expressions.makeExpString("a");
		Exp gtable = Expressions.makeExpTableLookup(g,a);
		ExpString b = Expressions.makeExpString("b");
		Exp e = Expressions.makeExpTableLookup(gtable, b);
		Exp v = Expressions.makeExpNameGlobal("x");		
		Stat s0 = Expressions.makeStatAssign(v,e);
		StatLabel s1 = Expressions.makeStatLabel("mylabel");
		Exp y = Expressions.makeExpNameGlobal("y");
		Exp two = Expressions.makeExpInt(2);
		Stat s2 = Expressions.makeStatAssign(y,two);
		Stat s3 = Expressions.makeStatGoto("mylabel");
		Exp f = Expressions.makeExpNameGlobal("f");
		Exp ae = Expressions.makeExpNameGlobal("a");
		Exp zero = Expressions.makeExpInt(0);
		Exp twohundred = Expressions.makeExpInt(200);
		List<Exp> args = Expressions.makeExpList(zero, twohundred);
		ExpFunctionCall fc = Expressions.makeExpFunCall(ae, args, null);		
		StatAssign s4 = Expressions.makeStatAssign(f,fc);
		StatDo statdo = Expressions.makeStatDo(s2,s3,s4);
		StatBreak statBreak = Expressions.makeStatBreak();
		Block expectedBlock = Expressions.makeBlock(s0,s1,statdo,statBreak);
		Chunk expectedChunk = new Chunk(expectedBlock.firstToken, expectedBlock);
		assertEquals(expectedChunk,c);
	}
	
	@Test
	void testNameChunk1() throws Exception {
		String input = "::slimshady::";
		ASTNode c = parseAndShow(input);	
		StatLabel s1 = Expressions.makeStatLabel("slimshady");
		Block b = Expressions.makeBlock(s1);
		Chunk expected = new Chunk(b.firstToken,b);
		assertEquals(expected,c);	
	}
	
	@Test
	void testBreakChunk1() throws Exception {
		String input = "::slimshady:: break";
		ASTNode c = parseAndShow(input);	
		StatLabel s1 = Expressions.makeStatLabel("slimshady");
		StatBreak statBreak = Expressions.makeStatBreak();
		Block b = Expressions.makeBlock(s1,statBreak);
		Chunk expected = new Chunk(b.firstToken,b);
		assertEquals(expected,c);	
	}
	
	@Test
	void testgotoChunk1() throws Exception {
		String input = "::slimshady:: goto slimshady";
		ASTNode c = parseAndShow(input);	
		StatLabel s1 = Expressions.makeStatLabel("slimshady");
		Stat s3 = Expressions.makeStatGoto("slimshady");
		Block b = Expressions.makeBlock(s1,s3);
		Chunk expected = new Chunk(b.firstToken,b);
		assertEquals(expected,c);	
	}	
	
	@Test
	void testWhileChunk1() throws Exception {
		String input = "while true do ::x::; x=g.a end";
		ASTNode c = parseAndShow(input);
	}	
	
	@Test
	void testRepeatChunk1() throws Exception {
		String input = "repeat ::x::; x=g(a) until true";
		ASTNode c = parseAndShow(input);
	}	
	
	@Test
	void testIfChunk1() throws Exception {
		String input = "if x==y then x=g(a)[b] end";
		ASTNode c = parseAndShow(input);
	}	
	
	@Test
	void testIfChunk2() throws Exception {
		String input = "if x==y then x=g(a)[b] elseif x>y then y=g[b](a) end";
		ASTNode c = parseAndShow(input);
	}	
	
	@Test
	void testIfChunk3() throws Exception {
		String input = "if x==y then x=g(a)[b] elseif x>y then y=g[b](a) else x,y = a.b.c[g] , g[a].b(c) end";
		ASTNode c = parseAndShow(input);
	}	
	
	@Test
	void testForChunk1() throws Exception {
		String input = "for x=true , true do ::x::;x=g[b].a end";
		ASTNode c = parseAndShow(input);
	}	
	
	@Test
	void testForChunk2() throws Exception {
		String input = "for x=true , true, f(x) do ::x::;x=g[b].a end";
		ASTNode c = parseAndShow(input);
	}	
	
	@Test
	void testForChunk3() throws Exception {
		String input = "for x,y,z in f(x),f[a],true do ::x::;goto x end";
		ASTNode c = parseAndShow(input);
	}
	
	@Test
	void testForChunk4() throws Exception {
		String input = "for x in f(x) do ::x::;goto x end";
		ASTNode c = parseAndShow(input);
	}
	
	@Test
	void testFunctionChunk1() throws Exception {
		String input = "function x(a,b,c) y=x(d,f,e) end";
		ASTNode c = parseAndShow(input);
	}
	
	@Test
	void testFunctionChunk2() throws Exception {
		String input = "function x,y,z(a,b,c) y=x(d,f,e) end";
		ASTNode c = parseAndShow(input);
	}
	
	@Test
	void testFunctionChunk3() throws Exception {
		String input = "function x,y,z:w(a,b,c) y=x(d,f,e) end";
		ASTNode c = parseAndShow(input);
	}
	
	@Test
	void testReturnChunk1() throws Exception {
		String input = "function x,y,z:w(a,b,c) y=x(d,f,e) return y; end";
		ASTNode c = parseAndShow(input);
	}
	
	@Test
	void testFunctionChunk4() throws Exception {
		String input = "local function x,y,z:w(a,b,c) y=x(d,f,e) return y; end";
		ASTNode c = parseAndShow(input);
	}

	@Test
	void testLocalMultiAssign1() throws Exception {
		String input = "local a,c=8,f(x)";
		Block b = parseBlockAndShow(input);		
	}

	@Test
	void testLocalMultiAssign2() throws Exception {
		String input = "local a=b";
		Block b = parseBlockAndShow(input);		
	}

	@Test
	void test1() throws Exception {
		String input = "{}";
		Block b = parseBlockAndShow(input);	
	}
}

