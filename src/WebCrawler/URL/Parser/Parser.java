package webcrawler.url.parser;

// import java.lang.reflect.Field
import java.lang.System;

// Java imports
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileWriter;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.Hashtable;

// Project imports
import concurrent.Worker;
import logger.Logger;
import logger.LogLevel;

import webcrawler.url.URLData;
import webcrawler.url.parser.matchers.ResourceMatcher;
import webcrawler.url.parser.matchers.URLResourceMatcher;
import webcrawler.url.parser.matchers.ImgResourceMatcher;
import webcrawler.url.parser.matchers.JavascriptResourceMatcher;
import webcrawler.url.parser.matchers.CSSResourceMatcher;


public class Parser extends Worker<URLData> {
    public Parser(long threadId, 
                  String logPrefix, 
                  BlockingQueue<URLData> parserQueue,
                  BlockingQueue<String> analyzerQueue,
                  Hashtable<String, BlockingQueue<String> > resourceQueues) {
        super(threadId, logPrefix, parserQueue);
        analyzerQueue_ = analyzerQueue;
        resourceQueues_ = resourceQueues;
        logPrefix_ += "[PARSER] ";

        // Chain of Responsibility used to see if there is any resource to 
        // parse in the body
        ResourceMatcher urlMatcher = new URLResourceMatcher();
        ResourceMatcher imgMatcher = new ImgResourceMatcher();
        ResourceMatcher jsMatcher = new JavascriptResourceMatcher();
        ResourceMatcher cssMatcher = new CSSResourceMatcher();

        urlMatcher.setNext(imgMatcher);
        imgMatcher.setNext(jsMatcher);
        jsMatcher.setNext(cssMatcher);
        chain_ = urlMatcher;
    }

    public void execute() throws InterruptedException {
        URLData urldata = queue_.take();
        urlLogPrefix_ = logPrefix_ + "[URL: " + urldata.url + "] ";
        this.parseBody(urldata);
    }

    private void parseBody(URLData urlData) {
        String[] lines = urlData.body.split("\n");

        startTime_ = System.currentTimeMillis();
        for (String line : lines) {
            String[] resourceMatched = new String[1];

            ResourceMatcher.ResourceMatched matched = 
                chain_.match(urlData.url, line, resourceMatched);
            try {
                switch (matched) {
                    case URL:
                        Logger.log(LogLevel.TRACE, urlLogPrefix_ 
                            + "URL parsed: " + resourceMatched[0]);
                        analyzerQueue_.put(resourceMatched[0]);
                        break;
                    case IMG:
                    case DOC:
                    case CSS:
                    case JS:
                    
                        Logger.log(LogLevel.TRACE, urlLogPrefix_ 
                            + matched.toString() + " parsed: " 
                            + resourceMatched[0]);

                        resourceQueues_.get(matched.toString()).
                            put(resourceMatched[0]);
                        break;
                    case UNKNOWN:
                        break;
                }
            }
            catch (InterruptedException e) {
                Logger.log(LogLevel.ERROR, "Could not insert resource in " 
                    + matched.toString() + "queue.");
            }
        }
        elapsedTime_ = System.currentTimeMillis() - startTime_;
        Logger.log(LogLevel.DEBUG, urlLogPrefix_ 
            + "Time elapsed processing URL: " + elapsedTime_ + " ms.");
    }

    private BlockingQueue<String> analyzerQueue_;
    private Hashtable<String, BlockingQueue<String> > resourceQueues_;
    private String urlLogPrefix_;
    private ResourceMatcher chain_;

    // For performance stats
    private long startTime_;
    private long elapsedTime_;
}
