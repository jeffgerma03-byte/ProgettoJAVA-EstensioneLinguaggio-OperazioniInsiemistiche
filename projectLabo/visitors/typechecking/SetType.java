package projectLabo.visitors.typechecking;

import java.util.Objects;

public final class SetType implements Type {
    private final Type elemType;

    public SetType(Type elemType) {
        this.elemType = Objects.requireNonNull(elemType);
    }

    public Type elemType() {
        return elemType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SetType other)) return false;
        return elemType.equals(other.elemType);
    }

    @Override
    public int hashCode() {
        return elemType.hashCode() * 31 + 7;
    }

    @Override
    public String toString() {
        return elemType.toString() + " set";
    }
}