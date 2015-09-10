package monitor;

// Java imports
import java.util.concurrent.BlockingQueue;

// Project imports
import concurrent.Worker;
import configparser.ConfigParser;
import logger.Logger;
import logger.LogLevel;
import monitor.MonitorEvent;
import monitor.StatsManager;


public class Monitor extends Worker<MonitorEvent> {
    public Monitor(long threadId, 
                   String logPrefix, 
                   BlockingQueue<MonitorEvent> monitorQueue,
                   StatsManager statsManager) {
        super(threadId, logPrefix, monitorQueue);
        statsManager_ = statsManager;
        logPrefix_ += "[MONITOR] ";
    }

    public void execute() throws InterruptedException {
        // TODO: Refactor this logic to support more stats and in a flexible way
        MonitorEvent event = queue_.take();
        switch (event.eventType) {
            case THREAD_STATE:
                statsManager_.updateThreadState(event.id, event.value);
                break;
            case URL:
                statsManager_.updateURLDownloads();
                break;
            case RESOURCE:
                statsManager_.updateResourceDownloads(event.id);
                break;
        }
    }

    private StatsManager statsManager_;
    private String logPrefix_;
}
