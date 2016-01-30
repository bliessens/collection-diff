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
    public void supportEmptyCollection() throws Exception {

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
    public void detectDeletedSingleton() throws Exception {

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

        final List<? extends Change> changes = AtomicChanges.changes(singleton(new Parent("id1", "benoit")), singleton(new Parent("id1", "bei")));
        assertTrue(changes.size() == 1);

    }


    @Test
    public void detectChangedFieldValueInSuperClass() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(singleton(new Child("id1", "benoit")), singleton(new Child("id1", "bei")));
        assertTrue(changes.size() == 1);

    }

    private static class Parent {
        private String id;
        private String name;

        public Parent(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Parent parent = (Parent) o;

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

    private static class Child extends Parent {
        public Child(String id, String name) {
            super(id, name);
        }
    }

}
