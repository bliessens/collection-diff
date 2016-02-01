package be.bliessens.masterdata;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class AtomicChanges {

    AtomicChanges() {
    }

    public static <T> List<? extends Change> changes(final Collection<T> sourceCollection, final Collection<T> targetCollection) {
        List<Change> changes = new LinkedList<>();

        changes.addAll(findEvictedAndNewEntries(sourceCollection, targetCollection));
        changes.addAll(findEntriesWithChangedPropertyValues(sourceCollection, targetCollection));

        return changes;
    }

    private static <T> List<Change> findEvictedAndNewEntries(Collection<T> sourceCollection, Collection<T> targetCollection) {
        final List<Change> changes = new LinkedList<>();
        final Collection<T> union = CollectionUtils.union(sourceCollection, targetCollection);

        for (T item : union) {
            if (!sourceCollection.contains(item)) {
                changes.add(new Change.Addition(item));
            }
            if (!targetCollection.contains(item)) {
                changes.add(new Change.Deletion(item));
            }
        }
        return changes;
    }


    private static <T> List<Change> findEntriesWithChangedPropertyValues(Collection<T> sourceCollection, Collection<T> targetCollection) {
        final List<Change> changes = new LinkedList<>();

        final Collection<T> intersection = CollectionUtils.intersection(sourceCollection, targetCollection);
        if (intersection.isEmpty()) {
            return changes;
        }
        final T next = intersection.iterator().next();
        final Class<?> declaringClass = next.getClass();
        List<Field> fields = getAllFields(new LinkedList<>(), next.getClass());
        try {
            for (T item : intersection) {
                T source = IterableUtils.find(sourceCollection, new EqualToPredicate<>(item));
                T target = IterableUtils.find(targetCollection, new EqualToPredicate<>(item));
                DiffBuilder builder = new DiffBuilder(source, target, ToStringStyle.SHORT_PREFIX_STYLE, false);
                for (Field field : fields) {
                    field.setAccessible(true);
                    builder.append(field.getName(), field.get(source), field.get(target));
                }
                final DiffResult differences = builder.build();
                if (differences.getNumberOfDiffs() > 0) {
                    for (Diff<?> diff : differences.getDiffs()) {
                        changes.add(new Change.ChangeFieldValue(declaringClass.getSimpleName() + "." + diff.getFieldName()));
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
