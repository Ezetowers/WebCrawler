package webcrawler.url.parser;

import java.lang.reflect.Field;

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

        Logger.log(LogLevel.DEBUG, urlLogPrefix_ + "A packet arrived.");
        Logger.log(LogLevel.TRACE, logPrefix_ + urldata.body);

        this.parseBody(urldata);
    }

    private void parseBody(URLData urldata) {
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
            /* else if (urlParsed.equals("#")) {
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
            } */ 
            else if (! urlParsed.startsWith("http://")) {
                Logger.log(LogLevel.DEBUG, urlLogPrefix_ + "Don't know how to parse this URL: " + urlParsed);
                return i;
            }

            Logger.log(LogLevel.DEBUG, urlLogPrefix_ + "URL found: " + urlParsed);
            try {
                analyzerQueue_.put(urlParsed);
            }
            catch (InterruptedException e ) {
                Logger.log(LogLevel.ERROR, urlLogPrefix_ + "Could not add URL to Analyzer queue. Error: " + e);
            }
        }

        return i;
    }

    private int searchImgTags(String url, char[] chars, int i) {
        // TODO:
        return i;
    }

    private BlockingQueue<String> analyzerQueue_;
    private String urlLogPrefix_;
}