package monitor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import concurrent.WorkersFactory;
import monitor.MonitorEvent;
import monitor.StatsManager;


public class MonitorFactory extends WorkersFactory<MonitorEvent> {
    public MonitorFactory(StatsManager statsManager) {
        queue_ = new ArrayBlockingQueue<MonitorEvent>(DEFAULT_QUEUE_SIZE);
        statsManager_ = statsManager;
    }

    public String logPrefix() {
        return "MONITOR_POOL";
    }

    public Thread make() {
        return new Monitor(this.getUniqueId(), 
                           this.logPrefix(), 
                           queue_,
                           statsManager_);
    }

    public BlockingQueue<MonitorEvent> getQueue() {
        return queue_;
    }

    private static final int DEFAULT_QUEUE_SIZE = 100000;
    private BlockingQueue<MonitorEvent> queue_;
    private StatsManager statsManager_;
}