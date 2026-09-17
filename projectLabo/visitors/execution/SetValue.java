    package projectLabo.visitors.execution;

    import java.util.Objects;
    import java.util.Set;   

    public final class SetValue implements Value {

        private final Set<Value> set;

        public SetValue(Set<Value> set) {
            this.set = Objects.requireNonNull(set);
        }

        @Override
        public Set<Value> toSet(){
            return set;
        }

        @Override
        public boolean equals (Object o){
            if (this == o) return true;
            if (!(o instanceof SetValue other)) return false;
            return set.equals(other.set);
        }

        @Override
        public int hashCode(){
            return set.hashCode();
        }

        @Override
        public String toString(){
            return "setOfSize(" + set.size() +")";
        }
    }