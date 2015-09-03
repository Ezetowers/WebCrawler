package webcrawler.url.downloader;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import concurrent.WorkersFactory;
import webcrawler.url.downloader.Downloader;


public class DownloaderFactory extends WorkersFactory<URL> {
    public DownloaderFactory(BlockingQueue<String> parseQueue) {
        queue_ = new ArrayBlockingQueue<URL>(DEFAULT_QUEUE_SIZE);
        parseQueue_ = parseQueue;
    }

    public String logPrefix() {
        return "URL_DOWNLOADER_POOL";
    }

    public Thread make() {
        return new Downloader(this.getUniqueId(), 
                              this.logPrefix(), 
                              queue_,
                              parseQueue_);
    }

    public BlockingQueue<URL> getQueue() {
        return queue_;
    }

    private static final int DEFAULT_QUEUE_SIZE = 10000;
    private BlockingQueue<URL> queue_;
    private BlockingQueue<String> parseQueue_;
}