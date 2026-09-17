package projectLabo.parser.ast;

import projectLabo.visitors.Visitor;
import static java.util.Objects.requireNonNull;
public class SetEnum implements Exp {

    private final Variable var;
    private final Exp set;
    private final Exp body;

    public SetEnum(Variable var, Exp set, Exp body) {
        this.var = requireNonNull(var);
        this.set = requireNonNull(set);
        this.body = requireNonNull(body);
    }


    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitSetEnum(var, set, body);
    }

	@Override
	public String toString() {
		return String.format("%s(%s)", getClass().getSimpleName(), var, set, body);
	}
}
