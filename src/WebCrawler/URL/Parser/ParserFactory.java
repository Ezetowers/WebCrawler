package webcrawler.url.parser;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.Hashtable;

import concurrent.WorkersFactory;
import webcrawler.url.parser.Parser;
import webcrawler.url.URLData;


public class ParserFactory extends WorkersFactory<URLData> {
    public ParserFactory(
        Hashtable<String, BlockingQueue<String> > resourceQueues) {

        queue_ = new ArrayBlockingQueue<URLData>(DEFAULT_QUEUE_SIZE);
        resourceQueues_ = resourceQueues;
    }

    public String logPrefix() {
        return "PARSER_POOL";
    }

    public Thread make() {
        return new Parser(this.getUniqueId(), 
                          this.logPrefix(), 
                          queue_,
                          analyzerQueue_,
                          resourceQueues_);
    }

    public BlockingQueue<URLData> getQueue() {
        return queue_;
    }

    public void setAnalyzerQueue(BlockingQueue<String> analyzerQueue) {
        analyzerQueue_ = analyzerQueue;
    }

    private static final int DEFAULT_QUEUE_SIZE = 100000;
    private BlockingQueue<URLData> queue_;
    private BlockingQueue<String> analyzerQueue_;
    private Hashtable<String, BlockingQueue<String> > resourceQueues_;
}