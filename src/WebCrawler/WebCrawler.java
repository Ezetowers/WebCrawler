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
        Depot depot = new Depot(ConfigParser.get("BASIC-PARAMS", 
                                                 "url-depot-filename", 
                                                 "/tmp/urlDepot.txt"));

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
        resourceQueue.put(ResourceMatcher.ResourceMatched.JS.toString(),
                          cssFactory.getQueue());
        resourceQueue.put(ResourceMatcher.ResourceMatched.CSS.toString(),
                          jsFactory.getQueue());
        resourceQueue.put(ResourceMatcher.ResourceMatched.DOC.toString(),
                          docFactory.getQueue());


        // Create the URL Thread pools
        ParserFactory parserFactory = new ParserFactory(resourceQueue);
        DownloaderFactory downloaderFactory = 
            new DownloaderFactory(parserFactory.getQueue(), depot);
        AnalyzerFactory analyzerFactory = 
            new AnalyzerFactory(downloaderFactory.getQueue(), depot);
        parserFactory.setAnalyzerQueue(analyzerFactory.getQueue());

        int analyzerThreads = Integer.parseInt(
            ConfigParser.get("URL-PARAMS", "analyzer-threads", "1"));
        int downloaderThreads = Integer.parseInt(
            ConfigParser.get("URL-PARAMS", "downloader-threads", "1"));
        int parserThreads = Integer.parseInt(
            ConfigParser.get("URL-PARAMS", "parser-threads", "1"));

        WorkersPool<String> analyzerPool = 
            new WorkersPool<String>(analyzerThreads, analyzerFactory);
        WorkersPool<URL> downloaderPool = 
            new WorkersPool<URL>(downloaderThreads, downloaderFactory);
        WorkersPool<URLData> parserPool = 
            new WorkersPool<URLData>(parserThreads, parserFactory);

        pools_.add(analyzerPool);
        pools_.add(downloaderPool);
        pools_.add(parserPool);

        this.startPools();

        // Trigger the program adding an URL to the Analyzer Pool
        analyzerPool.addTask(ConfigParser.get("BASIC-PARAMS", 
                                              "initial-url", 
                                              "http://www.atpworldtour.com"));

        while (! Thread.interrupted() && Analyzer.continueAnalyzing()) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                break;
            }
        }

        // Dump the content of the Depot to check how the run of the program was
        depot.dump();
    }

    public void run() {
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

    private void stopPools() {
        for (WorkersPool pool : pools_) {
            pool.stop();
        }
    }

    private ArrayList<WorkersPool> pools_;

}
