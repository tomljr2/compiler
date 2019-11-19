package interpreter;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import cop5556fa19.Token;
import cop5556fa19.AST.ASTVisitor;
import cop5556fa19.AST.Block;
import cop5556fa19.AST.Chunk;
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
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.FieldList;
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
import cop5556fa19.AST.StatFor;
import cop5556fa19.AST.StatForEach;
import cop5556fa19.AST.StatFunction;
import cop5556fa19.AST.StatGoto;
import cop5556fa19.AST.StatIf;
import cop5556fa19.AST.StatLabel;
import cop5556fa19.AST.StatLocalAssign;
import cop5556fa19.AST.StatLocalFunc;
import cop5556fa19.AST.StatRepeat;
import cop5556fa19.AST.StatWhile;

public abstract class ASTVisitorAdapter implements ASTVisitor {
	
	@SuppressWarnings("serial")
	public static class StaticSemanticException extends Exception{
		
			public StaticSemanticException(Token first, String msg) {
				super(first.line + ":" + first.pos + " " + msg);
			}
		}
	
	
	@SuppressWarnings("serial")
	public
	static class TypeException extends Exception{

		public TypeException(String msg) {
			super(msg);
		}
		
		public TypeException(Token first, String msg) {
			super(first.line + ":" + first.pos + " " + msg);
		}
		
	}
	
	public abstract List<LuaValue> load(Reader r) throws Exception;

	@Override
	public Object visitExpNil(ExpNil expNil, Object arg) {
		return LuaNil.nil;
	}

	@Override
	public Object visitExpBin(ExpBinary expBin, Object arg) throws Exception {
		throw new UnsupportedOperationException();
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
		List<Object> l = new ArrayList<Object>();
		List<Stat> s = block.stats;
		for(int i = 0; i < s.size(); i++)
		{
			Object t = s.get(i).visit(this, arg);
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
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatDo(StatDo statDo, Object arg) throws Exception {
		return statDo.b.visit(this, arg);
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
			if(!condition.equals(new LuaInt(0)) && !condition.equals(LuaNil.nil))
				return statIf.bs.get(i).visit(this, arg);
		}
		if(statIf.es.size() != statIf.bs.size())	//No else
		{
			return statIf.bs.get(statIf.bs.size()-1).visit(this, arg);
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
		return chunk.block.visit(this, arg);
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
		return new LuaInt(1);
	}

	@Override
	public Object visitExpFalse(ExpFalse expFalse, Object arg) {
		return new LuaInt(0);
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
	public Object visitLabel(StatLabel statLabel, Object ar) {
		throw new UnsupportedOperationException();
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
