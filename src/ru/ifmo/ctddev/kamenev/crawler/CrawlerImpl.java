package ru.ifmo.ctddev.kamenev.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

/**
 * Created by kamenev on 01.04.16.
 */
public class CrawlerImpl implements Crawler {
    private final int perhost;
    private final List<String> result;
    private final Downloader downloader;
    private final Map<String, Integer> onHost;
    private final Map<String, Deque<Runnable>> pending;
    private Phaser phaser;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;

    public CrawlerImpl(Downloader downloader, int downloaders, int extractors, int perhost) {
        this.downloader = downloader;
        this.phaser = new Phaser(1);
        this.result = new ArrayList();
        this.perhost = perhost;
        this.onHost = new HashMap<>();
        this.pending = new HashMap<>();
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
    }

    @Override
    public List<String> download(String url, int depth) throws IOException {
        visit(url, depth);
        phaser.arriveAndAwaitAdvance();
        phaser = new Phaser(1);
        return result;
    }

    private void visit(String uri, int depth) {
        result.add(uri);
        if (depth == 0) {
            return;
        }
        phaser.register();
        try {
            putHostTask(URLUtils.getHost(uri), () -> {
                try {
                    phaser.register();
                    Document tmp = downloader.download(uri);
                    extractors.submit(() -> {
                        try {
                            if (depth > 0) {
                                for (String to : tmp.extractLinks()) {
                                    if (!result.contains(to)) {
                                        visit(to, depth - 1);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            phaser.arrive();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        switchTask(URLUtils.getHost(uri));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    phaser.arrive();
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void putHostTask(String host, Runnable task) {

        onHost.putIfAbsent(host, 0);
        int nowUsingHost = onHost.get(host);
        if (nowUsingHost < perhost) {
            onHost.put(host, nowUsingHost + 1);
            downloaders.submit(task);
        } else {
            pending.putIfAbsent(host, new ArrayDeque<>());
            pending.get(host).add(task);
        }

    }

    private void switchTask(String host) {
        int nowUsingHost = onHost.get(host);
        if (pending.containsKey(host)) {
            if (!pending.get(host).isEmpty()) {
                downloaders.submit(pending.get(host).poll());
            }
            if (pending.get(host).isEmpty()) {
                pending.remove(host);
            }
        } else {
            onHost.put(host, nowUsingHost - 1);
        }
    }

    @Override
    public void close() {
        downloaders.shutdownNow();
        extractors.shutdownNow();
    }
}
