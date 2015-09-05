package webcrawler.url;

import java.lang.Enum;
import java.util.Hashtable;
import java.util.Set;
import java.net.URL;
import java.time.LocalTime;

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

    public Depot(String depotFilename) {
        depotFilename_ = depotFilename;
        /* UNREACHABLE_TIMEOUT = Long.parseLong(ConfigParser.get("DEPOT-PARAMS", 
                                                              "unreachable-timeout", 
                                                              "60"), 36);*/
    }

    /**
     * @brief Insert an URL into de Depot
     * @return Return the state 
     */
    public URLArchivedState add(String url) {
        DepotEntry entry = map_.get(url);
        URLArchivedState state = null;

        if (entry == null) {
            Logger.log(LogLevel.DEBUG, "[DEPOT] Inserting URL " + url + ".");
            entry = new DepotEntry(URLArchivedState.TO_BE_DOWNLOADED);
            map_.put(url, entry);
        }
        else {
            Logger.log(LogLevel.INFO, "[DEPOT] URL " + url + " already exists. State: " 
                + entry.state.toString());
        }

        return entry.state;
    }

    public void alter(String url, URLArchivedState state) {
    	DepotEntry entry = map_.get(url);
    	if (entry != null) {
    		Logger.log(LogLevel.DEBUG, "[DEPOT] Changing URL " + url + "to state " + state.toString());
    		entry.state = state;
    	} else {
    		// TODO: This should not happen!!
    		throw new IllegalStateException("[DEPOT] URL doesn't exists in alter.");
    	}
    }

    public void dump() {
        Set<String> keys = map_.keySet();
        Logger.log(LogLevel.DEBUG, "[DEPOT] Dump URL states:");

        for (String key : keys) {
            URLArchivedState state = map_.get(key).state;
            Logger.log(LogLevel.DEBUG, "[DEPOT] URL: " + key + " - State: " + state.toString());
        }
    }

    private Hashtable<String, DepotEntry> map_ = new Hashtable<String, DepotEntry>();
    // private ObjectOutputStream serializer_;
    private String depotFilename_;
    // private final long UNREACHABLE_TIMEOUT;
}