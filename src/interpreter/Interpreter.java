package interpreter;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.junit.platform.commons.util.StringUtils;

import java.util.HashMap; 
import java.lang.Math;

import cop5556fa19.Parser;
import cop5556fa19.Scanner;
import cop5556fa19.AST.*;
import static cop5556fa19.Token.Kind.*;

import interpreter.LuaTable.IllegalTableKeyException;
import interpreter.built_ins.print;
import interpreter.built_ins.println;
import interpreter.built_ins.toNumber;


public class Interpreter extends ASTVisitorAdapter{



	
	LuaTable _G; //global environment
	HashMap<Name,Object[]> _L;
	HashMap<Integer,ArrayList<Block>> _GT;
	Boolean inLoop;
	

	/* Instantiates and initializes global environment
	 * 
	 * Initially, the "standard library" routines implemented in Java are loaded.  For this assignment,
	 * this is just print and println.  
	 * 
	 * These functions impl
	 */
	void init_G() {
		_G = new LuaTable();
		_G.put("print", new print());
		_G.put("println", new println());
		_G.put("toNumber", new toNumber());
	}
	
	ASTNode root; //useful for debugging
		
	public Interpreter() {
		init_G();
		_L = new HashMap<>();
		_GT = new HashMap<>();
		inLoop=false;
	}
	

	
	@SuppressWarnings("unchecked")
	public List<LuaValue> load(Reader r) throws Exception {
		Scanner scanner = new Scanner(r); 
		Parser parser = new Parser(scanner);
		Chunk chunk = parser.parse();
		root = chunk;
		//Perform static analysis to prepare for goto.  Uncomment after u
		StaticAnalysis hg = new StaticAnalysis();
		chunk.visit(hg,new Object[] {_L,_GT});	
		//Interpret the program and return values returned from chunk.visit
		List<LuaValue> vals = (List<LuaValue>) chunk.visit(this,_G);
		return vals;
	}
	

	@Override
	public Object visitExpNil(ExpNil expNil, Object arg) {
		return LuaNil.nil;
	}

	@Override
	public Object visitExpBin(ExpBinary expBin, Object arg) throws Exception {
		LuaValue r1 = (LuaValue)expBin.e0.visit(this, arg); 
		LuaValue r2 = (LuaValue)expBin.e1.visit(this, arg); 
		if(expBin.op==KW_or)
		{
			if(r1!=LuaNil.nil && r1!=new LuaBoolean(false))
				return r1;
			else
				return r2;
		}
		if(expBin.op==KW_and)
		{
			if(r1==LuaNil.nil && r1==new LuaBoolean(false))
				return r1;
			else
				return r2;
		}
		
		if (((r1.getClass().equals(LuaInt.class) && r2.getClass().equals(LuaString.class))) && expBin.op==DOTDOT)
			return new LuaString(Integer.toString(((LuaInt)r1).v)+((LuaString)r2).value);

		if (((r2.getClass().equals(LuaInt.class) && r1.getClass().equals(LuaString.class))) && expBin.op==DOTDOT)
			return new LuaString(((LuaString)r1).value+Integer.toString(((LuaInt)r2).v));
		
		if(!r1.getClass().equals(r2.getClass()))
			throw new TypeException(expBin.firstToken,"Incompatible types from binary operator");
		if(r1.getClass().equals(new LuaInt(0).getClass()))
		{
			if(expBin.op==OP_PLUS)
			   return new LuaInt(((LuaInt)r1).v+((LuaInt)r2).v);
			else if(expBin.op==OP_MINUS)
				return new LuaInt(((LuaInt)r1).v-((LuaInt)r2).v);
			else if(expBin.op==OP_TIMES)
				return new LuaInt(((LuaInt)r1).v*((LuaInt)r2).v);
			else if(expBin.op==OP_DIV)
				return new LuaInt(((LuaInt)r1).v/((LuaInt)r2).v);
			else if(expBin.op==OP_MOD)
				return new LuaInt(((LuaInt)r1).v%((LuaInt)r2).v);
			else if(expBin.op==OP_POW)
				return new LuaInt(((LuaInt)r1).v^((LuaInt)r2).v);
			else if(expBin.op==OP_DIVDIV)
				return new LuaInt(Math.floorDiv(((LuaInt)r1).v,((LuaInt)r2).v));
			else if(expBin.op==BIT_AMP)
				return new LuaInt(((LuaInt)r1).v&((LuaInt)r2).v);
			else if(expBin.op==BIT_OR)
				return new LuaInt(((LuaInt)r1).v|((LuaInt)r2).v);
			else if(expBin.op==BIT_XOR)
				return new LuaInt(((LuaInt)r1).v^((LuaInt)r2).v);
			else if(expBin.op==BIT_SHIFTR)
				return new LuaInt(((LuaInt)r1).v>>((LuaInt)r2).v);
			else if(expBin.op==BIT_SHIFTL)
				return new LuaInt(((LuaInt)r1).v<<((LuaInt)r2).v);
			else if(expBin.op==DOTDOT)
				return new LuaInt(Integer.parseInt(Integer.toString(((LuaInt)r1).v)+Integer.toString(((LuaInt)r2).v)));
			else if(expBin.op==REL_EQEQ)
				return new LuaBoolean(((LuaInt)r1).v==((LuaInt)r2).v);
			else if(expBin.op==REL_NOTEQ)
				return new LuaBoolean(((LuaInt)r1).v!=((LuaInt)r2).v);
			else if(expBin.op==REL_GT)
				return new LuaBoolean(((LuaInt)r1).v>((LuaInt)r2).v);
			else if(expBin.op==REL_LT)
				return new LuaBoolean(((LuaInt)r1).v<((LuaInt)r2).v);
			else if(expBin.op==REL_GE)
				return new LuaBoolean(((LuaInt)r1).v>=((LuaInt)r2).v);
			else if(expBin.op==REL_LE)
				return new LuaBoolean(((LuaInt)r1).v<=((LuaInt)r2).v);
		}
		if(r1.getClass().equals(new LuaString("").getClass()))
		{
			if(expBin.op==OP_PLUS)
			   return new LuaString(((LuaString)r1).value+((LuaString)r2).value);
			else if(expBin.op==DOTDOT)
				   return new LuaString(((LuaString)r1).value+((LuaString)r2).value);
			else if(expBin.op==REL_EQEQ)
				   return new LuaBoolean(((LuaString)r1).value.equals(((LuaString)r2).value));
			else if(expBin.op==REL_NOTEQ)
				   return new LuaBoolean(!((LuaString)r1).value.equals(((LuaString)r2).value));
		}
		throw new interpreter.StaticSemanticException(expBin.firstToken,"Unsupported operation");
	}	

