package be.bliessens.masterdata;

import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;

public class AtomicChangesTest {

    private static final List<String> ABC = newArrayList("a", "b", "c");
    private static final List<String> ABCD = newArrayList("d", "a", "b", "c");

    @Test
    public void detectAddedListEntry() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(ABC, ABCD);

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0)).isInstanceOf(Change.Addition.class);
        assertThat(changes.get(0).getSubject()).isEqualTo("d");
    }

    @Test
    public void supportEmptySourceCollection() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(emptyList(), singletonList("D"));

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getSubject()).isEqualTo("D");
    }

    @Test
    public void detectDeletedListEntry() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(ABCD, ABC);

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getSubject()).isEqualTo("d");
    }

    @Test
    public void supportEmptyTargetCollection() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(singletonList("last"), emptyList());

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getSubject()).isEqualTo("last");
    }

    @Test
    public void detectMultipleAddedChanges() throws Exception {

        final HashBag source = new HashBag(ABC);
        source.add("a");

        final List<? extends Change> changes = AtomicChanges.changes(source, new HashBag(ABCD));

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getSubject()).isEqualTo("d");
    }

    @Test
    public void detectChangedFieldValue() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(singleton(new ParentClass("id1", "benoit")), singleton(new ParentClass("id1", "bie")));
        assertThat(changes).hasSize(1);

    }

    @Test
    public void detectChangedFieldValueInParentClass() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(singleton(new SubClass("id1", "benoit", 3)), singleton(new SubClass("id1", "bie", 3)));
        assertThat(changes).hasSize(1);

    }

    @Test
    public void detectChangedFieldsInClassHierarchy() throws Exception {

        final List<? extends Change> changes = AtomicChanges.changes(singleton(new SubClass("id1", "benoit", 34)), singleton(new SubClass("id1", "bie", 35)));
        assertThat(changes).hasSize(2);

    }

    @Test
    public void inspectReferencedFieldsOnly() throws Exception {

        List<? extends Change> changes = AtomicChanges.changes(singleton(new SubClass("id1", "benoit", 34)), singleton(new SubClass("id1", "bie", 35)), "name");
        assertThat(changes).hasSize(1);
        assertThat(Change.ChangeFieldValue.class.isInstance(changes.get(0)));

        changes = AtomicChanges.changes(singleton(new SubClass("id1", "benoit", 34)), singleton(new SubClass("id1", "bie", 35)), "name", "age");
        assertThat(changes).hasSize(2);

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

//        @Override
//        public boolean equals(Object o) {
//            if (this == o) {
//                return true;
//            }
//
//            if (o == null || getClass() != o.getClass()) {
//                return false;
//            }
//
//            SubClass subClass = (SubClass) o;
//
//            return new EqualsBuilder()
//                    .appendSuper(super.equals(o))
//                    .append(age, subClass.age)
//                    .isEquals();
//        }
//
//        @Override
//        public int hashCode() {
//            return new HashCodeBuilder(17, 37)
//                    .appendSuper(super.hashCode())
//                    .append(age)
//                    .toHashCode();
//        }
    }

}
