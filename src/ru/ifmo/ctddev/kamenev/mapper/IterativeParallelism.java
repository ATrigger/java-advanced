package ru.ifmo.ctddev.kamenev.mapper;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Class designed to parallel actions on list-based collections.
 * Can use {@link ParallelMapper} as well.
 * @author Vladislav Kamenev
 * @see info.kgeorgiy.java.advanced.concurrent.ListIP
 */
public class IterativeParallelism implements ListIP {
    private final ParallelMapper parallelMapper;

    /**
     * Creates new instance of class w\o usage of {@link ParallelMapper}
     */
    public IterativeParallelism() {
        parallelMapper = null;
    }

    /**
     * Creates new instance of class to use with {@link ParallelMapper}
     * @param to {@link ParallelMapper} to use to
     */
    public IterativeParallelism(ParallelMapper to) {
        parallelMapper = to;
    }

    private <T> List<List<? extends T>> slice(int parts, List<? extends T> target) {
        List<List<? extends T>> parted = new ArrayList<>();
        int n = target.size();
        parts = Math.min(parts, n);
        int chunk = n / parts;
        int to = 0;
        for (int it = 0; it < parts; it += 1) {
            int from = to;
            to += chunk + ((it < n % parts) ? (1) : (0));
            parted.add(target.subList(from, to));
        }
        return parted;
    }

    private <I, O> void startAndJoin(List<Worker<I, O>> target) throws InterruptedException {
        List<Thread> threads = target.stream().map(Thread::new).collect(Collectors.toList());
        threads.forEach(Thread::start);
        try {
            for (Thread thr : threads) {
                thr.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }

    }

    private class Worker<I, O> implements Runnable {
        private List<? extends I> data;
        private Function<List<? extends I>, O> func;
        private O result;

        public Worker(Function<List<? extends I>, O> func, List<? extends I> data) {
            this.func = func;
            this.data = data;
        }

        @Override
        public void run() {
            result = func.apply(data);
        }

        public O getResult() {
            return result;
        }
    }

    private <T, R> List<R> parallel(int number, List<? extends T> list, Function<List<? extends T>, R> func) throws InterruptedException {
        List<List<? extends T>> partition = slice(number, list);
        if (parallelMapper != null) {
            return parallelMapper.map(func, partition);
        }
        List<Worker<T, R>> threads = new ArrayList<>();
        for (List<? extends T> part : partition) {
            threads.add(new Worker<>(func, part));
        }
        startAndJoin(threads);
        return threads.stream().map(Worker::getResult).collect(Collectors.toList());
    }

    /**
     * <p>
     * Joins string representation of elements of the list.
     * </p>
     * Joins string representation of elements of the {@code list} using {@code StringBuilder}
     *
     * @param i    number of threads
     * @param list list to use to
     * @return {@code String} representation of all elements
     * @throws InterruptedException if any thread has interrupted any of created thread
     * @see #parallel(int, List, Function)
     */
    @Override
    public String join(int i, List<?> list) throws InterruptedException {
        StringBuilder res = new StringBuilder();
        Function<List<?>, String> toStringer = arg -> {
            StringBuilder nested = new StringBuilder();
            arg.stream().map(Object::toString).forEach(nested::append);
            return nested.toString();
        };
        parallel(i, list, toStringer).stream().forEach(res::append);
        return res.toString();
    }

    /**
     * Filters list with given {@code predicate}
     *
     * @param i         number of threads
     * @param list      list to use to
     * @param predicate predicate to filter to
     * @return list of elements that satisfies predicate
     * @throws InterruptedException if any thread has interrupted any of created thread
     * @see #parallel(int, List, Function)
     */
    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        List<T> res = new ArrayList<>();
        Function<List<? extends T>, List<T>> mapper = arg -> arg.stream().filter(predicate).collect(Collectors.toList());
        parallel(i, list, mapper).stream().forEach(res::addAll);
        return res;
    }

    /**
     * Returns a list consisting of the results of applying the given
     * {@code function} to the elements of given {@code list}.
     *
     * @param i        number of threads
     * @param list     list to use to
     * @param function function to map to
     * @return list of elements after using {@code function} on them
     * @throws InterruptedException if any thread has interrupted any of created thread
     * @throws NoSuchElementException if {@code list} is empty
     * @see #parallel(int, List, Function)
     */
    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        List<U> res = new ArrayList<>();
        Function<List<? extends T>, List<U>> mapper = arg -> arg.stream().map(function).collect(Collectors.toList());
        parallel(i, list, mapper).stream().forEach(res::addAll);
        return res;
    }

    /**
     * Returns the maximum element of given {@code list} according to the provided
     * {@code Comparator}.
     *
     * @param i          number of threads
     * @param list       list to use to
     * @param comparator comparator to use to
     * @return minimum element
     * @throws InterruptedException if any thread has interrupted any of created thread
     * @throws NoSuchElementException if {@code list} is empty
     * @see #parallel(int, List, Function)
     */
    @Override
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException, NoSuchElementException {
        return minimum(i, list, comparator.reversed());
    }

    /**
     * Returns the minimum element of given {@code list} according to the provided
     * {@code Comparator}.
     *
     * @param i          number of threads
     * @param list       list to use to
     * @param comparator comparator to use to
     * @return minimum element
     * @throws InterruptedException if any thread has interrupted any of created thread
     * @see #parallel(int, List, Function)
     */
    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException, NoSuchElementException {
        Function<List<? extends T>, T> min = arg -> arg.stream().min(comparator).get();
        return min.apply(parallel(i, list, min));
    }

    /**
     * Returns whether all elements of given {@code list} match the provided {@code predicate}.
     * May not evaluate the predicate on all elements if not necessary for
     * determining the result.  If the list is empty then {@code true} is
     * returned and the predicate is not evaluated.
     *
     * @param i         number of threads
     * @param list      list to use to
     * @param predicate predicate to check to
     * @return boolean according to description
     * @throws InterruptedException if any thread has interrupted any of created thread
     * @see #parallel(int, List, Function)
     */
    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return !any(i, list, predicate.negate());
    }

    /**
     * Returns whether any elements of given {@code list} match the provided {@code predicate}.
     * May not evaluate the predicate on all elements if not
     * necessary for determining the result.  If the stream is empty then
     * {@code false} is returned and the predicate is not evaluated.
     *
     * @param i         number of threads
     * @param list      list to use to
     * @param predicate predicate to check to
     * @return boolean according to description
     * @throws InterruptedException if any thread has interrupted any of created thread
     * @see #parallel(int, List, Function)
     */
    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallel(i, list, arg -> arg.stream().anyMatch(predicate)).stream().anyMatch(Predicate.isEqual(true));
    }
}
