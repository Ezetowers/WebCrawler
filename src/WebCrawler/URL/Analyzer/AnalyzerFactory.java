package webcrawler.url.analyzer;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import concurrent.WorkersFactory;
import monitor.MonitorEvent;
import webcrawler.url.analyzer.Analyzer;
import webcrawler.url.Depot;
import webcrawler.url.URLData;


public class AnalyzerFactory extends WorkersFactory<URLData> {
    public AnalyzerFactory(BlockingQueue<URLData> downloadQueue,
                           BlockingQueue<MonitorEvent> monitorQueue,
                           Depot depot) {
        queue_ = new ArrayBlockingQueue<URLData>(DEFAULT_QUEUE_SIZE);
        monitorQueue_ = monitorQueue;
        downloadQueue_ = downloadQueue;
        depot_ = depot;
    }

    public String logPrefix() {
        return "ANALYZER_POOL";
    }

    public Thread make() {
        return new Analyzer(this.getUniqueId(), 
                            this.logPrefix(), 
                            queue_,
                            downloadQueue_,
                            monitorQueue_,
                            depot_);
    }

    public BlockingQueue<URLData> getQueue() {
        return queue_;
    }

    private static final int DEFAULT_QUEUE_SIZE = 100000;
    private BlockingQueue<URLData> queue_;
    private BlockingQueue<URLData> downloadQueue_;
    private BlockingQueue<MonitorEvent> monitorQueue_;
    private Depot depot_;
}