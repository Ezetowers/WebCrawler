package webcrawler.url.downloader;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.Hashtable;

import concurrent.WorkersFactory;
import monitor.MonitorEvent;
import webcrawler.url.downloader.Downloader;
import webcrawler.url.Depot;
import webcrawler.url.URLData;


public class DownloaderFactory extends WorkersFactory<URLData> {
    public DownloaderFactory(BlockingQueue<URLData> parseQueue,
                             BlockingQueue<MonitorEvent> monitorQueue,
                             Depot depot) {

        queue_ = new ArrayBlockingQueue<URLData>(DEFAULT_QUEUE_SIZE);
        parseQueue_ = parseQueue;
        monitorQueue_ = monitorQueue;
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
                              monitorQueue_,
                              depot_);
    }

    public BlockingQueue<URLData> getQueue() {
        return queue_;
    }

    private static final int DEFAULT_QUEUE_SIZE = 100000;
    private BlockingQueue<URLData> queue_;
    private BlockingQueue<URLData> parseQueue_;
    private BlockingQueue<MonitorEvent> monitorQueue_;
    private Depot depot_;
}