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
import monitor.MonitorEvent;
import webcrawler.url.Depot;
import webcrawler.url.URLData;


public class Analyzer extends Worker<URLData> {
    public Analyzer(long threadId, 
                    String logPrefix, 
                    BlockingQueue<URLData> analyzerQueue,
                    BlockingQueue<URLData> downloadQueue,
                    BlockingQueue<MonitorEvent> monitorQueue,
                    Depot depot) {
        super(threadId, logPrefix, analyzerQueue);
        downloadQueue_ = downloadQueue;
        monitorQueue_ = monitorQueue;
        depot_ = depot;
        logPrefix_ += "[ANALYZER] ";
    }

    public void execute() throws InterruptedException {
        if (! Analyzer.continueAnalyzing()) {
            stop_ = true;
            MonitorEvent.sendStatusEvent(monitorQueue_,
                                         "ANALYZER-" + threadId_, 
                                         "STOPPED");
            return;
        }

        MonitorEvent.sendStatusEvent(monitorQueue_,
                                     "ANALYZER-" + threadId_, 
                                     "DEQUEING");

        URLData packet = queue_.take();
        Analyzer.counter_.inc();

        MonitorEvent.sendStatusEvent(monitorQueue_,
                                     "ANALYZER-" + threadId_, 
                                     "PROCESSING");

        urlLogPrefix_ = logPrefix_ + "[URL: " + packet.url.toString() + "] ";

        if (packet.nestingLevel() >= nestingThreshold_) {
            Logger.log(LogLevel.TRACE, urlLogPrefix_ + "URL nesting level: " 
                + packet.nestingLevel()
                + " Nesting Threshold: "
                + nestingThreshold_);
            Logger.log(LogLevel.DEBUG, urlLogPrefix_+ "URL won't be analyzed " 
                 + "because nesting threshold was reached.");
            return;
        }


        Logger.log(LogLevel.DEBUG, urlLogPrefix_ 
            + "Checking if URL is wellformed.");
        URL url = this.wellformedURL(packet.url);

        if (url != null) {
            Logger.log(LogLevel.INFO, urlLogPrefix_ 
                + "URL is wellformed. "
                + "Proceed to search for the URL on the depot.");

            if (depot_.add(url.toString()) == 
                Depot.URLArchivedState.TO_BE_DOWNLOADED) {
                Logger.log(LogLevel.DEBUG, urlLogPrefix_ 
                    + "URL succesfully processed");
                downloadQueue_.put(packet);
            }
            else {
                Logger.log(LogLevel.DEBUG, urlLogPrefix_ 
                    + "URL is being proccesed at the moment. Dropping.");
            }
        }
        else {
            Logger.log(LogLevel.INFO, urlLogPrefix_ 
                + "URL is malformed. Dropping.");
        }
    }

    private URL wellformedURL(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        }
        catch (MalformedURLException e) {
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

    private BlockingQueue<URLData> downloadQueue_;
    private BlockingQueue<MonitorEvent> monitorQueue_;
    private Depot depot_;
    private String urlLogPrefix_;
    private static final AtomicCounter counter_ = new AtomicCounter();
    private static final long amountURLsToProcess_ = 
        Long.valueOf(ConfigParser.get("BASIC-PARAMS", 
                                      "amount-iterations", 
                                      "1"));
    private static final int nestingThreshold_ = 
        Integer.parseInt(ConfigParser.get("URL-PARAMS", 
                                          "nesting-threshold", 
                                          "1"));
}
