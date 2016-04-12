package ru.ifmo.ctddev.kamenev.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;

import java.io.IOException;

/**
 * Created by kamenev on 04.04.16.
 */
public class Main {
    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 4) {
            System.out.println("Usage: url [downloads [extractors [perHost]]]");
            System.exit(1);
        }
        String target = "";
        int down = 10;
        int ext = 10;
        int perHost = 20;
        target = args[0];
        try {
            down = Integer.parseInt(args[1]);
            ext = Integer.parseInt(args[2]);
            perHost = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.out.println("Warning: using defaults");
        }
        try(Crawler crawler = new CrawlerImpl(new CachingDownloader(),down,ext,perHost)){
            crawler.download(target,2).getDownloaded().stream().forEach(System.out::println);
        } catch (IOException e) {
            System.out.println("Could not create temp dir");
            System.exit(1);
        }
    }
}
