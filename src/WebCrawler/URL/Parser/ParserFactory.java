package webcrawler.url.parser;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import concurrent.WorkersFactory;
import webcrawler.url.parser.Parser;


public class ParserFactory extends WorkersFactory<String> {
    public ParserFactory() {
        queue_ = new ArrayBlockingQueue<String>(DEFAULT_QUEUE_SIZE);
    }

    public String logPrefix() {
        return "PARSER_POOL";
    }

    public Thread make() {
        return new Parser(this.getUniqueId(), 
                          this.logPrefix(), 
                          queue_,
                          analyzerQueue_);
    }

    public BlockingQueue<String> getQueue() {
        return queue_;
    }

    public void setAnalyzerQueue(BlockingQueue<URL> analyzerQueue) {
        analyzerQueue_ = analyzerQueue;
    }

    private static final int DEFAULT_QUEUE_SIZE = 10000;
    private BlockingQueue<String> queue_;
    private BlockingQueue<URL> analyzerQueue_;
}