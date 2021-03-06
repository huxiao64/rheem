package org.qcri.rheem.core.util;

import java.util.*;

/**
 * This utility maintains canonical sets of objects.
 */
public class Canonicalizer<T> implements Set<T> {

    /**
     * Contains the objects.
     */
    private final Map<T, T> entries = new HashMap<>();

    public Canonicalizer() {
    }

    public Canonicalizer(Iterable<? extends T> objs) {
        this();
        this.addAll(objs);
    }

    public Canonicalizer(T... objs) {
        this();
        for (T obj : objs) {
            this.add(obj);
        }
    }


    /**
     * Add the given element if it is not contained in this instance, yet.
     *
     * @param obj the element to be added potentially
     * @return {@code obj} if it was added, otherwise the existing element
     */
    public T getOrAdd(T obj) {
        final T existingObj = this.entries.putIfAbsent(obj, obj);
        return existingObj == null ? obj : existingObj;
    }

    public void addAll(Iterable<? extends T> objs) {
        objs.forEach(this::getOrAdd);
    }

    @Override
    public int size() {
        return this.entries.size();
    }

    @Override
    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.entries.containsKey(o);
    }

    @Override
    public Iterator<T> iterator() {
        return this.entries.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return this.entries.keySet().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return this.entries.keySet().toArray(a);
    }

    @Override
    public boolean add(T t) {
        return this.entries.putIfAbsent(t, t) == null;
    }

    @Override
    public boolean remove(Object o) {
        return this.entries.remove(o) != null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.entries.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return c.stream().map(this::add).reduce(false, Boolean::logicalOr);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new RuntimeException("#retainAll() is not implemented");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.removeAll(c);
    }

    @Override
    public void clear() {
        this.entries.clear();
    }

    @Override
    public String toString() {
        return this.entries.keySet().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Canonicalizer<?> that = (Canonicalizer<?>) o;
        return Objects.equals(this.entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entries);
    }
}
