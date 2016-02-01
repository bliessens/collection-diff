package be.bliessens.masterdata;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.reflect.Field;
import java.util.*;

public final class AtomicChanges {

    AtomicChanges() {
    }

    /**
     * Find the differences between source and target.<br/>
     * The resulting list of <code>Change</code>s should be applied to <em>source</em>  in order to obtain <em>target</em>.
     * <p>
     * The algorithm replies on proper <code>equals()</code> and <code>hashCode()</code> implementations in T to find
     * identical entries in <em>source</em> and <em>target</em>.
     *
     * @param source the reference collection
     * @param target the collection for which we are interested in what is different when compared to <em>source</em>
     * @param fields optional list of field names that should be inspected for changes for all entries appearing in
     *               both <em>source</em> and <em>target</em> collections. Defaults to all declared fields in hierarchy of class T
     * @param <T>    the type of objects to be inspected. Must implement <code>equals()</code> and <code>hashCode()</code> to detect identical
     * @return list of detected <code>Change</code> objects
     */
    public static <T> List<? extends Change> changes(final Collection<T> source, final Collection<T> target, final String... fields) {
        final List<Change> changes = new LinkedList<>();

        changes.addAll(findEvictedAndNewEntries(source, target));
        final List<String> fieldList = (fields == null || fields.length == 0) ? new ArrayList<>() : Arrays.asList(fields);
        changes.addAll(findEntriesWithChangedPropertyValues(source, target, fieldList));

        return changes;
    }

    private static <T> List<Change> findEvictedAndNewEntries(Collection<T> sourceCollection, Collection<T> targetCollection) {
        final List<Change> changes = new LinkedList<>();
        final Collection<T> union = CollectionUtils.union(sourceCollection, targetCollection);

        for (T item : union) {
            if (!sourceCollection.contains(item)) {
                changes.add(Change.entryAdded(item));
            }
            if (!targetCollection.contains(item)) {
                changes.add(Change.entryDeleted(item));
            }
        }
        return changes;
    }


    private static <T> List<Change> findEntriesWithChangedPropertyValues(Collection<T> sourceCollection, Collection<T> targetCollection, Collection<String> includedFields) {
        final List<Change> changes = new LinkedList<>();

        final Collection<T> intersection = CollectionUtils.intersection(sourceCollection, targetCollection);
        if (intersection.isEmpty()) {
            return changes;
        }
        final T next = intersection.iterator().next();
        final Class<?> declaringClass = next.getClass();
        List<Field> fields = getAllFields(new LinkedList<>(), next.getClass());
        if (includedFields.isEmpty()) {
            fields.forEach(field -> includedFields.add(field.getName()));
        }
        try {
            for (T item : intersection) {
                T source = IterableUtils.find(sourceCollection, new EqualToPredicate<>(item));
                T target = IterableUtils.find(targetCollection, new EqualToPredicate<>(item));
                DiffBuilder builder = new DiffBuilder(source, target, ToStringStyle.SHORT_PREFIX_STYLE, false);
                for (Field field : fields) {
                    if (includedFields.contains(field.getName())) {
                        field.setAccessible(true);
                        builder.append(field.getName(), field.get(source), field.get(target));
                    }
                }
                final DiffResult differences = builder.build();
                if (differences.getNumberOfDiffs() > 0) {
                    for (Diff<?> diff : differences.getDiffs()) {
                        changes.add(Change.propertyChange(item, declaringClass, diff.getFieldName(), diff.getLeft(), diff.getRight()));
                    }
                }
            }

        } catch (IllegalArgumentException | IllegalAccessException ex) {
            System.out.println(ex);
        }
        return changes;
    }

    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    private static class EqualToPredicate<T> implements Predicate<T> {
        private final T item;

        public EqualToPredicate(T item) {
            this.item = item;
        }

        @Override
        public boolean evaluate(T object) {
            return item.equals(object);
        }
    }
}
