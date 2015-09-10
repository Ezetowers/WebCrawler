package webcrawler;

// Java imports
import java.lang.Thread;
import java.io.*;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.ArrayList;
import java.util.Hashtable;

// Project imports
import concurrent.WorkersPool;
import configparser.ConfigParser;
import logger.Logger;
import logger.LogLevel;
import monitor.MonitorFactory;
import monitor.MonitorEvent;
import monitor.StatsManager;
import webcrawler.resource.ResourceFactory;
import webcrawler.url.analyzer.Analyzer;
import webcrawler.url.analyzer.AnalyzerFactory;
import webcrawler.url.Depot;
import webcrawler.url.downloader.DownloaderFactory;
import webcrawler.url.parser.ParserFactory;
import webcrawler.url.parser.matchers.ResourceMatcher;
import webcrawler.url.URLData;


public class WebCrawler extends Thread {
    public void crawl() {
        // Init logger and config file
        pools_ = new ArrayList<WorkersPool>();
        ConfigParser.init();
        this.initLogger();
        this.recreateEnviroment();
        Depot depot = new Depot();

        int monitorThreads = 
            Integer.parseInt(ConfigParser.get("MONITOR-PARAMS", 
                                              "monitor-threads", 
                                              "1"));
        // Monitor objects
        statsManager_ = new StatsManager();
        MonitorFactory monitorFactory = new MonitorFactory(statsManager_);
        WorkersPool<MonitorEvent> monitorPool = 
            new WorkersPool<MonitorEvent>(monitorThreads, monitorFactory);
        BlockingQueue<MonitorEvent> monitorQueue = monitorFactory.getQueue();
        pools_.add(monitorPool);

        // Resource objects
        int imgResourceThreads = 
            Integer.parseInt(ConfigParser.get("RESOURCE-PARAMS", 
                                              "img-threads", 
                                              "1"));
        int cssResourceThreads = 
            Integer.parseInt(ConfigParser.get("RESOURCE-PARAMS", 
                                              "css-threads", 
                                              "1"));
        int jsResourceThreads = 
            Integer.parseInt(ConfigParser.get("RESOURCE-PARAMS", 
                                              "js-threads", 
                                              "1"));
        int docResourceThreads = 
            Integer.parseInt(ConfigParser.get("RESOURCE-PARAMS", 
                                              "doc-threads", 
                                              "1"));

        // Create the Resources Thread Pools
        ResourceFactory imgFactory = new ResourceFactory(
            ResourceMatcher.ResourceMatched.IMG.toString());
        ResourceFactory jsFactory = new ResourceFactory(
            ResourceMatcher.ResourceMatched.JS.toString());
        ResourceFactory cssFactory = new ResourceFactory(
            ResourceMatcher.ResourceMatched.CSS.toString());
        ResourceFactory docFactory = new ResourceFactory(
            ResourceMatcher.ResourceMatched.DOC.toString());

        WorkersPool<String> imgPool = 
            new WorkersPool<String>(imgResourceThreads, imgFactory);
        WorkersPool<String> jsPool = 
            new WorkersPool<String>(cssResourceThreads, cssFactory);
        WorkersPool<String> cssPool = 
            new WorkersPool<String>(jsResourceThreads, jsFactory);
        WorkersPool<String> docPool = 
            new WorkersPool<String>(docResourceThreads, docFactory);

        pools_.add(imgPool);
        pools_.add(jsPool);
        pools_.add(cssPool);
        pools_.add(docPool);

        Hashtable<String, BlockingQueue<String> > resourceQueue =
            new Hashtable<String, BlockingQueue<String> >();
        resourceQueue.put(ResourceMatcher.ResourceMatched.IMG.toString(),
                          imgFactory.getQueue());
        resourceQueue.put(ResourceMatcher.ResourceMatched.CSS.toString(),
                          cssFactory.getQueue());
        resourceQueue.put(ResourceMatcher.ResourceMatched.JS.toString(),
                          jsFactory.getQueue());
        resourceQueue.put(ResourceMatcher.ResourceMatched.DOC.toString(),
                          docFactory.getQueue());


        // Create the URL Thread pools
        ParserFactory parserFactory = new ParserFactory(resourceQueue,
                                                        monitorQueue);
        DownloaderFactory downloaderFactory = 
            new DownloaderFactory(parserFactory.getQueue(), 
                                  monitorQueue, 
                                  depot);
        AnalyzerFactory analyzerFactory = 
            new AnalyzerFactory(downloaderFactory.getQueue(), 
                                monitorQueue,
                                depot);
        parserFactory.setAnalyzerQueue(analyzerFactory.getQueue());

        int analyzerThreads = Integer.parseInt(
            ConfigParser.get("URL-PARAMS", "analyzer-threads", "1"));
        int downloaderThreads = Integer.parseInt(
            ConfigParser.get("URL-PARAMS", "downloader-threads", "1"));
        int parserThreads = Integer.parseInt(
            ConfigParser.get("URL-PARAMS", "parser-threads", "1"));

        WorkersPool<URLData> analyzerPool = 
            new WorkersPool<URLData>(analyzerThreads, analyzerFactory);
        WorkersPool<URLData> downloaderPool = 
            new WorkersPool<URLData>(downloaderThreads, downloaderFactory);
        WorkersPool<URLData> parserPool = 
            new WorkersPool<URLData>(parserThreads, parserFactory);

        pools_.add(analyzerPool);
        pools_.add(downloaderPool);
        pools_.add(parserPool);

        statsManager_.start();
        this.startPools();

        // Trigger the program adding an URL to the Analyzer Pool
        String initialUrl = ConfigParser.get("URL-PARAMS", 
                                             "initial-url", 
                                             "http://www.atpworldtour.com");
        URLData initialData = new URLData(0, initialUrl);
        analyzerPool.addTask(initialData);

        while (! Thread.interrupted() && Analyzer.continueAnalyzing()) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                break;
            }
        }
    }

    public void run() {
        try {
            this.stopThreads();
        }
        catch (InterruptedException e) {
        }

    }

    public void stopThreads() throws InterruptedException {
        statsManager_.interrupted();
        statsManager_.join();
        this.stopPools();
        Logger.getInstance().terminate();
    }

    private void initLogger() {
        String logFileName = ConfigParser.get("BASIC-PARAMS", "log-file");
        String logLevel = ConfigParser.get("BASIC-PARAMS", "log-level");

        Logger logger = Logger.getInstance();
        logger.init(logFileName, LogLevel.parse(logLevel));
    }

    private void startPools() {
        for (WorkersPool pool : pools_) {
            pool.start();
        }
    }

    public void stopPools() {
        for (WorkersPool pool : pools_) {
            pool.stop();
        }
    }

    /**
     * @brief Call this function to erase all the resources and files created 
     * in previous runs of the program and create new empty directories
     */
    private boolean recreateEnviroment() {
        // First erase previous files
        String resourceDirPath = ConfigParser.get("RESOURCE-PARAMS", "directory");
        File dir = new File(resourceDirPath);
        this.deleteDirectory(dir);

        // Erase stats file
        File statsFile = new File(ConfigParser.get("MONITOR-PARAMS",
                                                   "stats-file",
                                                   "/tmp/stats.txt"));
        if (statsFile.exists()) {
            statsFile.delete();
        }

        // Then create them again
        boolean dirCreated = false;
        try {
            dirCreated = dir.mkdir();
            if (dirCreated) {
                // Create the Folders for every resource to download later
                for (ResourceMatcher.ResourceMatched resourceDir : 
                     ResourceMatcher.ResourceMatched.values()) {
                    File file = new File(dir, resourceDir.toString());
                    file.mkdir();
                }
            }
        }
        catch(SecurityException e) {
            Logger.log(LogLevel.CRITIC, 
                "[RESOURCES] Cannot create resources folders. " + e);
            return false;
        }

        return true;
    }


    private boolean deleteDirectory(File dir) {
        if (! dir.exists() || ! dir.isDirectory()) {
            return false;
        }

        String[] children = dir.list();
        for (int i = 0; i < children.length; ++i) {
            File file = new File(dir, children[i]);
            if (file.isDirectory()) {
                this.deleteDirectory(file);
            }
            else {
                file.delete();
            }
        }

        return dir.delete();
    }

    private ArrayList<WorkersPool> pools_;
    private StatsManager statsManager_;
}
