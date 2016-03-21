package ru.ifmo.ctddev.kamenev.parallel;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by kamenev on 21.03.16.
 */
public class IterativeParallelism implements ListIP {
    private <In, Out> void startAndJoin(List<MyRunnable<In, Out>> target) {
        List<Thread> threads = new ArrayList<>();
        for (MyRunnable<In, Out> run : target) {
            threads.add(new Thread(run));
        }
        for (Thread thr : threads) {
            thr.start();
        }
        try {
            for (Thread thr : threads) {
                thr.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private class MyRunnable<InputType, OutputType> implements Runnable {
        private List<? extends InputType> data;
        private Function<List<? extends InputType>, OutputType> func;
        private OutputType result;

        public MyRunnable(Function<List<? extends InputType>, OutputType> func, List<? extends InputType> data) {
            this.func = func;
            this.data = data;
        }

        @Override
        public void run() {
            result = func.apply(data);
        }

        public OutputType getResult() {
            return result;
        }
    }

    private <T, R> List<R> parallel(int i, List<? extends T> list, Function<List<? extends T>, R> func) {
        List<MyRunnable<T, R>> threads = new ArrayList<>();
        int n = list.size();
        int step = Math.max(n/i,1);
        for (int it = 0; it < n; it += step) {
            threads.add(new MyRunnable<>(func, list.subList(it, Math.min(n, it + step))));
        }
        startAndJoin(threads);
        return threads.stream().map(MyRunnable::getResult).collect(Collectors.toList());
    }

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

    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        List<T> res = new ArrayList<>();
        Function<List<? extends T>, List<T>> mapper = arg -> arg.stream().filter(predicate).collect(Collectors.toList());
        parallel(i, list, mapper).stream().forEach(res::addAll);
        return res;
    }

    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        List<U> res = new ArrayList<>();
        Function<List<? extends T>, List<U>> mapper = arg -> arg.stream().map(function).collect(Collectors.toList());
        parallel(i, list, mapper).stream().forEach(res::addAll);
        return res;
    }

    @Override
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return minimum(i, list, comparator.reversed());
    }

    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> min = arg -> arg.stream().min(comparator).get();
        return min.apply(parallel(i, list, min));
    }

    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallel(i, list, arg -> arg.stream().allMatch(predicate)).stream().allMatch(Predicate.isEqual(true));
    }

    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallel(i, list, arg -> arg.stream().anyMatch(predicate)).stream().anyMatch(Predicate.isEqual(true));
    }
}
