package webcrawler.url.analyzer;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import concurrent.WorkersFactory;
import webcrawler.url.analyzer.Analyzer;
import webcrawler.url.Depot;


public class AnalyzerFactory extends WorkersFactory<URL> {
    public AnalyzerFactory(BlockingQueue<URL> downloadQueue,
                           Depot depot) {
        queue_ = new ArrayBlockingQueue<URL>(DEFAULT_QUEUE_SIZE);
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
                            depot_);
    }

    public BlockingQueue<URL> getQueue() {
        return queue_;
    }

    private static final int DEFAULT_QUEUE_SIZE = 10000;
    private BlockingQueue<URL> queue_;
    private BlockingQueue<URL> downloadQueue_;
    private Depot depot_;
}