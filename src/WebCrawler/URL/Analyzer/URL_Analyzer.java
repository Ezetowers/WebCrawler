package webcrawler.url.analyzer;

// Java imports
import java.util.concurrent.BlockingQueue;

// Project imports
import concurrent.Worker;
import logger.Logger;
import logger.LogLevel;
import webcrawler.url.URL;

public class URL_Analyzer extends Worker<URL> {
    public URL_Analyzer(long threadId, String logPrefix, BlockingQueue<URL> queue) {
        super(threadId, logPrefix, queue);
    }

    public void execute() throws InterruptedException {
        // TODO: Add logic
        Thread.sleep(1000);
        Logger.getInstance().log(LogLevel.INFO, logPrefix_ + "MONDONGO :)");
    }
}