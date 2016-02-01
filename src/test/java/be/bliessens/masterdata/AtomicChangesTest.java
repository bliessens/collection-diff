package be.bliessens.masterdata;

import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.*;
import static org.junit.Assert.assertTrue;

public class AtomicChangesTest {

    private static final List<String> ABC = newArrayList("a", "b", "c");
    private static final List<String> ABCD = newArrayList("d", "a", "b", "c");

    @Test
    public void detectAddedListEntry() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(ABC, ABCD);

        assertTrue(changes.size() == 1);
        assertTrue("d".equals(changes.get(0).getSubject()));
    }

    @Test
    public void supportEmptySourceCollection() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(emptyList(), singletonList("D"));

        assertTrue(changes.size() == 1);
        assertTrue("D".equals(changes.get(0).getSubject()));
    }

    @Test
    public void detectDeletedListEntry() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(ABCD, ABC);

        assertTrue(changes.size() == 1);
        assertTrue("d".equals(changes.get(0).getSubject()));
    }

    @Test
    public void supportEmptyTargetCollection() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(singletonList("last"), emptyList());

        assertTrue(changes.size() == 1);
        assertTrue("last".equals(changes.get(0).getSubject()));
    }

    @Test
    public void detectMultipleAddedChanges() throws Exception {

        final HashBag source = new HashBag(ABC);
        source.add("a");

        final List<? extends Change> changes = AtomicChanges.changes(source, new HashBag(ABCD));

        assertTrue(changes.size() == 1);
        assertTrue("d".equals(changes.get(0).getSubject()));
    }

    @Test
    public void detectChangedFieldValue() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(singleton(new ParentClass("id1", "benoit")), singleton(new ParentClass("id1", "bie")));
        assertTrue(changes.size() == 1);

    }

    @Test
    public void detectChangedFieldValueInParentClass() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(singleton(new SubClass("id1", "benoit", 3)), singleton(new SubClass("id1", "bie", 3)));
        assertTrue(changes.size() == 1);

    }

    @Test
    public void detectChangedFieldsClassHierarchy() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(singleton(new SubClass("id1", "benoit", 34)), singleton(new SubClass("id1", "bie", 35)));
        assertTrue(changes.size() == 2);

    }

    private static class ParentClass {

        private String id;
        private String name;

        public ParentClass(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ParentClass parent = (ParentClass) o;

            return new EqualsBuilder()
                    .append(id, parent.id)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(id)
                    .toHashCode();
        }

    }

    private static class SubClass extends ParentClass {

        private int age;

        public SubClass(String id, String name, int age) {
            super(id, name);
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SubClass subClass = (SubClass) o;

            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(age, subClass.age)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .appendSuper(super.hashCode())
                    .append(age)
                    .toHashCode();
        }
    }

}
