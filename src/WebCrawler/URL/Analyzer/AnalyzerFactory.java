package webcrawler.url.analyzer;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import concurrent.WorkersFactory;
import webcrawler.url.analyzer.Analyzer;



public class AnalyzerFactory extends WorkersFactory {
    public AnalyzerFactory() {
        queue_ = new ArrayBlockingQueue<URL>(DEFAULT_QUEUE_SIZE);
    }

    public String logPrefix() {
        return "ANALYZER POOL";
    }

    public Thread make() {
        return new Analyzer(this.getUniqueId(), this.logPrefix(), queue_);
    }

    private static final int DEFAULT_QUEUE_SIZE = 10000;
    private BlockingQueue<URL> queue_;
}