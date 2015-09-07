package webcrawler;

// Java imports
import java.lang.Thread;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

// Project imports
import concurrent.WorkersPool;
import configparser.ConfigParser;
import logger.Logger;
import logger.LogLevel;
import webcrawler.url.analyzer.Analyzer;
import webcrawler.url.analyzer.AnalyzerFactory;
import webcrawler.url.Depot;
import webcrawler.url.downloader.DownloaderFactory;
import webcrawler.url.parser.ParserFactory;
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

        // Create the Thread pools
        ParserFactory parserFactory = new ParserFactory();
        DownloaderFactory downloaderFactory = new DownloaderFactory(parserFactory.getQueue(), depot);
        AnalyzerFactory analyzerFactory = new AnalyzerFactory(downloaderFactory.getQueue(), depot);
        parserFactory.setAnalyzerQueue(analyzerFactory.getQueue());

        int analyzerThreads = Integer.parseInt(ConfigParser.get("POOL-PARAMS", "analyzer-threads", "1"));
        int downloaderThreads = Integer.parseInt(ConfigParser.get("POOL-PARAMS", "downloader-threads", "1"));
        int parserThreads = Integer.parseInt(ConfigParser.get("POOL-PARAMS", "parser-threads", "1"));

        WorkersPool<String> analyzerPool = new WorkersPool<String>(analyzerThreads, analyzerFactory);
        WorkersPool<URL> downloaderPool = new WorkersPool<URL>(downloaderThreads, downloaderFactory);
        WorkersPool<URLData> parserPool = new WorkersPool<URLData>(parserThreads, parserFactory);

        pools_.add(analyzerPool);
        pools_.add(downloaderPool);
        pools_.add(parserPool);

        this.startPools();

        // Trigger the program adding an URL to the Analyzer Pool
        analyzerPool.addTask(ConfigParser.get("BASIC-PARAMS", "initial-url", "http://www.atpworldtour.com"));

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