	@Override
	public Object visitUnExp(ExpUnary unExp, Object arg) throws Exception {
		LuaValue r = (LuaValue)unExp.e.visit(this, arg); 
		if(unExp.op==KW_not || unExp.op==BIT_XOR)
		{
			if(r.getClass().equals(LuaBoolean.class) || r==LuaNil.nil)
				return new LuaBoolean(true);
			else
				return new LuaBoolean(false);
		}
		if(unExp.op==OP_HASH)
		{
			if(r.getClass().equals(LuaString.class))
				return new LuaInt((((LuaString)r).value).length());
		}
		if(unExp.op==OP_MINUS)
		{
			if(r.getClass().equals(LuaInt.class))
				return new LuaInt(-((LuaInt)r).v);
		}
		throw new interpreter.StaticSemanticException(unExp.firstToken,"Unsupported operation");
	}

	@Override
	public Object visitExpInt(ExpInt expInt, Object arg) {
		return new LuaInt(expInt.v);
	}

	@Override
	public Object visitExpString(ExpString expString, Object arg) {
		return new LuaString(expString.v);
	}

	@Override
	public Object visitExpTable(ExpTable expTableConstr, Object arg) throws Exception {
		LuaTable ret = new LuaTable();
		if(expTableConstr.fields != null)
		   for(int i = 0; i < expTableConstr.fields.size();i++)
		   {
			   Field f = expTableConstr.fields.get(i);
			   Object o = f.visit(this, arg);
			   if(f.getClass().equals(FieldImplicitKey.class))
			      ret.putImplicit((LuaValue)o);
			   else if(f.getClass().equals(FieldExpKey.class) || f.getClass().equals(FieldNameKey.class))
			      ret.put((LuaValue)((Object[])o)[0],(LuaValue)((Object[])o)[1]);
		   }
		return ret;
	}

