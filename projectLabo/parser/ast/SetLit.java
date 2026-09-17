package projectLabo.parser.ast;

import projectLabo.visitors.Visitor;
import static java.util.Objects.requireNonNull;

public class SetLit implements Exp {

    private final Exp exp;

    public SetLit(Exp exp) {
        this.exp = requireNonNull(exp);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitSetLit(exp);
    }

	@Override
	public String toString() {
		return String.format("%s(%s)", getClass().getSimpleName(), exp);
	}
}
