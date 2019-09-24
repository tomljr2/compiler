/**
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5556fa19.AST.Block;
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
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.FieldNameKey;
import cop5556fa19.AST.FuncBody;
import cop5556fa19.AST.Name;
import cop5556fa19.AST.ParList;
import cop5556fa19.Token.Kind;
import static cop5556fa19.Token.Kind.*;

public class ExpressionParser {
	
	@SuppressWarnings("serial")
	class SyntaxException extends Exception {
		Token t;
		
		public SyntaxException(Token t, String message) {
			super(t.line + ":" + t.pos + " " + message);
		}
	}
	
	final Scanner scanner;
	Token t;  //invariant:  this is the next token


	ExpressionParser(Scanner s) throws Exception {
		this.scanner = s;
		t = scanner.getNext(); //establish invariant
	}


	Exp exp() throws Exception {
		Token first = t;
		Exp e0=null;
		/*while (isKind(KW_or)) {
			Token op = consume();
			Exp e1 = andExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}*/
		Token r;
		if(isKind(KW_nil))
		{
			r=match(KW_nil);
			e0 = new ExpNil(first);
		}
		else if(isKind(KW_false))
		{
			r=match(KW_false);
			e0 = new ExpFalse(first);
		}
		else if(isKind(KW_true))
		{
			r=match(KW_true);
			e0 = new ExpTrue(first);
		}
		else if(isKind(INTLIT))
		{
			r=match(INTLIT);
			e0 = new ExpInt(first);
		}
		else if(isKind(STRINGLIT))
		{
			r=match(STRINGLIT);
			e0 = new ExpString(first);
		}
		else if(isKind(DOTDOTDOT))
		{
			r=match(DOTDOTDOT);
			e0 = new ExpVarArgs(first);
		}
		else if(isKind(KW_function))
		{ e0 = functiondef(); }
		else if(isKind(NAME) || isKind(LPAREN))
		{ e0 = prefixexp(); }
		else if(isKind(LCURLY))
		{ e0 = tableconstructor(); }
		else if(isUnaryOp())
		{
			Kind k = unop();
			Exp e = exp();
			e0 = new ExpUnary(first,k,e);
		}
		else
		{ error(first,first.text); }
		
		// This will check after an expression to see if it is also a
		// right associative binary expression
		if(isKind(DOTDOT) || isKind(OP_POW))
		{ return binop(e0); }
		
		return e0;
	}
	
	private ExpBinary binop(Exp e0) throws Exception
	{
		Token first = t,op=null;
		Exp e1 = null;

		if(isKind(OP_POW)) { op = match(OP_POW); }
		/*else if(isUnaryOp())
		{
			if(isKind(KW_not))
				op = match(KW_not);
			else if(isKind(OP_HASH))
				op = match(OP_HASH);
			else if(isKind(OP_MINUS))
				op = match(OP_MINUS);
			else if(isKind(BIT_XOR))
				op = match(BIT_XOR);
		}*/
		else if(isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_DIVDIV) || isKind(OP_MOD))
		{
			if(isKind(OP_TIMES))
				op = match(OP_TIMES);
			else if(isKind(OP_DIV))
				op = match(OP_DIV);
			else if(isKind(OP_DIVDIV))
				op = match(OP_DIVDIV);
			else if(isKind(OP_MOD))
				op = match(OP_MOD);
		}
		else if(isKind(OP_PLUS) || isKind(OP_MINUS))
		{
			if(isKind(OP_PLUS))
				op = match(OP_PLUS);
			else if(isKind(OP_MINUS))
				op = match(OP_MINUS);
		}
		else if(isKind(DOTDOT)) { op = match(DOTDOT); }
		else if(isKind(BIT_SHIFTL) || isKind(BIT_SHIFTR))
		{
			if(isKind(BIT_SHIFTL))
				op = match(BIT_SHIFTL);
			else if(isKind(BIT_SHIFTR))
				op = match(BIT_SHIFTR);
		}
		else if(isKind(BIT_AMP)) { op = match(BIT_AMP); }
		else if(isKind(BIT_XOR)) { op = match(BIT_XOR); }
		else if(isKind(BIT_OR)) { op = match(BIT_OR); }
		else if(isKind(REL_LT) || isKind(REL_GT) || isKind(REL_LE) || isKind(REL_GE) ||
				isKind(REL_NOTEQ) || isKind(REL_EQEQ))
		{
			if(isKind(REL_LT))
				op = match(REL_LT);
			else if(isKind(REL_GT))
				op = match(REL_GT);
			else if(isKind(REL_LE))
				op = match(REL_LE);
			else if(isKind(REL_GE))
				op = match(REL_GE);
			else if(isKind(REL_NOTEQ))
				op = match(REL_NOTEQ);
			else if(isKind(REL_EQEQ))
				op = match(REL_EQEQ);
		}
		else if(isKind(KW_and)) { op = match(KW_and); }
		else if(isKind(KW_or)) { op = match(KW_or); }
		
		e1=exp();
		
		return new ExpBinary(first,e0,op,e1);
	}
	
	private Kind unop() throws Exception
	{
		Token r;
		Kind k=null;
		if(isKind(OP_MINUS))
		{
			r = match(OP_MINUS);
			k = OP_MINUS;
		}
		else if(isKind(KW_not))
		{
			r = match(KW_not);
			k = KW_not;
		}
		else if(isKind(OP_HASH))
		{
			r = match(OP_HASH);
			k = OP_HASH;
		}
		else if(isKind(BIT_XOR))
		{
			r = match(BIT_XOR);
			k = BIT_XOR;
		}
		return k;
	}
	
	// Some helper functions
	private boolean isUnaryOp()
	{
		return (isKind(OP_MINUS) || isKind(KW_not) ||
				isKind(OP_HASH) || isKind(BIT_XOR));
	}
	
	private boolean isBinaryOp()
	{
		return (isKind(OP_PLUS) || isKind(OP_MINUS) ||
				isKind(OP_TIMES) || isKind(OP_DIV) ||
				isKind(OP_DIVDIV) || isKind(OP_POW) ||
				isKind(OP_MOD) || isKind(BIT_AMP) ||
				isKind(BIT_XOR) || isKind(BIT_OR) ||
				isKind(BIT_SHIFTR) || isKind(BIT_SHIFTL) ||
				isKind(DOTDOT) || isKind(REL_LT) ||
				isKind(REL_LE) || isKind(REL_GT) ||
				isKind(REL_GE) || isKind(REL_EQEQ) ||
				isKind(REL_NOTEQ) || isKind(KW_and) ||
				isKind(KW_or));
	}
	
	private ExpTable tableconstructor() throws Exception
	{
		Token first = t;
		Token r = match(LCURLY);
		List<Field> l = null;
		if(!isKind(RCURLY))
		{
			l=fieldlist();
		}
		r = match(RCURLY);
		
		return new ExpTable(first,l);
	}
	
	private List<Field> fieldlist() throws Exception
	{
		Token first = t;
		List<Field> l = new ArrayList<Field>();
		
		l.add(field());
		
		while(isKind(COMMA) || isKind(SEMI))
		{
			fieldsep();
			l.add(field());
		}
		
		if(isKind(COMMA) || isKind(SEMI))
			fieldsep();
		
		return l;
	}
	
	private void fieldsep() throws Exception
	{
		Token r;
		if(isKind(COMMA))
			r=match(COMMA);
		else if(isKind(SEMI))
			r=match(SEMI);
	}
	
	private Field field() throws Exception
	{
		Token first = t,r;
		Field f=null;
		if(isKind(LSQUARE))
		{
			r=match(LSQUARE);
			Exp e0 = exp();
			r=match(RSQUARE);
			r=match(ASSIGN);
			Exp e1 = exp();
			f = new FieldExpKey(first,e0,e1);
		}
		else if(isKind(NAME))
		{
			Token name=match(NAME);
			r=match(ASSIGN);
			Exp e0 = exp();
			f = new FieldNameKey(first,new Name(name,name.text),e0);
		}
		else
		{
			Exp e0 = exp();
			f = new FieldImplicitKey(first,e0);
		}
		return f;
	}
	
	private Exp prefixexp() throws Exception
	{
		Token first = t;
		Token r;
		Exp e = null;
		if(isKind(NAME))
		{
			r=match(NAME);
			e = new ExpName(first);
		}
		else if(isKind(LPAREN))
		{
			r=match(LPAREN);
			e = exp();
			r=match(RPAREN);
		}
		return e;
	}

	private ExpFunction functiondef() throws Exception
	{
		Token first = t;
		Token r=match(KW_function);
		FuncBody fb = functionbody();
		return new ExpFunction(first,fb);
	}
	
	private FuncBody functionbody() throws Exception
	{
		Token first = t;
		Token r = match(LPAREN);
		ParList pl=null;
		if(!isKind(RPAREN))
		{ pl=parlist(false); }
		r=match(RPAREN);
		Block b = block();
		r=match(KW_end);
		
		return new FuncBody(first,pl,b);
	}
	
	private ParList parlist(boolean hasVar) throws Exception
	{
		Token first = t;
		List<Name> l = null;
		if(isKind(NAME))
		{
			l=namelist(hasVar);
		}
		else if(isKind(DOTDOTDOT))
		{ Token r=match(DOTDOTDOT); }
		return new ParList(first,l,hasVar);
	}
	
	private List<Name> namelist(boolean hasVar) throws Exception
	{
		List<Name> l = new ArrayList<Name>();
		Token r = match(NAME);
		l.add(new Name(r,r.text));
		if(!hasVar)
			return l;
		while(isKind(COMMA))
		{
			r=match(COMMA);
			r=match(NAME);
			l.add(new Name(r,r.text));
		}
		
		return l;
	}
	
	private Exp andExp() throws Exception{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("andExp");  //I find this is a more useful placeholder than returning null.
	}


	private Block block() {
		return new Block(null);  //this is OK for Assignment 2
	}


	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}

	/**
	 * @param kind
	 * @return
	 * @throws Exception
	 */
	Token match(Kind kind) throws Exception {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		error(kind);
		return null; // unreachable
	}

	/**
	 * @param kind
	 * @return
	 * @throws Exception
	 */
	Token match(Kind... kinds) throws Exception {
		Token tmp = t;
		if (isKind(kinds)) {
			consume();
			return tmp;
		}
		StringBuilder sb = new StringBuilder();
		for (Kind kind1 : kinds) {
			sb.append(kind1).append(kind1).append(" ");
		}
		error(kinds);
		return null; // unreachable
	}

	Token consume() throws Exception {
		Token tmp = t;
        t = scanner.getNext();
		return tmp;
	}
	
	void error(Kind... expectedKinds) throws SyntaxException {
		String kinds = Arrays.toString(expectedKinds);
		String message;
		if (expectedKinds.length == 1) {
			message = "Expected " + kinds + " at " + t.line + ":" + t.pos;
		} else {
			message = "Expected one of" + kinds + " at " + t.line + ":" + t.pos;
		}
		throw new SyntaxException(t, message);
	}

	void error(Token t, String m) throws SyntaxException {
		String message = m + " at " + t.line + ":" + t.pos;
		throw new SyntaxException(t, message);
	}
	


}