	@Override
	public Object visitExpList(ExpList expList, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitParList(ParList parList, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitFunDef(ExpFunction funcDec, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitName(Name name, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		List<Stat> s = block.stats;
		Object t=null;
		for(int i = (int)(((Object[])arg)[1]); i < s.size(); i++)
		{
			t = s.get(i).visit(this,((Object[])arg)[0]);
			if(t!=null && t.getClass().equals(new ArrayList<RetStat>().getClass()))
				return t;
			if(t!=null && t.getClass().equals(Integer.class))
			{
				if((Integer)t==-1)
					return t;
			}
			if(s.get(i).getClass().equals(new StatGoto(null,null).getClass()))
				break;
		}
		return null;
	}

	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg, Object arg2) {
		return -1;
	}

	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg) throws Exception {
		return -1;
	}

	@Override
	public Object visitStatGoto(StatGoto statGoto, Object arg) throws Exception {
		try {
			ArrayList<Block> l= (ArrayList<Block>)(_L.get(statGoto.name)[0]);
			ArrayList<Block> gt = _GT.get(statGoto.hashCode());
			
			if(gt.contains(l.get(l.size()-1)))
				return l.get(l.size()-1).visit(this, new Object[] {arg,(int)(_L.get(statGoto.name)[1])});
			
			throw new interpreter.StaticSemanticException(statGoto.firstToken, "Incorrect goto usage");
		}
		catch(NullPointerException e)
		{
			throw new interpreter.StaticSemanticException(statGoto.firstToken,"Incorrect goto usage.");
		}
	}

	@Override
	public Object visitStatDo(StatDo statDo, Object arg) throws Exception {
		Object t = statDo.b.visit(this, new Object[] {arg,0});
		if(t!=null && t.getClass().equals(Integer.class))
			if((Integer)t==-1 && !inLoop)
				return null;
			else if((Integer)t==-1 && inLoop)
				return -1;
		return t;
	}

	@Override
	public Object visitStatWhile(StatWhile statWhile, Object arg) throws Exception {
		Boolean wasInLoop = inLoop;
		inLoop=true;
		LuaBoolean condition = (LuaBoolean)statWhile.e.visit(this, arg);
		while(condition.value)
		{
			Object t=statWhile.b.visit(this, new Object[] {arg,0});
			
			if(t!=null && t.getClass().equals(Integer.class))
			   if((Integer)t==-1)
			      break;
			condition = (LuaBoolean)statWhile.e.visit(this, arg);
		}
		if(!wasInLoop)
			inLoop=false;
		return null;
	}

	@Override
	public Object visitStatRepeat(StatRepeat statRepeat, Object arg) throws Exception {
		Boolean wasInLoop = inLoop;
		inLoop=true;
		LuaBoolean condition = new LuaBoolean(false);
		while(!condition.value)
		{
			Object t=statRepeat.b.visit(this, new Object[] {arg,0});
			
			if(t!=null && t.getClass().equals(Integer.class))
			   if((Integer)t==-1)
			      break;
			condition = (LuaBoolean)statRepeat.e.visit(this, arg);
		}
		if(!wasInLoop)
			inLoop=false;
		return null;
	}

	@Override
	public Object visitStatIf(StatIf statIf, Object arg) throws Exception {
		for(int i = 0; i < statIf.es.size();i++)
		{
			LuaValue condition = (LuaValue)statIf.es.get(i).visit(this, arg);
			if(!condition.equals(new LuaBoolean(false)) && !condition.equals(LuaNil.nil))
				return statIf.bs.get(i).visit(this, new Object[] {arg,0});
		}
		if(statIf.es.size() != statIf.bs.size())	//No else
		{
			return statIf.bs.get(statIf.bs.size()-1).visit(this, new Object[] {arg,0});
		}
		return null;
	}

	@Override
	public Object visitStatFor(StatFor statFor1, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatForEach(StatForEach statForEach, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitFuncName(FuncName funcName, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatFunction(StatFunction statFunction, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatLocalFunc(StatLocalFunc statLocalFunc, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatLocalAssign(StatLocalAssign statLocalAssign, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitRetStat(RetStat retStat, Object arg) throws Exception {
		List<Object> l = new ArrayList<Object>();
		for(int i = 0; i < retStat.el.size();i++)
			l.add(retStat.el.get(i).visit(this, arg));
		return l;
	}

	@Override
	public Object visitChunk(Chunk chunk, Object arg) throws Exception {
		try {
		   return chunk.block.visit(this, new Object[] {arg,0});
		}
		catch(Exception e)
		{
			if(e.getClass()!=interpreter.StaticSemanticException.class && 
			   e.getClass()!=TypeException.class && e.getClass()!=IllegalTableKeyException.class
			   && e.getClass()!=NumberFormatException.class)
			   throw new interpreter.StaticSemanticException(chunk.firstToken, "Error somewhere");
			else
				throw e;
		}
	}

	@Override
	public Object visitFieldExpKey(FieldExpKey fieldExpKey, Object arg) throws Exception {
		return new Object[] {fieldExpKey.key.visit(this, arg),fieldExpKey.value.visit(this, arg)};
	}

	@Override
	public Object visitFieldNameKey(FieldNameKey fieldNameKey, Object arg) throws Exception {
		return new Object[] {new LuaString(fieldNameKey.name.name), 
				(LuaValue)fieldNameKey.exp.visit(this, arg)};
	}
	
	@Override
	public Object visitFieldImplicitKey(FieldImplicitKey fieldImplicitKey, Object arg) throws Exception {
		return fieldImplicitKey.exp.visit(this, arg);
	}

	@Override
	public Object visitExpTrue(ExpTrue expTrue, Object arg) {
		return new LuaBoolean(true);
	}

	@Override
	public Object visitExpFalse(ExpFalse expFalse, Object arg) {
		return new LuaBoolean(false);
	}

	@Override
	public Object visitFuncBody(FuncBody funcBody, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpVarArgs(ExpVarArgs expVarArgs, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatAssign(StatAssign statAssign, Object arg) throws Exception {
		int diff = statAssign.varList.size() - statAssign.expList.size();
		if(diff < 0)
		{
			statAssign = new StatAssign(statAssign.firstToken,
					statAssign.varList,statAssign.expList.subList(0, statAssign.expList.size()+diff));
		}
		else if (diff>0)
		{
			while(diff!=0)
			{
				statAssign.expList.add(ExpNil.expNilConst);
				diff--;
			}
		}
		List<LuaValue[]> temp = new ArrayList<>();
		for(int i = 0; i < statAssign.expList.size();i++)
		{
			LuaValue r = (LuaValue)statAssign.expList.get(i).visit(this, arg);
			Exp s = statAssign.varList.get(i);
			LuaValue l = (LuaValue)statAssign.varList.get(i).visit(this, arg);
			if(s.getClass().equals(ExpName.class))
			   l = new LuaString(((ExpName)s).name);
			else if(s.getClass().equals(ExpTableLookup.class))
			   l = (LuaValue)s.visit(this, new Object[] {arg,r});
			LuaValue[] lr= new LuaValue[]{l,r};
			temp.add(lr);
		}
		for(int i = 0; i < temp.size(); i++)
			((LuaTable)arg).put(temp.get(i)[0], temp.get(i)[1]);
		return ((LuaTable)arg);
	}

	@Override
	public Object visitExpTableLookup(ExpTableLookup expTableLookup, Object arg) throws Exception {
		if(!arg.getClass().equals(Object[].class))
		{
			LuaValue q;
			if(expTableLookup.key.getClass().equals(ExpString.class))
			{
				try
				{
					Integer i = Integer.parseInt(((ExpString)(expTableLookup.key)).v);
					q=new LuaInt(i);
				}
				catch(NumberFormatException e)
				{
					q=new LuaString(((ExpString)(expTableLookup.key)).v);
				}
				catch(NullPointerException e)
				{
					throw new interpreter.StaticSemanticException(expTableLookup.firstToken, "Invalid table lookup");
				}
			}
			else if(expTableLookup.key.getClass().equals(ExpInt.class))
			   q = new LuaInt(((ExpInt)(expTableLookup.key)).v);
			else
				throw new interpreter.StaticSemanticException(expTableLookup.firstToken, "Invalid table lookup");
			LuaTable t = ((LuaTable)((expTableLookup.table).visit(this, arg)));
		    return t.get(q);
		}
		else
		{
			LuaValue q = (LuaValue)(((LuaTable)(((Object[])arg)[0])).get
					((LuaValue)(expTableLookup.key.visit(this, ((Object[])arg)[0]))));
			LuaTable t = ((LuaTable)expTableLookup.table.visit(this, ((Object[])arg)[0]));
		    t.put(q,(LuaValue)((Object[])arg)[1]);
		}
		return null;
	}

	@Override
	public Object visitExpFunctionCall(ExpFunctionCall expFunctionCall, Object arg) throws Exception {
		List<LuaValue> args = new ArrayList<>();
		List<LuaValue> l = new ArrayList<>();
		for(int i = 0; i < expFunctionCall.args.size();i++)
			args.add((LuaValue) (expFunctionCall.args.get(i).visit(this, arg)));
		   if(((JavaFunction)expFunctionCall.f.visit(this, arg)).call(args)!=null)
			   l.addAll(((JavaFunction)expFunctionCall.f.visit(this, arg)).call(args));
		  
		if(l.isEmpty())
		   return LuaNil.nil;
		else
			return l.get(l.size()-1);
	}

	@Override
	public Object visitLabel(StatLabel statLabel, Object arg) {
		return null;
	}

	@Override
	public Object visitFieldList(FieldList fieldList, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpName(ExpName expName, Object arg) throws Exception{
		LuaValue res = ((LuaTable)arg).get(new LuaString(expName.name));
		if (res != LuaNil.nil)
			return res;
		return LuaNil.nil;
	}
	

}
