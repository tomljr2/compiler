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
import cop5556fa19.AST.Chunk;
import cop5556fa19.AST.Exp;
import cop5556fa19.AST.ExpBinary;
import cop5556fa19.AST.ExpFalse;
import cop5556fa19.AST.ExpFunction;
import cop5556fa19.AST.ExpFunctionCall;
import cop5556fa19.AST.ExpInt;
import cop5556fa19.AST.ExpList;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpNil;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTable;
import cop5556fa19.AST.ExpTableLookup;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpUnary;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.FieldNameKey;
import cop5556fa19.AST.FuncBody;
import cop5556fa19.AST.FuncName;
import cop5556fa19.AST.Name;
import cop5556fa19.AST.ParList;
import cop5556fa19.AST.RetStat;
import cop5556fa19.AST.Stat;
import cop5556fa19.AST.StatAssign;
import cop5556fa19.AST.StatBreak;
import cop5556fa19.AST.StatDo;
import cop5556fa19.AST.StatGoto;
import cop5556fa19.AST.StatFor;
import cop5556fa19.AST.StatForEach;
import cop5556fa19.AST.StatFunction;
import cop5556fa19.AST.StatIf;
import cop5556fa19.AST.StatLabel;
import cop5556fa19.AST.StatLocalAssign;
import cop5556fa19.AST.StatLocalFunc;
import cop5556fa19.AST.StatRepeat;
import cop5556fa19.AST.StatWhile;
import cop5556fa19.AST.TableDeref;
import cop5556fa19.Token.Kind;
import static cop5556fa19.Token.Kind.*;

public class Parser {
	
	@SuppressWarnings("serial")
	class SyntaxException extends Exception {
		Token t;
		
		public SyntaxException(Token t, String message) {
			super(t.line + ":" + t.pos + " " + message);
		}
	}
	
	final Scanner scanner;
	Token t;  //invariant:  this is the next token


	public Parser(Scanner s) throws Exception {
		this.scanner = s;
		t = scanner.getNext(); //establish invariant
	}

	public Chunk parse() throws Exception {
		Chunk chunk = chunk();
		if (!isKind(EOF)) throw new SyntaxException(t, "Parse ended before end of input");
		return chunk;
	}
	
	private Chunk chunk() throws Exception
	{
		Token first = t;
		Block b = block();
		return new Chunk(first,b);
	}

	private Block block() throws Exception{
		Token first = t,r;
		List<Stat> l = new ArrayList<Stat>();
		while(isKind(NAME) || isKind(LPAREN) || isKind(COLONCOLON) || isKind(KW_break) || isKind(KW_goto) || isKind(KW_do) ||
				isKind(KW_while) || isKind(KW_repeat) ||isKind(KW_if) || isKind(KW_for) || isKind(KW_function) || isKind(KW_local)
				|| isKind(SEMI))
		{
			if(isKind(SEMI)) r=match(SEMI);
			else l.add(stat());
		}
		
		if(isKind(KW_return))
		{
			r=match(KW_return);
			if(!isKind(EOF) && !isKind(SEMI))
			{
				l.add(new RetStat(first,explist()));
			}
			if(isKind(SEMI))
				r=match(SEMI);
		}
		
		return new Block(first,l);
	}
	
