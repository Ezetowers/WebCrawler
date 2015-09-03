import java.util.HashMap;
import java.net.URL;
import java.time.LocalTime;

import configparser.ConfigParser;

public class Depot {
    public enum URLArchivedState {
        TO_BE_DOWNLOADED,
        DOWNLOADED,
        UNREACHABLE
    }

    class DepotEntry {
        public URL url;
        public URLArchivedState state;
    }

    public Depot(String depotFilename) {
        depotFilename_ = depotFilename;
        UNREACHABLE_TIMEOUT = Long.parseLong(ConfigParser.get("DEPOT-PARAMS", 
                                                              "unreachable-timeout", 
                                                              "60"), 36);

        // serializer_ = new ObjectOutputStream

        // Check if the file already exists. In that case, bring it to the map        
    }

    private void flush() {

    }

    private HashMap<String, DepotEntry> map_;
    // private ObjectOutputStream serializer_;
    private String depotFilename_;
    private final long UNREACHABLE_TIMEOUT;

}