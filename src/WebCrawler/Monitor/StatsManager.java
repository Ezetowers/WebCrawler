package monitor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.System;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Set;

import configparser.ConfigParser;
import logger.Logger;
import logger.LogLevel;

public class StatsManager extends Thread {
    public StatsManager() {
        lock_ = new ReentrantLock();
        threadsState_ = new Hashtable<String, String>();
        resourcesInfo_ = new HashMap<String, Integer>();
        logPrefix_ = "[STATS_MANAGER] ";
        urlDownloadedCounter_ = 0;
    }

    public void run() {
        while (! Thread.interrupted()) {
            try {
                // THREAD STATS
                FileWriter fileWriter = new FileWriter(statsFile_, false);
                fileWriter.write("Amount URLs Downloaded: " 
                    + urlDownloadedCounter_ + "\n");
                fileWriter.write("THREAD STATES: \n");

                Set<String> keys = threadsState_.keySet();
                for (String key : keys) {
                    String line = key + ": - " + threadsState_.get(key) + "\n";
                    fileWriter.write(line);
                }

                Set<String> resourcesKeys = resourcesInfo_.keySet();
                for (String resourceKey : resourcesKeys) {
                    String line = resourceKey + ": - " 
                        + threadsState_.get(resourceKey) + "\n";
                    fileWriter.write(line);
                }

                fileWriter.close();
                Thread.sleep(refreshPeriodicity_ * 1000);
            }
            catch(IOException e) {
                Logger.log(LogLevel.CRITIC, logPrefix_ 
                    + "Error writing stats file: " + e);
                System.exit(0);
            }            
            catch (InterruptedException e) {
                Logger.log(LogLevel.INFO, logPrefix_ 
                    + "Stopping Stats Manager Thread: " + e);
            }
        }
    }

    public void updateThreadState(String threadID, String value) {
        lock_.lock();
        String entryValue = threadsState_.get(threadID);
        if (entryValue == null) {
            threadsState_.put(threadID, value);
        }
        else {
            entryValue = value;
        }

        lock_.unlock();
    }

    public void updateURLDownloads() {
        lock_.lock();
        ++urlDownloadedCounter_;
        lock_.unlock();
    }

    public void updateResourceDownloads(String resource) {
        lock_.lock();
        Integer counter = resourcesInfo_.get(resource);

        if (counter != null) {
            resourcesInfo_.put(resource, new Integer(counter + 1));
        }
        else {
            resourcesInfo_.put(resource, new Integer(0));
        }
        lock_.unlock();
    }



    private Hashtable<String, String> threadsState_;
    private long urlDownloadedCounter_;
    private HashMap<String, Integer> resourcesInfo_;

    private String statsFile_ = ConfigParser.get("MONITOR-PARAMS",
                                                 "stats-file",
                                                 "/tmp/stats.txt");
    private int refreshPeriodicity_ = 
        Integer.parseInt(ConfigParser.get("MONITOR-PARAMS",
                                          "refresh-periodicity",
                                          "5"));
    private Lock lock_;
    private String logPrefix_;
}