	private Stat stat() throws Exception
	{
		Token first= t,r;
		Stat ret = null;
		
		if(isKind(NAME) || isKind(LPAREN))
		{
			List<Exp> vl = varlist();
			r=match(ASSIGN);
			List<Exp> el = explist();
			ret = new StatAssign(first,vl,el);
		}
		else if(isKind(COLONCOLON))
		{
			ret = new StatLabel(first,label(),null,-1);	//Change this
		}
		else if(isKind(KW_break))
		{
			r=match(KW_break);
			ret = new StatBreak(first);
		}
		else if(isKind(KW_goto))
		{
			r=match(KW_goto);
			r=match(NAME);
			ret = new StatGoto(first,new Name(first,r.text));
		}
		else if(isKind(KW_do))
		{
			r=match(KW_do);
			Block b = block();
			r=match(KW_end);
			ret = new StatDo(first,b);
		}
		else if(isKind(KW_while))
		{
			r=match(KW_while);
			Exp e0 = exp();
			r=match(KW_do);
			Block b = block();
			r=match(KW_end);
			ret=new StatWhile(first,e0,b);
		}
		else if(isKind(KW_repeat))
		{
			r=match(KW_repeat);
			Block b = block();
			r=match(KW_until);
			Exp e = exp();
			ret = new StatRepeat(first,b,e);
		}
		else if(isKind(KW_if))
		{
			List<Exp> el = new ArrayList<>();
			List<Block> bl = new ArrayList<>();
			r=match(KW_if);
			el.add(exp());
			r=match(KW_then);
			bl.add(block());
			while(isKind(KW_elseif))
			{
				r=match(KW_elseif);
				el.add(exp());
				r=match(KW_then);
				bl.add(block());
			}
			if(isKind(KW_else))
			{
				r=match(KW_else);
				bl.add(block());
			}
			r=match(KW_end);
			ret = new StatIf(first,el,bl);
		}
		else if (isKind(KW_for))
		{
			r=match(KW_for);
			r=match(NAME);
			ExpName n = new ExpName(r.text);
			if(isKind(ASSIGN))
			{
				r=match(ASSIGN);
				Exp e0 = exp();
				r=match(COMMA);
				Exp e1 = exp();
				Exp e2 = null;
				if(isKind(COMMA))
				{
					r=match(COMMA);
					e2=exp();
				}
				r=match(KW_do);
				Block b = block();
				r=match(KW_end);
				ret = new StatFor(first,n,e0,e1,e2,b);
			}
			else if(isKind(COMMA) || isKind(KW_in))
			{
				List<ExpName> nl = namelist(n);
				r=match(KW_in);
				List<Exp> el = explist();
				r=match(KW_do);
				Block b = block();
				r=match(KW_end);
				
				ret = new StatForEach(first,nl,el,b);
			}
		}
		else if(isKind(KW_function))
		{
			r=match(KW_function);
			FuncName fn = funcname();
			FuncBody fb = functionbody();
			ret = new StatFunction(first,fn,fb);	
		}
		else if(isKind(KW_local))
		{
			r=match(KW_local);
			if(isKind(KW_function))
			{
				r=match(KW_function);
				FuncName fn = funcname();
				FuncBody fb = functionbody();
				ret = new StatLocalFunc(first,fn,fb);
			}
			else if (isKind(NAME))
			{
				r=match(NAME);
				List<ExpName> nl = namelist(new ExpName(r.text));
				List<Exp> el = null;
				if(isKind(ASSIGN))
				{
					r=match(ASSIGN);
					el = explist();
				}
				ret = new StatLocalAssign(first,nl,el);
			}
		}
		
		return ret;	
	}
	
	private FuncName funcname() throws Exception
	{
		Token first = t,r;
		List<ExpName> nl = new ArrayList<>();
		ExpName ac = null;
		r=match(NAME);
		nl.add(new ExpName(r.text));
		while(isKind(COMMA))
		{
			r=match(COMMA);
			r=match(NAME);
			nl.add(new ExpName(r.text));
		}
		if(isKind(COLON))
		{
			r=match(COLON);
			r=match(NAME);
			ac = new ExpName(r.text);
		}
		return new FuncName(first,nl,ac);
	}
	
	private List<ExpName> namelist(ExpName n) throws Exception
	{
		Token r;
		List<ExpName> l = new ArrayList<ExpName>();
		l.add(n);
		while(isKind(COMMA))
		{
			r=match(COMMA);
			r=match(NAME);
			l.add(new ExpName(r.text));
		}
		return l;
	}
	
	private Name label() throws Exception
	{
		Token first=t,r;
		r=match(COLONCOLON);
		r=match(NAME);
		Name n = new Name(first,r.text);
		r=match(COLONCOLON);
		return n;
	}
	
	private List<Exp> varlist() throws Exception
	{
		Token r;
		List<Exp> l = new ArrayList<>();
		l.add(var());
		while(isKind(COMMA))
		{
			r=match(COMMA);
			l.add(var());
		}
		return l;
	}
	
	private Exp var() throws Exception
	{
		Token first = t,r;
		Exp e = null;
		Exp e0 = null;
		if(isKind(NAME))
		{
			r=match(NAME);

			e = new ExpName(r);
			if(isKind(LSQUARE) || isKind(DOT) || isKind(LPAREN) || isKind(LCURLY) || isKind(COLON))
				e0 = pt(first,e);
		}
		else if(isKind(LPAREN))
		{
			r=match(LPAREN);
			e = exp();
			r=match(RPAREN);
			if(isKind(LSQUARE) || isKind(DOT) || isKind(LPAREN) || isKind(LCURLY) || isKind(COLON))
			   e0 = pt(first,e);
		}
		if(e0 != null)
		   return e0;
		return e;
		
	}

	Exp exp() throws Exception {
		Token first = t;
		Exp e0 = prec1();
		
		return e0;
	}
	
	Exp prec1() throws Exception
	{
		Exp e0 = prec2();
		Token first = t, op = null;
		Exp e1 = null;
		ExpBinary eb = null;
		while(isKind(KW_or))
		{
			op=match(KW_or);
			e1=prec2();
			eb = new ExpBinary(first,e0,op,e1);
			e0 = eb;
		}

		if(eb==null)
			return e0;
		else 
			return eb;
	}
	
