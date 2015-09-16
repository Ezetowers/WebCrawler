package webcrawler;

import java.lang.Thread;
import java.lang.Runtime;

import configparser.ConfigParser;
import webcrawler.WebCrawler;
import logger.Logger;
import logger.LogLevel;

public class MainClass {
    public static void main(String[] args) {
        ConfigParser.init();
        MainClass.initLogger();

        // Attach a Hook who will be trigger by the JVM when the program
        // interrupted it's execution by a signal like SIGINT or SIGTERM.
        // THIS DOESN'T AFFECT THE WORK OF THREADS. THE APP ITSELF HAS THE 
        // RESPONSIBILITY OF STOP EVERY THREAD BEFORE QUIT. 
        // YOU HAVE BEEN WARNED.
        WebCrawler app = new WebCrawler();
        Runtime.getRuntime().addShutdownHook(app);
        app.crawl();
        try {
            app.waitThreads();
            app.join();
        }
        catch (InterruptedException e) {
        }


        Logger.log(LogLevel.NOTICE, "[MAIN CLASS] Stop crawling.");
        Logger.log(LogLevel.NOTICE, "[MAIN CLASS] Program finished.");
        Logger.terminate();
    }

    public static void initLogger() {
        String logFileName = ConfigParser.get("BASIC-PARAMS", "log-file");
        String logLevel = ConfigParser.get("BASIC-PARAMS", "log-level");
        Logger.init(logFileName, LogLevel.parse(logLevel));
    }
}