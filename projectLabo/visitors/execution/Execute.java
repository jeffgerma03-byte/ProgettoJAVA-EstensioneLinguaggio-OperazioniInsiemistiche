package projectLabo.visitors.execution;

import java.io.PrintWriter;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import projectLabo.environments.EnvironmentException;
import projectLabo.parser.ast.Block;
import projectLabo.parser.ast.Exp;
import projectLabo.parser.ast.Stmt;
import projectLabo.parser.ast.StmtSeq;
import projectLabo.parser.ast.Variable;
import projectLabo.visitors.Visitor;

public class Execute implements Visitor<Value> {

	private final DynamicEnv env = new DynamicEnv();
	private final PrintWriter printWriter; // output stream used to print values

	public Execute() {
		printWriter = new PrintWriter(System.out, true);
	}

	public Execute(PrintWriter printWriter) {
		this.printWriter = requireNonNull(printWriter);
	}

	// dynamic semantics for programs; no value returned by the visitor

	@Override
	public Value visitLangProg(StmtSeq stmtSeq) {
		try {
			stmtSeq.accept(this);
			// possible runtime errors
			// EnvironmentException: undefined variable
		} catch (EnvironmentException e) {
			throw new InterpreterException(e);
		}
		return null;
	}

	// dynamic semantics for sequences of statements
	// no value returned by the visitor

	@Override
	public Value visitEmptyStmtSeq() {
		return null;
	}

	@Override
	public Value visitNonEmptyStmtSeq(Stmt first, StmtSeq rest) {
		first.accept(this);
		rest.accept(this);
		return null;
	}

	// dynamic semantics for statements; no value returned by the visitor

	@Override
	public Value visitAssertStmt(Exp exp) {
		if(!exp.accept(this).toBool())
			throw new InterpreterException(new AssertionError());
		return null;
	}

	@Override
	public Value visitAssignStmt(Variable var, Exp exp) {
		env.update(var, exp.accept(this));
		return null;
	}

	@Override
	public Value visitIfStmt(Exp exp, Block thenBlock, Block elseBlock) {
		if (exp.accept(this).toBool())
			thenBlock.accept(this);
		else if (elseBlock != null)
			elseBlock.accept(this);
		return null;
	}

	@Override
	public Value visitPrintStmt(Exp exp) {
		printWriter.println(exp.accept(this));
		return null;
	}

	@Override
	public Value visitVarStmt(Variable var, Exp exp) {
		env.dec(var, exp.accept(this));
		return null;
	}

	@Override
	public Value visitBlock(StmtSeq stmtSeq) {
		env.enterLevel();
		stmtSeq.accept(this);
		env.exitLevel();
		return null;
	}

	// dynamic semantics of expressions; a value is returned by the visitor

	@Override
	public IntValue visitAdd(Exp left, Exp right) {
		return new IntValue(left.accept(this).toInt() + right.accept(this).toInt());
	}

	@Override
	public BoolValue visitAnd(Exp left, Exp right) {
		return new BoolValue(left.accept(this).toBool() && right.accept(this).toBool());
	}

	@Override
	public BoolValue visitBoolLiteral(boolean value) {
		return new BoolValue(value);
	}

	@Override
	public BoolValue visitEq(Exp left, Exp right) {
		return new BoolValue(left.accept(this).equals(right.accept(this)));
	}

	@Override
	public Value visitFst(Exp exp) {
		return exp.accept(this).toPair().fstVal();
	}

	@Override
	public IntValue visitIntLiteral(int value) {
		return new IntValue(value);
	}

	@Override
	public IntValue visitMinus(Exp exp) {
		return new IntValue(-exp.accept(this).toInt());
	}

	@Override
	public IntValue visitMul(Exp left, Exp right) {
		return new IntValue(left.accept(this).toInt() * right.accept(this).toInt());
	}

	@Override
	public BoolValue visitNot(Exp exp) {
		return new BoolValue(!exp.accept(this).toBool());
	}

	@Override
	public PairValue visitPairLit(Exp left, Exp right) {
		return new PairValue(left.accept(this), right.accept(this));
	}

	@Override
	public Value visitSnd(Exp exp) {
		return exp.accept(this).toPair().sndVal();
	}

	@Override
	public Value visitVariable(Variable var) {
		return env.lookup(var);
	}
	





   // --- UNION: A ++ B ---
    @Override
    public Value visitUnion(Exp left, Exp right) {
        Set<Value> s1 = left.accept(this).toSet();
        Set<Value> s2 = right.accept(this).toSet();
        Set<Value> res = new HashSet<>(s1);
        res.addAll(s2);
        return new SetValue(res);
    }

    // --- DIFF: A \ B ---
    @Override
    public Value visitDiff(Exp left, Exp right) {
        Set<Value> s1 = left.accept(this).toSet();
        Set<Value> s2 = right.accept(this).toSet();
        Set<Value> res = new HashSet<>(s1);
        res.removeAll(s2);
        return new SetValue(res);
    }

    // --- IN: e in S ---
    @Override
    public Value visitIn(Exp left, Exp right) {
        Value v = left.accept(this);
        Set<Value> s = right.accept(this).toSet();
        return new BoolValue(s.contains(v));
    }

    // --- SIZE: #S ---
    @Override
    public Value visitSize(Exp exp) {
        Set<Value> s = exp.accept(this).toSet();
        return new IntValue(s.size());
    }

    // --- SET LITERAL: {e} ---
    @Override
    public Value visitSetLit(Exp exp) {
        Value v = exp.accept(this);
        Set<Value> res = new HashSet<>();
        res.add(v);
        return new SetValue(res);
    }

    // --- COMPRENSIONE: { for x in S | e } ---
	@Override
	public Value visitSetEnum(Variable var, Exp set, Exp body) {
    	Set<Value> in = set.accept(this).toSet();
    	Set<Value> out = new HashSet<>();

    	for (Value elem : in) {
        	env.enterLevel();      
        	env.dec(var, elem);       
        	out.add(body.accept(this));
        	env.exitLevel(); 
    	}
    	return new SetValue(out);
	}

    // --- WHILE ---
    @Override
    public Value visitWhile(Exp exp, Block body) { 
		while (exp.accept(this).toBool()) {
			body.accept(this);
		}
		return null;
    }
}





