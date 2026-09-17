package projectLabo.parser.ast;

import projectLabo.visitors.Visitor;
import static java.util.Objects.requireNonNull;

public class While implements Stmt {

    private final Exp exp;
    private final Block body;

    public While(Exp exp, Block body) {
        this.exp = requireNonNull(exp);
        this.body = requireNonNull(body);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitWhile(exp, body);
    }

	@Override
	public String toString() {
		return String.format("%s(%s)", getClass().getSimpleName(), exp, body);
	}
}
