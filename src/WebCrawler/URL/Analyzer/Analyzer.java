package webcrawler.url.analyzer;

// Java imports
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.concurrent.BlockingQueue;

// Project imports
import concurrent.Worker;
import concurrent.AtomicCounter;
import configparser.ConfigParser;
import logger.Logger;
import logger.LogLevel;
import webcrawler.url.Depot;


public class Analyzer extends Worker<String> {
    public Analyzer(long threadId, 
                    String logPrefix, 
                    BlockingQueue<String> analyzerQueue,
                    BlockingQueue<URL> downloadQueue,
                    Depot depot) {
        super(threadId, logPrefix, analyzerQueue);
        downloadQueue_ = downloadQueue;
        depot_ = depot;
        logPrefix_ += "[ANALYZER] ";
    }

    public void execute() throws InterruptedException {
        if (! Analyzer.continueAnalyzing()) {
            stop_ = true;
            return;
        }

        String packet = queue_.take();
        urlLogPrefix_ = logPrefix_ + "[URL: " + packet.toString() + "] ";
        Analyzer.counter_.inc();

        Logger.log(LogLevel.DEBUG, urlLogPrefix_ 
            + "Checking if URL is wellformed.");
        URL url = this.wellformedURL(packet);

        if (url != null) {
            Logger.log(LogLevel.INFO, urlLogPrefix_ 
                + "URL is wellformed. "
                + "Proceed to search for the URL on the depot.");

            if (depot_.add(url.toString()) == 
                Depot.URLArchivedState.TO_BE_DOWNLOADED) {
                Logger.log(LogLevel.DEBUG, urlLogPrefix_ 
                    + "URL succesfully processed");
                downloadQueue_.put(url);
            }
            else {
                Logger.log(LogLevel.INFO, urlLogPrefix_ + "URL is malformed.");
                // TODO:
            }
        }
        else {
            // TODO: I don't know yet
        }
    }

    private URL wellformedURL(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        }
        catch (MalformedURLException e) {
            // Logger.log(LogLevel.INFO, urlLogPrefix_ 
            //     + "[ANALYZER] Error forming URL.");
            System.err.println(e);
        }
        return url;
    }

    private static void incCounter() {
        counter_.inc();
    }

    public static boolean continueAnalyzing() {
        if (amountURLsToProcess_ == 0) {
            return true;
        }
        
        Logger.log(LogLevel.DEBUG, "[ATOMIC COUNTER] Counter value: " 
            + counter_.counter() + " - Threshold value: " 
            + amountURLsToProcess_);
        return counter_.counter() <= amountURLsToProcess_;
    }

    private BlockingQueue<URL> downloadQueue_;
    private Depot depot_;
    private String urlLogPrefix_;
    private static final AtomicCounter counter_ = new AtomicCounter();
    private static final long amountURLsToProcess_ = 
        Long.valueOf(ConfigParser.get("BASIC-PARAMS", 
                                      "amount-iterations", 
                                      "1"));
}
