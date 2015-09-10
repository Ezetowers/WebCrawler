package webcrawler;

import java.lang.Thread;
import java.lang.Runtime;

import webcrawler.WebCrawler;
import logger.Logger;
import logger.LogLevel;

public class MainClass {
    public static void main(String[] args) {
        WebCrawler app = new WebCrawler();
        // Runtime.getRuntime().addShutdownHook(app);
        app.crawl();
        Logger.log(LogLevel.NOTICE, "[MAIN CLASS] Stop crawling.");
        try {
            app.join();
            Logger.log(LogLevel.NOTICE, "[MAIN CLASS] Program finished.");
            app.stopThreads();
        }
        catch (InterruptedException e) {
            System.out.println("[MAIN CLASS] Program interrupted.");
        }
    }
}