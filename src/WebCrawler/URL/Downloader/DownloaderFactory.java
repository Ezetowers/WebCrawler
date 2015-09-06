package webcrawler.url.downloader;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import concurrent.WorkersFactory;
import webcrawler.url.downloader.Downloader;
import webcrawler.url.Depot;
import webcrawler.url.URLData;


public class DownloaderFactory extends WorkersFactory<URL> {
    public DownloaderFactory(BlockingQueue<URLData> parseQueue,
                             Depot depot) {
        queue_ = new ArrayBlockingQueue<URL>(DEFAULT_QUEUE_SIZE);
        parseQueue_ = parseQueue;
        depot_ = depot;
    }

    public String logPrefix() {
        return "URL_DOWNLOADER_POOL";
    }

    public Thread make() {
        return new Downloader(this.getUniqueId(), 
                              this.logPrefix(), 
                              queue_,
                              parseQueue_,
                              depot_);
    }

    public BlockingQueue<URL> getQueue() {
        return queue_;
    }

    private static final int DEFAULT_QUEUE_SIZE = 100000;
    private BlockingQueue<URL> queue_;
    private BlockingQueue<URLData> parseQueue_;
    private Depot depot_;
}