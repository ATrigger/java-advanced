package ru.ifmo.ctddev.kamenev.arrayset;

import java.util.*;
//export CLASSPATH=/home/kamenev/java-advanced-2016/lib/hamcrest-core-1.3.jar:/home/kamenev/java-advanced-2016/lib/jsoup-1.8.1.jar:/home/kamenev/java-advanced-2016/lib/junit-4.11.jar:/home/kamenev/java-advanced-2016/lib/quickcheck-0.6.jar:/home/kamenev/java-advanced-2016/artifacts/ArraySetTest.jar:/home/kamenev/java-advanced-2016/artifacts/WalkTest.jar:/home/kamenev/IdeaProjects/ArraySet/out/production/ArraySet:/home/kamenev/IdeaProjects/ArraySet/out/production/ArraySet/

/**
 * Created by kamenev on 29.02.16.
 */
public class ArraySet<T extends Comparable<? super T>> extends AbstractSet<T> implements NavigableSet<T> {
    private final List<T> base;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Comparator<? super T> comp) {
        this(Collections.emptyList(), comp);
    }

    private ArraySet(List<T> list, Comparator<? super T> comp) {
        base = list;
        comparator = comp;
    }

    public ArraySet(Collection<T> collection, Comparator<? super T> comp) {
        Objects.requireNonNull(collection);
        if (comp == null) comparator = Comparator.<T>naturalOrder();
        else comparator = comp;
        if (collection.isEmpty()) {
            base = Collections.emptyList();
        } else {
            ArrayList<T> tmp = new ArrayList<>(collection);
            Collections.sort(tmp, comp);
            int result = 0;
            for (int i = 1; i < tmp.size(); i++) {
                if (comparator.compare(tmp.get(result), tmp.get(i)) != 0) {
                    tmp.set(++result, tmp.get(i));
                }
            }
            tmp.subList(result + 1, tmp.size()).clear();
            base = Collections.unmodifiableList(tmp);
        }
    }

    public ArraySet(Collection<T> collection) {
        this(collection, null);
    }

    @Override
    public T lower(T e) {
        int pos = lowerPos(e);
        return pos == -1 ? null : base.get(pos);
    }

    private int lowerPos(T e) {
        Objects.requireNonNull(e);
        int pos = Collections.binarySearch(base, e, comparator);
        if (pos >= 0) {
            return pos - 1;
        }
        return -(pos + 1) - 1;
    }

    @Override
    public T floor(T e) {
        int ind = floorPos(e);
        return ind == -1 ? null : base.get(ind);
    }

    private int floorPos(T e) {
        Objects.requireNonNull(e);
        int ind = Collections.binarySearch(base, e, comparator);
        if (ind < 0) {
            ind = -(ind + 1);
            return ind - 1;
        } else {
            return ind;
        }
    }

    @Override
    public T ceiling(T e) {
        int ind = ceilingPos(e);
        return ind == base.size() ? null : base.get(ind);
    }

    private int ceilingPos(T e) {
        Objects.requireNonNull(e);
        int ind = Collections.binarySearch(base, e, comparator);
        if (ind < 0) {
            ind = -(ind + 1);
        }
        return ind;
    }

    @Override
    public T higher(T e) {
        int ind = higherPos(e);
        return ind == base.size() ? null : base.get(ind);
    }

    private int higherPos(T e) {
        int ind = Collections.binarySearch(base, e, comparator);
        if (ind >= 0) {
            return ind + 1;
        } else {
            ind = -(ind + 1);
            return ind;
        }
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return base.iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        if (base instanceof ReversingList) {
            return new ArraySet<>(new ReversingList<>(((ReversingList<T>) base)), Collections.reverseOrder(comparator));
        }
        return new ArraySet<>(new ReversingList<>(base, true), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {

        return new Iterator<T>() {
            private ListIterator<T> a = base.listIterator(size() - 1);

            @Override
            public boolean hasNext() {
                return a.hasPrevious();
            }

            @Override
            public T next() {
                return a.previous();
            }

        };
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        Objects.requireNonNull(fromElement);
        Objects.requireNonNull(toElement);
        int from = fromInclusive ? ceilingPos(fromElement) : higherPos(fromElement);
        int to = toInclusive ? floorPos(toElement) : lowerPos(toElement);
        if (from > to) {
            if (comparator.compare(fromElement, toElement) == 0) {
                return new ArraySet<>(Collections.emptyList(), comparator);
            }
        }
        return new ArraySet<>(base.subList(from, to + 1), comparator);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        return new ArraySet<>(base.subList(0, (inclusive ? floorPos(toElement) : lowerPos(toElement)) + 1), comparator);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return new ArraySet<>(base.subList(inclusive ? ceilingPos(fromElement) : higherPos(fromElement), base.size()), comparator);
    }

    @Override
    public Comparator<? super T> comparator() {
        return (comparator.equals( Comparator.<T>naturalOrder())) ? null : comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return base.get(0);
    }

    @Override
    public T last() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return base.get(base.size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(base, (T) o, comparator) >= 0;
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    class ReversingList<T1> extends AbstractList<T1> implements RandomAccess {
        private List<T1> data;
        private boolean reversed;

        ReversingList(ReversingList<T1> revList) {
            data = revList.data;
            reversed = !revList.reversed;
        }

        ReversingList(List<T1> list, boolean reversed) {
            data = list;
            this.reversed = reversed;
        }

        @Override
        public T1 get(int index) {
            return reversed ? data.get(data.size() - index - 1) : data.get(index);
        }

        @Override
        public int size() {
            return data.size();
        }
    }
}
