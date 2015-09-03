package webcrawler;

// Java imports
import java.lang.Thread;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;

// External imports
import org.ini4j.Ini;

// Project imports
import concurrent.WorkersPool;
import logger.Logger;
import logger.LogLevel;
import webcrawler.url.analyzer.AnalyzerFactory;
import webcrawler.url.downloader.DownloaderFactory;
import webcrawler.url.parser.ParserFactory;


public class WebCrawler extends Thread {
    public void crawl() {
        // Init logger and config file
        pools_ = new ArrayList<WorkersPool>();
        Ini configFile = this.initConfigFile();

        this.initLogger(configFile);

        // Create the Thread pools
        ParserFactory parserFactory = new ParserFactory();
        DownloaderFactory downloaderFactory = new DownloaderFactory(parserFactory.getQueue());
        AnalyzerFactory analyzerFactory = new AnalyzerFactory(downloaderFactory.getQueue());
        parserFactory.setAnalyzerQueue(analyzerFactory.getQueue());

        int analyzerThreads = Integer.parseInt(configFile.get("POOL-PARAMS", "analyzer-threads"));
        int downloaderThreads = Integer.parseInt(configFile.get("POOL-PARAMS", "analyzer-threads"));
        int parserThreads = Integer.parseInt(configFile.get("POOL-PARAMS", "analyzer-threads"));

        WorkersPool<URL> analyzerPool = new WorkersPool<URL>(analyzerThreads, analyzerFactory);
        WorkersPool<URL> downloaderPool = new WorkersPool<URL>(downloaderThreads, downloaderFactory);
        WorkersPool<String> parserPool = new WorkersPool<String>(parserThreads, parserFactory);

        pools_.add(analyzerPool);
        pools_.add(downloaderPool);
        pools_.add(parserPool);

        this.startPools();

        // Trigger the program adding an URL to the Analyzer Pool
        try {
            URL url = new URL("http://www.atpworldtour.com");
            analyzerPool.addTask(url);
        }
        catch (MalformedURLException e) {
            System.err.println("[WEBCRAWLER] Error forming URL.");
            System.err.println(e);
        }


        while (! Thread.interrupted()) {
            try {
                Thread.sleep(1000); 
            }
            catch (InterruptedException e) {
            }
        }
    }

    public void run() {
        this.stopPools();
        Logger.getInstance().terminate();
    }

    private Ini initConfigFile() {
        // TODO: Receive the config file from an argument
        String configFileName = "configuration.ini";
        Ini configFile = new Ini();
        try {
            configFile.load(new FileReader(configFileName));
        }
        catch(IOException e) {
            System.err.println("[MAIN CLASS] Could not open config file.");
            System.err.println(e);
            System.exit(-1);
        } 
        return configFile;
    }

    private void initLogger(Ini configFile) {
        String logFileName = configFile.get("BASIC-PARAMS", "log_file");
        String logLevel = configFile.get("BASIC-PARAMS", "log_level");

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
