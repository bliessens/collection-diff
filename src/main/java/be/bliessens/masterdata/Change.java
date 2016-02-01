package be.bliessens.masterdata;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class Change {

    private final Object subject;

    protected Change(Object subject) {
        this.subject = subject;
    }

    public Object getSubject() {
        return subject;
    }

    public static Change propertyChange(Object subject, Class theClazz, String fieldName, Object oldValue, Object newValue) {
        return new ChangeFieldValue(subject, theClazz, fieldName, oldValue, newValue);
    }

    public static Change entryDeleted(Object deletedEntry) {
        return new Deletion(deletedEntry);
    }

    public static Change entryAdded(Object deletedEntry) {
        return new Addition(deletedEntry);
    }

    static class Deletion extends Change {
        public Deletion(Object subject) {
            super(subject);
        }
    }

    static class Addition extends Change {
        public Addition(Object subject) {
            super(subject);
        }
    }

    static class ChangeFieldValue extends Change {
        private Class type;
        private String fieldName;
        private Object oldValue, newValue;

        public ChangeFieldValue(Object subject, Class type, String fieldName, Object oldValue, Object newValue) {
            super(subject);
            this.type = type;
            this.fieldName = fieldName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("type", type)
                    .append("fieldName", fieldName)
                    .append("oldValue", oldValue)
                    .append("newValue", newValue)
                    .toString();
        }
    }
}
