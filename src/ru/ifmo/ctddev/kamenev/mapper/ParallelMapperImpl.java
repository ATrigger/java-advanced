package ru.ifmo.ctddev.kamenev.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Class designed to manage threads.
 * Can be used in addition to {@link IterativeParallelism}
 *
 * @see ParallelMapper
 */
public class ParallelMapperImpl implements ParallelMapper {
    private List<Thread> threadPool;
    private Tasks tasks;

    /**
     * Creates new instance of class and starts {@code threadcount} threads
     *
     * @param threadcount number of threads to start
     * @see Worker
     */
    public ParallelMapperImpl(int threadcount) {
        tasks = new Tasks();
        threadPool = new ArrayList<>();
        for (int i = 0; i < threadcount; i++) {
            Thread tmp = new Thread(new Worker(tasks));
            threadPool.add(tmp);
            tmp.start();
        }
    }

    /**
     * Creates tasks of given data and pass it to workers
     *
     * @param f    function to use for processing
     * @param args list of input data
     * @param <T>  type that describes input data
     * @param <R>  type that describes output data
     * @return list of results of processing every given part of {@code args}
     * @throws InterruptedException
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<Task<? super T, ? extends R>> currentTasks = new ArrayList<>();
        for (T part : args) {
            Task<? super T, ? extends R> tmp = new Task<>(part, f);
            currentTasks.add(tmp);
            tasks.newTask(tmp);
        }
        List<R> result = new ArrayList<>();
        for (Task<? super T, ? extends R> tt : currentTasks) {
            result.add(tt.after());
        }
        return result;
    }

    /**
     * Stops all threads
     *
     * @throws InterruptedException
     */
    @Override
    public void close() throws InterruptedException {
        threadPool.forEach(Thread::interrupt);
    }
}
