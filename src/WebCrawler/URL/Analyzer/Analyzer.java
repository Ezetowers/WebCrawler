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
        String packet = queue_.take();
        Logger.log(LogLevel.DEBUG, logPrefix_ + "Checking if URL [" + packet + "] is wellformed.");
        URL url = this.wellformedURL(packet);

        if (url != null) {
            Logger.log(LogLevel.INFO, logPrefix_ + "URL " + url.toString() + 
                " is wellformed. Proceed to search for the URL on the depot.");

            if (depot_.add(url.toString()) == Depot.URLArchivedState.TO_BE_DOWNLOADED) {
                Logger.log(LogLevel.DEBUG, logPrefix_ + "URL succesfully processed: " + url.toString());
                downloadQueue_.put(url);
            }
            else {
                Logger.log(LogLevel.INFO, logPrefix_ + "URL is malformed. .");
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
            Logger.log(LogLevel.INFO, "[ANALYZER] Error forming URL.");
            System.err.println(e);
        }
        return url;
    }

    private BlockingQueue<URL> downloadQueue_;
    private Depot depot_;
}
