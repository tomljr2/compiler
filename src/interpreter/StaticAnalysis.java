package interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap; 

import cop5556fa19.AST.ASTNode;
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


public class StaticAnalysis implements ASTVisitor{

    ArrayList<Block> blockList;
    
	@Override
	public Object visitExpNil(ExpNil expNil, Object arg) throws Exception {
		return null;
	}

	@Override
	public Object visitExpBin(ExpBinary expBin, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitUnExp(ExpUnary unExp, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpInt(ExpInt expInt, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpString(ExpString expString, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpTable(ExpTable expTableConstr, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpList(ExpList expList, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitParList(ParList parList, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFunDef(ExpFunction funcDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitName(Name name, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		
		List<Stat> s = block.stats;
		blockList.add(block);
		for(int i = 0; i < s.size(); i++)
		{
			if(s.get(i).getClass().equals(new StatLabel(null,null,null,-1).getClass()))
			{
				((StatLabel)s.get(i)).setIndex(i);
			}
			s.get(i).visit(this, arg);
		}
		blockList.remove(blockList.size()-1);
		return null;
	}

	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg, Object arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatGoto(StatGoto statGoto, Object arg) throws Exception {		
		((HashMap<Integer,ArrayList<Block>>)(((Object[])arg)[1])).put(statGoto.hashCode(), 
				(ArrayList<Block>)blockList.clone());
		return null;
	}

	@Override
	public Object visitStatDo(StatDo statDo, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statDo.b.visit(this,arg);
		return null;
	}

	@Override
	public Object visitStatWhile(StatWhile statWhile, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatRepeat(StatRepeat statRepeat, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatIf(StatIf statIf, Object arg) throws Exception {
		for(int i = 0; i < statIf.bs.size();i++)
		{
			statIf.bs.get(i).visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitStatFor(StatFor statFor1, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatForEach(StatForEach statForEach, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFuncName(FuncName funcName, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatFunction(StatFunction statFunction, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatLocalFunc(StatLocalFunc statLocalFunc, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatLocalAssign(StatLocalAssign statLocalAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitRetStat(RetStat retStat, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitChunk(Chunk chunk, Object arg) throws Exception {
		blockList=new ArrayList<>();
		chunk.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitFieldExpKey(FieldExpKey fieldExpKey, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFieldNameKey(FieldNameKey fieldNameKey, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFieldImplicitKey(FieldImplicitKey fieldImplicitKey, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpTrue(ExpTrue expTrue, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpFalse(ExpFalse expFalse, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFuncBody(FuncBody funcBody, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpVarArgs(ExpVarArgs expVarArgs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatAssign(StatAssign statAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpTableLookup(ExpTableLookup expTableLookup, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpFunctionCall(ExpFunctionCall expFunctionCall, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitLabel(StatLabel statLabel, Object arg) throws Exception {
		if(((HashMap<Name,Object[]>)(((Object[])arg)[0])).get(statLabel.label)!=null)
			return -1;
		((HashMap<Name,Object[]>)(((Object[])arg)[0])).put(statLabel.label, new Object[] 
						{(ArrayList<Block>)blockList.clone(),statLabel.index});
		return 1;
	}

	@Override
	public Object visitFieldList(FieldList fieldList, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpName(ExpName expName, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
