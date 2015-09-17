package webcrawler.url.downloader;

// Java imports
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.concurrent.BlockingQueue;

// Project imports
import concurrent.Worker;
import monitor.MonitorEvent;
import logger.Logger;
import logger.LogLevel;
import webcrawler.url.Depot;
import webcrawler.url.URLData;


public class Downloader extends Worker<URLData> {
    private final String USER_AGENT = 
        "Googlebot/2.1 (+http://www.google.com/bot.html)";

    public Downloader(long threadId, 
                      String logPrefix, 
                      BlockingQueue<URLData> downloadQueue,
                      BlockingQueue<URLData> parseQueue,
                      BlockingQueue<MonitorEvent> monitorQueue,
                      Depot depot) {
        super(threadId, logPrefix, downloadQueue);
        parseQueue_ = parseQueue;
        monitorQueue_ = monitorQueue;
        depot_ = depot;

        logPrefix_ += "[DOWNLOADER] ";
    }

    public void execute() throws InterruptedException {
        try {
            MonitorEvent.sendStatusEvent(monitorQueue_,
                                 "DOWNLOADER-" + threadId_, 
                                 "DEQUEING");

            URLData urlData = queue_.take();
            URL url = new URL(urlData.url);

            MonitorEvent.sendStatusEvent(monitorQueue_,
                                 "DOWNLOADER-" + threadId_, 
                                 "CONNECTING");

            urlLogPrefix_ = logPrefix_ + "[URL: " + url.toString() + "] ";
            InputStream in = this.downloadResource(url);

            if (in != null) {
                MonitorEvent.sendStatusEvent(monitorQueue_,
                                     "DOWNLOADER-" + threadId_, 
                                     "RETRIEVING");
                urlData.body = this.retrieveResource(in);
                MonitorEvent.sendURLDownloadMsg(monitorQueue_);
                parseQueue_.put(urlData);
            }
        }
        catch (MalformedURLException e) {
            // This cannot happen because this case was checked in 
            // the analyzer thread
            Logger.log(LogLevel.CRITIC, urlLogPrefix_ 
                + "Malformed URL. Aborting program");
            System.exit(-1);
        }
        catch (IOException e) {
            Logger.log(LogLevel.ERROR, urlLogPrefix_ 
                + "Error while getting response: " + e.toString());
        }
    }

    private InputStream downloadResource(URL url) throws IOException, 
                                                         InterruptedException {
        // Open connection
        HttpURLConnection connection;
        connection = (HttpURLConnection) url.openConnection();
        Logger.log(LogLevel.DEBUG, urlLogPrefix_ 
            + "Connection was established succesfully");

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);

        MonitorEvent.sendStatusEvent(monitorQueue_,
                             "DOWNLOADER-" + threadId_, 
                             "DOWNLOADING");

        // Retrieve the URL body
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            Logger.log(LogLevel.ERROR, urlLogPrefix_ 
                + "HTTP GET Response arrived with errors. Code: " 
                + responseCode);
            depot_.alter(url.toString(), 
                         Depot.URLArchivedState.UNREACHABLE);
            return null;
        }
        Logger.log(LogLevel.DEBUG, urlLogPrefix_ 
            + "URL sucessfully downloaded");
        depot_.alter(url.toString(), Depot.URLArchivedState.DOWNLOADED);

        InputStream in = new BufferedInputStream(connection.getInputStream());
        return in;
    }

    private String retrieveResource(InputStream in) throws IOException {
        // Retrieve the file and store it in a buffer
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;

        while (-1 != (n = in.read(buf))) {
           out.write(buf, 0, n);
        }

        out.close();
        in.close();
        return new String(out.toByteArray());
    }

    private BlockingQueue<URLData> parseQueue_;
    private BlockingQueue<MonitorEvent> monitorQueue_;
    private Depot depot_;
    private String urlLogPrefix_;
}
