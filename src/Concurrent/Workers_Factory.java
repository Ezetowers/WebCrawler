package concurrent;

import java.lang.Thread;

public abstract class Workers_Factory {
    public abstract Thread make();

    public long getUniqueId() {
        return ++count_;
    }

    private long count_ = 0;
}