package webcrawler;

import java.lang.Thread;
import java.lang.Runtime;
import webcrawler.WebCrawler;

public class MainClass {
    public static void main(String[] args) throws Exception {
        WebCrawler app = new WebCrawler();
        Runtime.getRuntime().addShutdownHook(app);
        app.crawl();
        app.join();
    }
}