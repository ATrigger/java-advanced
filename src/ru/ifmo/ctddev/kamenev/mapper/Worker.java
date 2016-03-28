package ru.ifmo.ctddev.kamenev.mapper;

/**
 * Class that describes one worker thread
 */
public class Worker implements Runnable {
    private final Tasks tasks;

    /**
     * Creates new worker and assignes source of tasks to him
     *
     * @param source task to get from
     */
    public Worker(Tasks source) {
        tasks = source;
    }

    /**
     * Launch the worker
     */
    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                tasks.getTask().before();
            }
        } catch (InterruptedException e) {
            //
        }
    }
}
