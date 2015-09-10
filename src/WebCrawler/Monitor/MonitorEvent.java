package monitor;

import java.util.concurrent.BlockingQueue;

public class MonitorEvent {
    public enum EventType {
        THREAD_STATE,
        URL,
        RESOURCE;
    }

    public static void sendStatusEvent(BlockingQueue<MonitorEvent> queue,
                                       String rhsId,
                                       String rhsValue) 
        throws InterruptedException {

        MonitorEvent monEvent = new MonitorEvent();
        monEvent.eventType = EventType.THREAD_STATE;
        monEvent.id = rhsId;
        monEvent.value = rhsValue;
        queue.put(monEvent);
    }


    public static void sendURLDownloadMsg(BlockingQueue<MonitorEvent> queue) 
        throws InterruptedException {
            
        MonitorEvent monEvent = new MonitorEvent();
        monEvent.eventType = EventType.URL;
        queue.put(monEvent);
    }

    public static void sendResourceMsg(BlockingQueue<MonitorEvent> queue,
                                       String resource) 
        throws InterruptedException {
            
        MonitorEvent monEvent = new MonitorEvent();
        monEvent.eventType = EventType.URL;
        monEvent.id = resource;
        queue.put(monEvent);
    }

    public EventType eventType;
    public String id;
    public String value;
}