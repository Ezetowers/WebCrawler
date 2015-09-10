package webcrawler.url;

import java.lang.Enum;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Hashtable;
import java.util.Set;


import configparser.ConfigParser;
import logger.Logger;
import logger.LogLevel;

public class Depot {
    public enum URLArchivedState {
        TO_BE_DOWNLOADED,
        DOWNLOADED,
        UNREACHABLE;
    }

    public class DepotEntry {
        public DepotEntry(URLArchivedState value) {
            state = value;
        }

        // TODO: Timestamp to be added in a near future
        public URLArchivedState state;

    }

    public Depot() {
        depotFilename_ = ConfigParser.get("BASIC-PARAMS", 
                                          "url-depot-filename", 
                                          "/tmp/urlDepot.txt");
        map_ = new Hashtable<String, DepotEntry>();
        lock_ = new ReentrantLock();
    }

    /**
     * @brief Insert an URL into de Depot
     * @return Return the state of the URL
     */
    public URLArchivedState add(String url) {
        lock_.lock();
        DepotEntry entry = map_.get(url);
        URLArchivedState state = null;

        if (entry == null) {
            Logger.log(LogLevel.DEBUG, "[DEPOT] Inserting URL " + url + ".");
            entry = new DepotEntry(URLArchivedState.TO_BE_DOWNLOADED);
            map_.put(url, entry);
            this.serialize();
        }
        else {
            Logger.log(LogLevel.INFO, "[DEPOT] URL " + url 
                + " already exists. State: " 
                + entry.state.toString());
        }

        lock_.unlock();
        return entry.state;
    }

    public void alter(String url, URLArchivedState state) {
        lock_.lock();
        DepotEntry entry = map_.get(url);

        if (entry != null) {
            Logger.log(LogLevel.DEBUG, "[DEPOT] Changing URL " + url 
                + " to state " + state.toString());
            entry.state = state;
            this.serialize();
        } else {
            // TODO: This should not happen!!
            lock_.unlock();
            throw new IllegalStateException("[DEPOT] URL doesn't exists in "
                + "alter.");
        }

        lock_.unlock();
    }

    public void serialize() {
        Set<String> keys = map_.keySet();

        try {
            fileWriter_ = new FileWriter(depotFilename_, false);
            for (String key : keys) {
                URLArchivedState state = map_.get(key).state;

                    fileWriter_.write(key + "," + state.toString() + "\n");
                    fileWriter_.flush();
                }
            fileWriter_.close();
        }
        catch(IOException e) {
            Logger.log(LogLevel.DEBUG, 
                "[DEPOT] Error while serializing: " + e);            
        }
    }

    public void dump() {
        lock_.lock();
        Set<String> keys = map_.keySet();
        Logger.log(LogLevel.DEBUG, "[DEPOT] Dump URL states:");

        for (String key : keys) {
            URLArchivedState state = map_.get(key).state;
            Logger.log(LogLevel.DEBUG, "[DEPOT] URL: " + key + " - State: " 
                + state.toString());
        }
        lock_.unlock();
    }

    public int size() {
        lock_.lock();
        int size = map_.size();
        lock_.unlock();
        Logger.log(LogLevel.DEBUG, "[DEPOT] Size: " + size);
        return map_.size();
    }

    private Hashtable<String, DepotEntry> map_;
    private String depotFilename_;
    private Lock lock_;
    private static FileWriter fileWriter_;
}
