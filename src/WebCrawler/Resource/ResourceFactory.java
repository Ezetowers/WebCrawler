package webcrawler.resource;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import concurrent.WorkersFactory;
import monitor.MonitorEvent;
import webcrawler.resource.ResourceDownloader;


public class ResourceFactory extends WorkersFactory<String> {
    public ResourceFactory(BlockingQueue<MonitorEvent> monitorQueue,
                           String resourceName) {
        queue_ = new ArrayBlockingQueue<String>(DEFAULT_QUEUE_SIZE);
        monitorQueue_ = monitorQueue;
        resourceName_ = resourceName;
    }

    public String logPrefix() {
        return resourceName_ + " RESOURCE_POOL";
    }

    public Thread make() {
        return new ResourceDownloader(this.getUniqueId(), 
                                      this.logPrefix(), 
                                      queue_,
                                      monitorQueue_,
                                      resourceName_);
    }

    public BlockingQueue<String> getQueue() {
        return queue_;
    }

    private static final int DEFAULT_QUEUE_SIZE = 100000;
    private BlockingQueue<MonitorEvent> monitorQueue_;
    private BlockingQueue<String> queue_;
    private String resourceName_;
}