	Exp prec2() throws Exception
	{
		Exp e0 = prec3();
		Token first = t, op = null;
		Exp e1 = null;
		ExpBinary eb = null;
		while(isKind(KW_and))
		{
			op=match(KW_and);
			e1=prec3();
			eb = new ExpBinary(first,e0,op,e1);
			e0 = eb;
		}

		if(eb==null)
			return e0;
		else 
			return eb;
	}
	
	Exp prec3() throws Exception
	{
		Exp e0 = prec4();
		Token first = t, op = null;
		Exp e1 = null;
		ExpBinary eb = null;
		while(isRel())
		{
			op=match(t.kind);
			e1=prec4();
			eb = new ExpBinary(first,e0,op,e1);
			e0 = eb;
		}

		if(eb==null)
			return e0;
		else 
			return eb;
	}
	
	Exp prec4() throws Exception
	{
		Exp e0 = prec5();
		Token first = t, op = null;
		Exp e1 = null;
		ExpBinary eb = null;
		while(isKind(BIT_OR))
		{
			op=match(BIT_OR);
			e1=prec5();
			eb = new ExpBinary(first,e0,op,e1);
			e0 = eb;
		}

		if(eb==null)
			return e0;
		else 
			return eb;
	}
	
	Exp prec5() throws Exception
	{
		Exp e0 = prec6();
		Token first = t, op = null;
		Exp e1 = null;
		ExpBinary eb = null;
		while(isKind(BIT_XOR))
		{
			op=match(BIT_XOR);
			e1=prec6();
			eb = new ExpBinary(first,e0,op,e1);
			e0 = eb;
		}

		if(eb==null)
			return e0;
		else 
			return eb;
	}
	
	Exp prec6() throws Exception
	{
		Exp e0 = prec7();
		Token first = t, op = null;
		Exp e1 = null;
		ExpBinary eb = null;
		while(isKind(BIT_AMP))
		{
			op=match(BIT_AMP);
			e1=prec7();
			eb = new ExpBinary(first,e0,op,e1);
			e0 = eb;
		}

		if(eb==null)
			return e0;
		else 
			return eb;
	}
	
	Exp prec7() throws Exception
	{
		Exp e0 = prec8();
		Token first = t, op = null;
		Exp e1 = null;
		ExpBinary eb = null;
		while(isShift())
		{
			op=match(t.kind);
			e1=prec8();
			eb = new ExpBinary(first,e0,op,e1);
			e0 = eb;
		}

		if(eb==null)
			return e0;
		else 
			return eb;
	}
	
	Exp prec8() throws Exception
	{
		Exp e0=prec9();
		Exp e1=null;
		ExpBinary eb = null;
		Token first = t, op = null;
		if(isKind(DOTDOT))
		{
			op=match(DOTDOT);
			e1 = prec8();
			eb = new ExpBinary(first,e0,op,e1);
		}

		if(eb==null)
			return e0;
		else 
			return eb;
	}
	
	Exp prec9() throws Exception
	{
		Exp e0 = prec10();
		Token first = t, op = null;
		Exp e1 = null;
		ExpBinary eb = null;
		while(isAddOp())
		{
			op=match(t.kind);
			e1=prec10();
			eb = new ExpBinary(first,e0,op,e1);
			e0 = eb;
		}

		if(eb==null)
			return e0;
		else 
			return eb;
	}
	
	Exp prec10() throws Exception
	{
		Exp e0 = prec11();
		Token first = t, op = null;
		Exp e1 = null;
		ExpBinary eb = null;
		while(isMultOp())
		{
			op=match(t.kind);
			e1=prec11();
			eb = new ExpBinary(first,e0,op,e1);
			e0 = eb;
		}

		if(eb==null)
			return e0;
		else 
			return eb;
	}
	
	Exp prec11() throws Exception
	{
		Exp e0 = null;
		Token first = t, op = null;
		Exp eu = null;
		if(!isUnaryOp())
			e0=prec12();
		else
		{
			op=match(t.kind);
			e0=prec12();
			eu = new ExpUnary(first,op.kind,e0);
		}

		if(eu==null)
			return e0;
		else 
			return eu;
		
	}
	
	Exp prec12() throws Exception
	{
		Exp e0=getNextExp();
		Exp e1=null;
		ExpBinary eb = null;
		Token first = t, op = null;
		if(isKind(OP_POW))
		{
			op=match(OP_POW);
			e1 = prec12();
			eb = new ExpBinary(first,e0,op,e1);
		}

		if(eb==null)
			return e0;
		else 
			return eb;
	}
	
