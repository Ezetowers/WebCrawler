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
import webcrawler.url.Depot;


public class Analyzer extends Worker<URL> {
    public Analyzer(long threadId, 
                    String logPrefix, 
                    BlockingQueue<URL> analyzerQueue,
                    BlockingQueue<URL> downloadQueue,
                    Depot depot) {
        super(threadId, logPrefix, analyzerQueue);
        downloadQueue_ = downloadQueue;
        depot_ = depot;
        logPrefix_ += "[ANALYZER] ";
    }

    public void execute() throws InterruptedException {
        URL url = queue_.take();
        Logger.log(LogLevel.DEBUG, logPrefix_ + "Checking if URL [" + url.toString() + "] is wellformed.");
        if (this.wellformedURL(url)) {
            Logger.log(LogLevel.INFO, logPrefix_ + "URL is wellformed. Proceed to search for the URL on the depot.");
            depot_.add(url.toString());

            // TODO: Depot stuff
            Logger.log(LogLevel.DEBUG, logPrefix_ + "URL succesfully processed: " + url.toString());
            downloadQueue_.put(url);
        }
        else {
            // TODO: I don't know yet
        }
    }

    private boolean wellformedURL(URL url) {
        // TODO:
        return true;
    }

    private BlockingQueue<URL> downloadQueue_;
    private Depot depot_;
}