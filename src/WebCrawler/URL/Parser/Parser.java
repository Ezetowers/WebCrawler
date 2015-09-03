package webcrawler.url.parser;

// Java imports
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

// Project imports
import concurrent.Worker;
import logger.Logger;
import logger.LogLevel;


public class Parser extends Worker<String> {
    public Parser(long threadId, 
                    String logPrefix, 
                    BlockingQueue<String> parserQueue,
                    BlockingQueue<URL> analyzerQueue) {
        super(threadId, logPrefix, parserQueue);
        analyzerQueue_ = analyzerQueue;
        logPrefix_ += "[PARSER] ";
    }

    public void execute() throws InterruptedException {
        String urlBody = queue_.take();
        Logger.log(LogLevel.DEBUG, logPrefix_ + "A packet arrived.");

        // TODO: Depot stuff
        // Logger.log(LogLevel.DEBUG, logPrefix_ + "URL succesfully processed: " + url.toString());
        // downloadQueue_.put(url);
    }

    private BlockingQueue<URL> analyzerQueue_;
}