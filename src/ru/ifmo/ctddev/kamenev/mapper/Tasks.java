package ru.ifmo.ctddev.kamenev.mapper;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Class that manages tasks which are given to workers
 *
 * @see Worker
 * @see Task
 */
public class Tasks {
    private Queue<Task<?, ?>> tasks;

    /**
     * Creates a new instance of class
     */
    public Tasks() {
        tasks = new LinkedList<>();
    }

    /**
     * Puts new {@link Task} in a queue of tasks and notifies all about new task
     *
     * @param input task to enqueue
     * @see #getTask()
     */
    public synchronized void newTask(Task<?, ?> input) {
        tasks.add(input);
        notifyAll();
    }

    /**
     * Returns a {@link Task} or put thread to sleep if nothing is available at the moment
     *
     * @return task
     * @throws InterruptedException if {@link #wait()} is interrupted
     */
    public synchronized Task<?, ?> getTask() throws InterruptedException {
        while (tasks.isEmpty()) {
            wait();
        }
        return tasks.poll();
    }
}
