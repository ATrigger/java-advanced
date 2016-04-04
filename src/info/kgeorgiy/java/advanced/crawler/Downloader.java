package info.kgeorgiy.java.advanced.crawler;

import java.io.IOException;

/**
 * Downloads {@link Document documents}.
 *
 * @see CachingDownloader
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public interface Downloader {
    /**
     * Downloads {@link Document} by
     * <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     *
     * @param url URL to download.
     * @return downloaded document.
     * @throws java.io.IOException if an error occurred.
     */
    public Document download(final String url) throws IOException;
}
