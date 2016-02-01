package be.bliessens.masterdata;

import org.junit.Test;

public class ChangeTest {

    @Test
    public void propertyChange() throws Exception {
        final AtomicChangesTest.SubClass subject = new AtomicChangesTest.SubClass("id2", "albert", 12);
        final Change change = Change.propertyChange(subject, AtomicChangesTest.SubClass.class, "name", "albert", "Albert");
        System.out.println(change);
    }
}