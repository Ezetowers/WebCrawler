package monitor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.System;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

import configparser.ConfigParser;
import logger.Logger;
import logger.LogLevel;

public class StatsManager extends Thread {
    public StatsManager() {
        lock_ = new ReentrantLock();
        logPrefix_ = "[STATS_MANAGER] ";
    }

    public void run() {
        while (! Thread.interrupted()) {
            try {
                // THREAD STATS
                FileWriter fileWriter = new FileWriter(statsFile_, false);
                fileWriter.write("Amount URLs Downloaded: " 
                    + urlDownloadedCounter_);
                fileWriter.write("THREAD STATES: ");

                Set<String> keys = threadsState_.keySet();
                for (String key : keys) {
                    String line = key + ": - " + threadsState_.get(key);
                    fileWriter.write(line);
                }

                Set<String> resourcesKeys = resourcesInfo_.keySet();
                for (String resourceKey : resourcesKeys) {
                    String line = resourceKey + ": - " 
                        + threadsState_.get(resourceKey);
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
            ++counter;
        }
        else {
            counter = new Integer(0);
            resourcesInfo_.put(resource, counter);
        }
        lock_.unlock();
    }



    private Hashtable<String, String> threadsState_;
    public long urlDownloadedCounter_;
    public Hashtable<String, Integer> resourcesInfo_;

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