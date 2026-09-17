package projectLabo.parser.ast;

import projectLabo.visitors.Visitor;

public class Size extends UnaryOp{

    public Size (Exp exp){
        super(exp);
    }

    @Override
    public <T> T accept(Visitor<T> visitor){
        return visitor.visitSize(exp);
    }
}