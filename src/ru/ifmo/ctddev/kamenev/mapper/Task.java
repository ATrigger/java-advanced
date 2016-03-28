package ru.ifmo.ctddev.kamenev.mapper;

import java.util.Optional;
import java.util.function.Function;

/**
 * Class designed to apply {@code inProcess} to {@code input} in a separate thread
 *
 * @param <T> type that describes input data
 * @param <R> type after applying {@code inProcess} to {@code input}
 * @see java.util.function.Function
 */
public class Task<T, R> {
    private T arg;
    private Optional<R> result;
    private Function<? super T, ? extends R> process;

    /**
     * Creates new task.
     *
     * @param input     data to process
     * @param inProcess function to use to process
     */
    public Task(T input, Function<? super T, ? extends R> inProcess) {
        result = Optional.empty();
        this.arg = input;
        this.process = inProcess;
    }

    /**
     * Applies function {@code inProgress} and stores the result.
     */
    public synchronized void before() {
        result = Optional.of(process.apply(arg));
        notifyAll();
    }

    /**
     * Gets results, that was stored at {@link #before()} or waits if it is not ready yet.
     *
     * @return result after applying {@code inProgress} to {@code input}
     * @throws InterruptedException if this thread is interrupted during awit
     */
    public synchronized R after() throws InterruptedException {
        while (!result.isPresent()) {
            wait();
        }
        return result.get();
    }
}
