package ru.ifmo.ctddev.kamenev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Thread.yield;

/**
 * Created by kamenev on 01.04.16.
 */
public class CrawlerImpl implements Crawler {
    private final int perhost;
    private final Downloader downloader;
    private final Map<String, Integer> onHost;
    private final Map<String, Queue<Runnable>> pending;

    private final ExecutorService downloaders;
    private final ExecutorService extractors;

    public CrawlerImpl(Downloader downloader, int downloaders, int extractors, int perhost) {
        this.downloader = downloader;
        this.perhost = perhost;
        this.onHost = new HashMap<>();
        this.pending = new HashMap<>();
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
    }

    @Override
    public Result download(String url, int depth) {
        return new Pimpl().download(url,depth);
    }
    private class Pimpl {
        private final Phaser phaser;
        private final Set<String> result;
        private final Map<String, IOException> failure;

        private Pimpl(){
            result = ConcurrentHashMap.newKeySet();
            failure = new ConcurrentHashMap<>();
            phaser = new Phaser(1);
        }
        private Result download(String url, int depth){
            crawl(url,depth);
            phaser.arriveAndAwaitAdvance();
            result.removeAll(failure.keySet());
            return new Result(new ArrayList<>(result), failure);
        }
        private void crawl(String url, int depth){
            result.add(url);
            if (depth == 0) {
                return;
            }
            phaser.register();
            try {
                putHostTask(URLUtils.getHost(url), () -> {
                    try {
                        Document tmp = downloader.download(url);
                        phaser.register();
                        extractors.submit(() -> {
                            try {
                                List<String> extracted = tmp.extractLinks();
                                if (depth > 0) {
                                    extracted.stream().filter(to -> !result.contains(to)).forEach(to -> {
                                        crawl(to, depth - 1);
                                    });
                                }
                            } catch (IOException e) {
                                failure.put(url,e);

                            } finally {
                                phaser.arrive();
                            }
                        });
                    } catch (IOException e) {
                        failure.put(url,e);

                    } finally {
                        try {
                            switchTask(URLUtils.getHost(url));
                        } catch (MalformedURLException e) {
                            failure.put(url,e);
                        }
                        finally {
                            phaser.arrive();
                        }
                    }
                });
            } catch (MalformedURLException e) {
                failure.put(url,e);
            }

        }
    }

    private void putHostTask(String host, Runnable task) {
        synchronized (onHost) {
            onHost.putIfAbsent(host, 0);
            if (onHost.get(host) < perhost) {
                onHost.put(host, onHost.get(host) + 1);
                downloaders.submit(task);
            } else {
                synchronized (pending) {
                    pending.putIfAbsent(host, new ConcurrentLinkedQueue<>());
                    pending.get(host).add(task);
                }
            }
        }
    }

    private void switchTask(String host) {
        synchronized (onHost) {
            if (pending.containsKey(host)) {
                synchronized (pending) {
                    if (!pending.get(host).isEmpty()) {
                        downloaders.submit(pending.get(host).poll());
                    }
                    if (pending.get(host).isEmpty()) {
                        pending.remove(host);
                    }
                }
            } else {
                onHost.put(host, onHost.get(host) - 1);
            }
        }
    }

    @Override
    public void close() {
        downloaders.shutdownNow();
        extractors.shutdownNow();
    }
}
