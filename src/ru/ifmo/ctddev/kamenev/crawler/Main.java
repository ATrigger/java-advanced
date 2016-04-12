package ru.ifmo.ctddev.kamenev.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;

import java.io.IOException;

/**
 * Created by kamenev on 04.04.16.
 */
public class Main {
    public static void main(String[] args) {
        try (CrawlerImpl crawler = new CrawlerImpl(new CachingDownloader(),400,400,1000)){
            crawler.download("http://ifmo.ru",1).getDownloaded().forEach(System.out::println);
            //crawler.download("http://ifmo.ru",1).getDownloaded().forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
