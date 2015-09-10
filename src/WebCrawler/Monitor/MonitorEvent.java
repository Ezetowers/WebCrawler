package monitor;

public class MonitorEvent {
    public enum EventType {
        THREAD_STATE,
        URL,
        RESOURCE;
    }

    public EventType eventType;
    // This value can be null 
    public String id;
    public String value;
}