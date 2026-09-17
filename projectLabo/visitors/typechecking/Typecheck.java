package projectLabo.visitors.typechecking;

import projectLabo.environments.EnvironmentException;
import projectLabo.parser.ast.Block;
import projectLabo.parser.ast.Exp;
import projectLabo.parser.ast.Stmt;
import projectLabo.parser.ast.StmtSeq;
import projectLabo.parser.ast.Variable;
import projectLabo.visitors.Visitor;
import static projectLabo.visitors.typechecking.AtomicType.*;

public class Typecheck implements Visitor<Type> {

	private final StaticEnv env = new StaticEnv();

	// useful to typecheck binary operations where operands must have the same type
	private void checkBinOp(Exp left, Exp right, Type type) {
		type.checkEqual(left.accept(this));
		type.checkEqual(right.accept(this));
	}

	// static semantics for programs; no value returned by the visitor

	@Override
	public Type visitLangProg(StmtSeq stmtSeq) {
		try {
			stmtSeq.accept(this);
		} catch (EnvironmentException e) { // undeclared variable
			throw new TypecheckerException(e);
		}
		return null;
	}

	// static semantics for sequences of statements
	// no value returned by the visitor

	@Override
	public Type visitEmptyStmtSeq() {
		return null;
	}

	@Override
	public Type visitNonEmptyStmtSeq(Stmt first, StmtSeq rest) {
		first.accept(this);
		rest.accept(this);
		return null;
	}
	
	// static semantics for statements; no value returned by the visitor

	@Override
	public Type visitAssertStmt(Exp exp) {
		BOOL.checkEqual(exp.accept(this));
		return null;
	}

	@Override
	public Type visitAssignStmt(Variable var, Exp exp) {
		var found = env.lookup(var);
		found.checkEqual(exp.accept(this));
		return null;
	}

	@Override
	public Type visitIfStmt(Exp exp, Block thenBlock, Block elseBlock) {
		BOOL.checkEqual(exp.accept(this));
		thenBlock.accept(this);
		if (elseBlock != null)
			elseBlock.accept(this);
		return null;
	}
	
	@Override
	public Type visitPrintStmt(Exp exp) {
		exp.accept(this);
		return null;
	}

	@Override
	public Type visitVarStmt(Variable var, Exp exp) {
		env.dec(var, exp.accept(this));
		return null;
	}

	@Override
	public Type visitBlock(StmtSeq stmtSeq) {
		env.enterLevel();
		stmtSeq.accept(this);
		env.exitLevel();
		return null;
	}

	// static semantics of expressions; a type is returned by the visitor

	@Override
	public AtomicType visitAdd(Exp left, Exp right) {
		checkBinOp(left, right, INT);
		return INT;
	}

	@Override
	public AtomicType visitAnd(Exp left, Exp right) {
		checkBinOp(left, right, BOOL);
		return BOOL;
	}

	@Override
	public AtomicType visitBoolLiteral(boolean value) {
		return BOOL;
	}

	@Override
	public AtomicType visitEq(Exp left, Exp right) {
		left.accept(this).checkEqual(right.accept(this));
		return BOOL;
	}

	@Override
	public Type visitFst(Exp exp) {
		return exp.accept(this).toPairType().fstType();
	}

	@Override
	public AtomicType visitIntLiteral(int value) {
		return INT;
	}

	@Override
	public AtomicType visitMinus(Exp exp) {
		INT.checkEqual(exp.accept(this));
		return INT;
	}
	
	@Override
	public AtomicType visitMul(Exp left, Exp right) {
		checkBinOp(left, right, INT);
		return INT;
	}

	@Override
	public AtomicType visitNot(Exp exp) {
		BOOL.checkEqual(exp.accept(this));
		return BOOL;
	}

	@Override
	public PairType visitPairLit(Exp left, Exp right) {
		return new PairType(left.accept(this), right.accept(this));
	}

	@Override
	public Type visitSnd(Exp exp) {
		return exp.accept(this).toPairType().sndType();
	}
	
	@Override
	public Type visitVariable(Variable var) {
		return env.lookup(var);
	}







	// Aggiunte per il progetto



	//WHILE
	@Override	
	public Type visitWhile(Exp exp, Block body) {
    	BOOL.checkEqual(exp.accept(this));
    	body.accept(this);
    	return null;
	}

	//SET
	@Override
	public Type visitSetLit(Exp exp) {
    	Type el = exp.accept(this);
    	return new SetType(el);
	}
	
	//SET ENUM
	@Override
	public Type visitSetEnum(Variable var, Exp setExp, Exp elemExp) {
    	Type R = setExp.accept(this);
    	if (!(R instanceof SetType r))
        	throw new TypecheckerException(R.toString(), "SET");
    	Type elemType = r.elemType();

    	env.enterLevel();
    	env.dec(var, elemType);
    	Type outElem = elemExp.accept(this);
    	env.exitLevel();

    	return new SetType(outElem);
	}

	
	//UNION
	@Override
	public Type visitUnion(Exp left, Exp right) {
    	Type L = left.accept(this);
    	if (!(L instanceof SetType l)) {
        	throw new TypecheckerException(L.toString(), "SET");
    	}
    	Type R = right.accept(this);
    	if (!(R instanceof SetType r)) {
        	throw new TypecheckerException(R.toString(), "SET");
    	}
    	new SetType(l.elemType()).checkEqual(new SetType(r.elemType()));
    	return new SetType(l.elemType());
	}

	
	//DIFF
	@Override
	public Type visitDiff(Exp left, Exp right) {
    	Type L = left.accept(this);
    	if (!(L instanceof SetType l)) {
        	throw new TypecheckerException(L.toString(), "SET");
    	}
    	Type R = right.accept(this);
    	if (!(R instanceof SetType r)) {
        	throw new TypecheckerException(R.toString(), "SET");
    	}
    	new SetType(l.elemType()).checkEqual(new SetType(r.elemType()));
    	return new SetType(l.elemType());
	}

	
	//IN
	@Override
	public Type visitIn(Exp left, Exp right){
		Type tEl = left.accept(this);      // tipo dell’elemento
    	Type R = right.accept(this);    // tipo del set

    	if (!(R instanceof SetType)) {
        	throw new TypecheckerException(R.toString(), "SET");
    	}
    	SetType st = (SetType) R;
    	new SetType(tEl).checkEqual(new SetType(st.elemType()));

    	return AtomicType.BOOL;
	}


	//SIZE
	@Override
	public Type visitSize(Exp exp) {
    	Type found = exp.accept(this);
    	if (!(found instanceof SetType))
        	throw new TypecheckerException(found.toString(), "SET");
    	return INT;
	}







}
