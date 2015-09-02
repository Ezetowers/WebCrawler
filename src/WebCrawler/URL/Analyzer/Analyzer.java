package webcrawler.url.analyzer;

// Java imports
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

// Project imports
import concurrent.Worker;
import logger.Logger;
import logger.LogLevel;


public class Analyzer extends Worker<URL> {
    public Analyzer(long threadId, String logPrefix, BlockingQueue<URL> queue) {
        super(threadId, logPrefix, queue);
    }

    public void execute() throws InterruptedException {
        // TODO: Add logic
        URL url = queue_.take();
        Logger.getInstance().log(LogLevel.DEBUG, logPrefix_ + "Proceed to process an URL " + url.toString());
    }
}