	private boolean isRel()
	{
		return (isKind(REL_LT) || isKind(REL_GT) ||
				isKind(REL_LE) || isKind(REL_GE) ||
				isKind(REL_NOTEQ) || isKind(REL_EQEQ));
	}
	
	private boolean isShift()
	{
		return (isKind(BIT_SHIFTL) || isKind(BIT_SHIFTR));
	}
	
	private boolean isAddOp()
	{
		return (isKind(OP_PLUS) || isKind(OP_MINUS));
	}
	
	private boolean isMultOp()
	{
		return (isKind(OP_TIMES) || isKind(OP_DIV) ||
				isKind(OP_DIVDIV) || isKind(OP_MOD));
	}
	Exp getNextExp() throws Exception
	{
		Token first = t;
		Exp e0=null;
		while (isKind(KW_or)) {
			Token op = consume();
			Exp e1 = andExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
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
		
		return e0;
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
			if(!isKind(RCURLY))
			   l.add(field());
		}
		
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
			if(	isKind(ASSIGN))
			{
				r=match(ASSIGN);
				Exp e0 = exp();
				f = new FieldNameKey(first,new Name(name,name.text),e0);
			}
			else if(isKind(LPAREN))
			{
				f = new FieldImplicitKey(first,pt(first,new ExpName(name)));
			}
			else
			{
				f = new FieldImplicitKey(first,new ExpName(name));
			}
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
		Exp e0 = null;
		if(isKind(NAME))
		{
			r=match(NAME);

			e = new ExpName(r);
			e0 = pt(first,e);
		}
		else if(isKind(LPAREN))
		{
			r=match(LPAREN);
			e = exp();
			r=match(RPAREN);
			e0 = pt(first,e);
		}
		if(e0 != null)
		   return e0;
		return e;
	}
	
	private Exp pt(Token f, Exp name) throws Exception
	{
		Token first = t,r;
		Exp e = null;
		if(isKind(LSQUARE))
		{
			r = match(LSQUARE);
			Exp e0 = new ExpString(exp().firstToken);
			r = match(RSQUARE);
			e0 = new ExpTableLookup(f,name,e0);
			Exp e1 = pt(first,e0);
			if(e1 != null)
				e = e1;
			else
				e =e0;
		}
		else if(isKind(DOT))
		{
			r=match(DOT);
			r=match(NAME);
			Exp e0 = new ExpTableLookup(f,name,new ExpString(r));
			Exp e1 = pt(first,e0);
			if(e1 != null)
				e = e1;
			else
				e =e0;
		}
		else if(isKind(LPAREN) || isKind(LCURLY) || isKind(STRINGLIT))
		{
			Exp e0 = new ExpFunctionCall(f,name,args());
			Exp e1 = pt(first,e0);
			if(e1 != null)
			   e = e1;
			else
				e =e0;
		}
		else if(isKind(COLON))
		{
			r=match(COLON);
			r=match(NAME);
			Exp n1 = new ExpString(r);
			List<Exp> l = args();
			l.add(0,name);
			Exp e0 = new ExpFunctionCall(f,new ExpTableLookup(f,name,n1),l);
			Exp e1 = pt(first,n1);
			if(e1 != null)
			   e = e1;
			else
				e =e0;
		}
		return e;
	}
	
	private List<Exp> args() throws Exception
	{
		Token first = t,r;
		List<Exp> l = new ArrayList<>();
		if(isKind(LPAREN))
		{
			r=match(LPAREN);
			if(!isKind(RPAREN))
			{
				l = explist();
			}
			r=match(RPAREN);
		}
		else if(isKind(LCURLY))
		{
			l.add(tableconstructor());
		}
		else
		{
			r=match(t.kind);
			l.add(new ExpString(r));
		}
		
		return l;
	}
	
	private List<Exp> explist() throws Exception
	{
		Token r;
		List<Exp> l = new ArrayList<Exp>();
		l.add(exp());
		while(isKind(COMMA))
		{
			r=match(COMMA);
			l.add(exp());
		}
		return l;
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
		List<Name> l = new ArrayList<>();
		if(isKind(NAME))
		{
			l=namelist(hasVar);
		}
		if(isKind(DOTDOTDOT))
		{ 
			Token r=match(DOTDOTDOT);
			hasVar=true;
		}
		return new ParList(first,l,hasVar);
	}
	
	private List<Name> namelist(boolean hasVar) throws Exception
	{
		List<Name> l = new ArrayList<Name>();
		Token r = match(NAME);
		l.add(new Name(r,r.text));
		while(isKind(COMMA))
		{
			r=match(COMMA);
			if(isKind(DOTDOTDOT)) break;
			r=match(NAME);
			l.add(new Name(r,r.text));
		}
		
		return l;
	}
	
	private Exp andExp() throws Exception{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("andExp");  //I find this is a more useful placeholder than returning null.
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
