package interpreter;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap; 
import java.lang.Math;

import cop5556fa19.Parser;
import cop5556fa19.Scanner;
import cop5556fa19.AST.*;
import static cop5556fa19.Token.Kind.*;
import interpreter.built_ins.print;
import interpreter.built_ins.println;
import interpreter.built_ins.toNumber;


public class Interpreter extends ASTVisitorAdapter{



	
	LuaTable _G; //global environment
	HashMap<Name,Object[]> _H;

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
		_H = new HashMap<>();
	}
	

	
	@SuppressWarnings("unchecked")
	public List<LuaValue> load(Reader r) throws Exception {
		Scanner scanner = new Scanner(r); 
		Parser parser = new Parser(scanner);
		Chunk chunk = parser.parse();
		root = chunk;
		//Perform static analysis to prepare for goto.  Uncomment after u
		StaticAnalysis hg = new StaticAnalysis();
		chunk.visit(hg,_H);	
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
		}
		if(r1.getClass().equals(new LuaString("").getClass()))
		{
			if(expBin.op==OP_PLUS)
			   return new LuaString(((LuaString)r1).value+((LuaString)r2).value);
			if(expBin.op==DOTDOT)
				   return new LuaString(((LuaString)r1).value+((LuaString)r2).value);
		}
		throw new interpreter.StaticSemanticException(expBin.firstToken,"Unsupported operation");
	}	

	@Override
	public Object visitUnExp(ExpUnary unExp, Object arg) throws Exception {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
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
		for(int i = (int)(((Object[])arg)[1]); i < s.size(); i++)
		{
			Object t = s.get(i).visit(this,((Object[])arg)[0]);
			if(t!=null && t.getClass().equals(new ArrayList<RetStat>().getClass()))
				return t;
		}
		return null;
	}

	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatGoto(StatGoto statGoto, Object arg) throws Exception {
		try {
			Object[] l= _H.get(statGoto.name);
			return ((Block)(l[0])).visit(this, new Object[]{arg,l[1]});
		}
		catch(NullPointerException e)
		{
			throw new StaticSemanticException(statGoto.firstToken,"Incorrect goto usage.");
		}
	}

	@Override
	public Object visitStatDo(StatDo statDo, Object arg) throws Exception {
		return statDo.b.visit(this, new Object[] {arg,0});
	}

	@Override
	public Object visitStatWhile(StatWhile statWhile, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatRepeat(StatRepeat statRepeat, Object arg) throws Exception {
		throw new UnsupportedOperationException();
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
		return chunk.block.visit(this, new Object[] {arg,0});
	}

	@Override
	public Object visitFieldExpKey(FieldExpKey fieldExpKey, Object object) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitFieldNameKey(FieldNameKey fieldNameKey, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Object visitFieldImplicitKey(FieldImplicitKey fieldImplicitKey, Object arg) throws Exception {
		throw new UnsupportedOperationException();
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
			LuaValue l = new LuaString(((ExpName)statAssign.varList.get(i)).name);
			LuaValue[] lr= new LuaValue[]{l,r};
			temp.add(lr);
		}
		for(int i = 0; i < temp.size(); i++)
			((LuaTable)arg).put(temp.get(i)[0], temp.get(i)[1]);
		return null;
	}

	@Override
	public Object visitExpTableLookup(ExpTableLookup expTableLookup, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpFunctionCall(ExpFunctionCall expFunctionCall, Object arg) throws Exception {
		throw new UnsupportedOperationException();
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
