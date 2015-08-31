package webcrawler.url.analyzer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import concurrent.Workers_Factory;
import webcrawler.url.analyzer.URL_Analyzer;
import webcrawler.url.URL;

public class URL_Analyzer_Factory extends Workers_Factory {
    public URL_Analyzer_Factory() {
        queue_ = new ArrayBlockingQueue<URL>(DEFAULT_QUEUE_SIZE);
    }

    public String logPrefix() {
        return "ANALYZER POOL";
    }

    public Thread make() {
        return new URL_Analyzer(this.getUniqueId(), this.logPrefix(), queue_);
    }

    private static final int DEFAULT_QUEUE_SIZE = 10000;
    private BlockingQueue<URL> queue_;
}