package monitor;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.lang.System;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import configparser.ConfigParser;
import logger.Logger;
import logger.LogLevel;

public class StatsManager extends Thread {
    public StatsManager() {
        lock_ = new ReentrantReadWriteLock(true);
        threadsState_ = new Hashtable<String, String>();
        resourcesInfo_ = new HashMap<String, Integer>();
        logPrefix_ = "[STATS_MANAGER] ";
        urlDownloadedCounter_ = 0;
    }

    public void run() {
        while (! Thread.interrupted()) {
            try {
                // THREAD STATS
                lock_.readLock().lock();
                StringBuffer buffer = new StringBuffer();
                buffer.append("Amount URLs Downloaded: " 
                    + urlDownloadedCounter_ + "\n");

                this.prettifyResourcesStats(buffer);

                buffer.append("Resources Downloaded: \n");
                for (Map.Entry<String, Integer> entry : 
                    resourcesInfo_.entrySet()) {
                    buffer.append(entry.getKey() + ": - " 
                        + entry.getValue() + "\n");
                }

                // Finally block is not used because we need to free the lock
                // at this point. Lock while writing to disk is a bad idea
                lock_.readLock().unlock();
                this.updateStatsFile(buffer);
                Thread.sleep(refreshPeriodicity_ * 1000);
            }
            catch (InterruptedException e) {
                Logger.log(LogLevel.INFO, logPrefix_ 
                    + "Stopping Stats Manager Thread: " + e);
                lock_.readLock().unlock();
            }
        }
    }

    public void updateThreadState(String threadID, String value) {
        lock_.writeLock().lock();
        String entryValue = threadsState_.get(threadID);
        threadsState_.put(threadID, value);
        lock_.writeLock().unlock();
    }

    public void updateURLDownloads() {
        lock_.writeLock().lock();
        ++urlDownloadedCounter_;
        Logger.log(LogLevel.NOTICE, "[STATS] URL amount: " 
            + urlDownloadedCounter_);
        lock_.writeLock().unlock();
    }

    public void updateResourceDownloads(String resource) {
        lock_.writeLock().lock();
        Integer counter = resourcesInfo_.get(resource);

        if (counter != null) {
            resourcesInfo_.put(resource, new Integer(counter + 1));
        }
        else {
            resourcesInfo_.put(resource, new Integer(0));
        }
        lock_.writeLock().unlock();
    }

    private void prettifyResourcesStats(StringBuffer buffer) {
        buffer.append("THREAD STATES: \n");
        HashMap<String, Integer> prettyStats = new HashMap<String, Integer>();

        Set<String> keys = threadsState_.keySet();
        for (String key : keys) {
            String[] splitedKey = key.split("-");

            String prettyStatsKey = splitedKey[0] + " - "  
                + threadsState_.get(key);
            Integer prettyStatsValue = prettyStats.get(prettyStatsKey);

            if (prettyStats.get(prettyStatsKey) != null) {
                prettyStats.put(prettyStatsKey, prettyStatsValue + 1);
            }
            else {
                prettyStats.put(prettyStatsKey, 1);
            }
        }

        // Show the stats
        for (Map.Entry<String, Integer> entry : prettyStats.entrySet()) {
            String line = entry.getKey() + ": - " + entry.getValue() + "\n";
            buffer.append(line);
        }
    }

    private void updateStatsFile(StringBuffer buffer) {
        try {
            FileWriter fileWriter = new FileWriter(statsFile_, false);
            fileWriter.write(buffer.toString());
            fileWriter.flush();
            fileWriter.close();
        }
        catch(IOException e) {
            Logger.log(LogLevel.CRITIC, logPrefix_ 
                + "Error writing stats file: " + e);
            System.exit(0);
        }            
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
    private ReadWriteLock lock_;
    private String logPrefix_;
}