package be.bliessens.masterdata;

public abstract class Change {

    private final Object subject;

    public Change(Object subject) {
        this.subject = subject;
    }

    public Object getSubject() {
        return subject;
    }

    public static class Deletion extends Change {
        public Deletion(Object subject) {
            super(subject);
        }
    }

    public static class Addition extends Change {
        public Addition(Object subject) {
            super(subject);
        }
    }

    public static class ChangeFieldValue extends Change {
        private String typeAndFieldName;

        public ChangeFieldValue(String classAndFieldName) {
            super(classAndFieldName);
            this.typeAndFieldName = classAndFieldName;
        }

        @Override
        public String toString() {
            return this.typeAndFieldName;
        }
    }
}
