package webcrawler.url.analyzer;

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


public class Analyzer extends Worker<URL> {
    public Analyzer(long threadId, 
                    String logPrefix, 
                    BlockingQueue<URL> analyzerQueue,
                    BlockingQueue<URL> downloadQueue) {
        super(threadId, logPrefix, analyzerQueue);
        downloadQueue_ = downloadQueue;
        logPrefix_ += "[ANALYZER] ";
    }

    public void execute() throws InterruptedException {
        URL url = queue_.take();
        Logger.log(LogLevel.DEBUG, logPrefix_ + "Proceed to process an URL " + url.toString());

        // TODO: Depot stuff
        Logger.log(LogLevel.DEBUG, logPrefix_ + "URL succesfully processed: " + url.toString());
        downloadQueue_.put(url);
    }

    private BlockingQueue<URL> downloadQueue_;
}