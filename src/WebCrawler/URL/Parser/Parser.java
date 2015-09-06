package webcrawler.url.parser;

// import java.lang.reflect.Field
import java.lang.System;

// Java imports
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

// Project imports
import concurrent.Worker;
import logger.Logger;
import logger.LogLevel;
import webcrawler.url.URLData;


public class Parser extends Worker<URLData> {
    public Parser(long threadId, 
                  String logPrefix, 
                  BlockingQueue<URLData> parserQueue,
                  BlockingQueue<String> analyzerQueue) {
        super(threadId, logPrefix, parserQueue);
        analyzerQueue_ = analyzerQueue;
        logPrefix_ += "[PARSER] ";
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
            this.searchHyperlinkTags(urlData.url, line);

            /* startTime_ = System.currentTimeMillis();
            // this.searchImgTags(urlData.url, line);
            elapsedTime_ = System.currentTimeMillis() - startTime_;*/
        }
        elapsedTime_ = System.currentTimeMillis() - startTime_;
        Logger.log(LogLevel.DEBUG, urlLogPrefix_ + "Time elapsed processing URL: " + elapsedTime_ + " ms.");
    }

    private void searchHyperlinkTags(String url, String line) {
        Pattern pattern = Pattern.compile("href=\"([^\"]*)\" ");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String urlMatched = matcher.group(1);
            if (urlMatched.startsWith("/")) {
                urlMatched = url + urlMatched;
            }
            else if (urlMatched.endsWith(".css")) {
                // TODO:
                return;
            }
            else if (! urlMatched.startsWith("http://") && ! urlMatched.startsWith("https://")) {
                Logger.log(LogLevel.DEBUG, urlLogPrefix_ + "Don't know how to parse this URL: " + urlMatched);
                return;
            }

            Logger.log(LogLevel.DEBUG, urlLogPrefix_ + "URL parsed: " + urlMatched);
            try {
                analyzerQueue_.put(urlMatched);
            }
            catch (InterruptedException e) {
                Logger.log(LogLevel.ERROR, urlLogPrefix_ + "Could not add URL to Analyzer queue. Error: " + e);
            }
        }
    }

    private BlockingQueue<String> analyzerQueue_;
    private String urlLogPrefix_;

    // For performance stats
    private long startTime_;
    private long elapsedTime_;
}




    /*private void parseBody(URLData urldata) {
        try { 
            // Get the array of chars of the string with Reflection. See this topic for more information:
            // http://stackoverflow.com/questions/8894258/fastest-way-to-iterate-over-all-the-chars-in-a-string
            Field field = String.class.getDeclaredField("value");
            field.setAccessible(true);
            char[] chars = (char[]) field.get(urldata.body);

            int len = chars.length;
            Logger.log(LogLevel.INFO, urlLogPrefix_ + "Start to process URL Body");

            // TODO: Suppose that the webpage downloaded is wellformed
            // Start from the 6th character to compare 'href' in the loop without adding
            // a condition checking a special case in the first six caracteres
            for (int i = 6; i < len; ++i) {
                // TODO: This is ugly :(, think another way of do this
                i = this.searchHyperlinkTags(urldata.url, chars, i);
                i = this.searchImgTags(urldata.url, chars, i);
            }
            Logger.log(LogLevel.INFO, "[PARSER] End processing URL Body");
        }
        catch (NoSuchFieldException e) {
            Logger.log(LogLevel.ERROR, urlLogPrefix_ + ":" + e);
        }
        catch (IllegalAccessException e) {
            Logger.log(LogLevel.ERROR, urlLogPrefix_ + ":" + e);
        }
    }

    private int searchHyperlinkTags(String url, char[] chars, int i) {
        String urlParsed = new String();

        if (chars[i - 6] == 'h' && chars[i - 5] == 'r' &&
            chars[i - 4] == 'e' && chars[i - 3] == 'f' &&
            chars[i - 2] == '=' && chars[i - 1] == '"') {

            while (chars[i] != '"') {
                urlParsed += chars[i];
                ++i;
            }

            if (urlParsed.startsWith("/")) {
                urlParsed = url + urlParsed;
            }
            else if (urlParsed.equals("#")) {
                // TODO: Investigate if this kind of URLs give us more information
                // Format of this <a...> tags
                // <a href="#" some-property="relative-url">
                ++i;
                while (chars[i] != '"') {
                    ++i;
                }

                ++i;
                urlParsed = "";
                urlParsed = urldata.url + "#";
                while (chars[i] != '"') {
                    newString += chars[i];
                    ++i;
                }
            } 
            else if (! urlParsed.startsWith("http://")) {
                Logger.log(LogLevel.DEBUG, urlLogPrefix_ + "Don't know how to parse this URL: " + urlParsed);
                return i;
            }

            Logger.log(LogLevel.DEBUG, urlLogPrefix_ + "URL found: " + urlParsed);
            try {
                analyzerQueue_.put(urlParsed);
            }
            catch (InterruptedException e) {
                Logger.log(LogLevel.ERROR, urlLogPrefix_ + "Could not add URL to Analyzer queue. Error: " + e);
            }
        }

        return i;
    }

    private int searchImgTags(String url, char[] chars, int i) {
        // TODO:
        return i;
    }*/