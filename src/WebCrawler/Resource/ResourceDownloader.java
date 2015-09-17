package webcrawler.resource;

// Java imports
import java.io.*;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

// Project imports
import concurrent.Worker;
import configparser.ConfigParser;
import logger.Logger;
import logger.LogLevel;
import monitor.MonitorEvent;
import webcrawler.url.Depot;
import webcrawler.url.URLData;


public class ResourceDownloader extends Worker<String> {
    private final String USER_AGENT = 
        "Googlebot/2.1 (+http://www.google.com/bot.html)";

    public ResourceDownloader(long threadId, 
                              String logPrefix,
                              BlockingQueue<String> downloadQueue,
                              BlockingQueue<MonitorEvent> monitorQueue,
                              String resource) {
        super(threadId, logPrefix, downloadQueue);
        monitorQueue_ = monitorQueue;
        resource_ = resource;

        directory_ = ConfigParser.get("RESOURCE-PARAMS", "directory", "/tmp");
        directory_ += "/" + resource + "/";
        logPrefix_ += "[" + resource_.toUpperCase() + " DOWNLOADER] ";

    }

    public void execute() throws InterruptedException {
        MonitorEvent.sendStatusEvent(monitorQueue_,
                                     resource_ + "-RESOURCE-" + threadId_, 
                                     "DEQUEING");
        String urlName = queue_.take();

        try {
            MonitorEvent.sendStatusEvent(monitorQueue_,
                                         resource_ + "-RESOURCE-" + threadId_, 
                                         "CONNECTING");

            URL url = new URL(urlName);
            InputStream in = this.retrieveResource(url);
            if (in != null) {
                MonitorEvent.sendStatusEvent(monitorQueue_,
                                             resource_ + "-RESOURCE-" 
                                             + threadId_, 
                                             "STORING");

                if (this.storeResource(url, in)) {
                    MonitorEvent.sendResourceMsg(monitorQueue_, resource_);
                }
            }
        }
        catch (MalformedURLException e) {
            Logger.log(LogLevel.WARNING, logPrefix_ 
                + "Error forming URL: " + urlName);
        }
        catch (IOException e) {
            Logger.log(LogLevel.WARNING, logPrefix_ 
                + "Error while getting response: " + e.toString());
        }
    }

    private InputStream retrieveResource(URL url) throws IOException, 
                                                         InterruptedException {
        HttpURLConnection connection;
        connection = (HttpURLConnection) url.openConnection();
        Logger.log(LogLevel.DEBUG, logPrefix_
            + "Connection was established succesfully");

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);

        MonitorEvent.sendStatusEvent(monitorQueue_,
                                     resource_ + "-RESOURCE-" + threadId_, 
                                     "DOWNLOADING");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            Logger.log(LogLevel.ERROR, logPrefix_ 
                + "HTTP GET Response arrived with errors. Code: " 
                + responseCode);
            return null;
        }
        Logger.log(LogLevel.DEBUG, logPrefix_ 
            + "Resource sucessfully downloaded");

        InputStream in = new BufferedInputStream(connection.getInputStream());
        return in;
    }

    private boolean storeResource(URL url, InputStream in) throws IOException {
        // Check if file exists
        String resourceFileName = this.createResourceFilename(url);
        File file = new File(resourceFileName);

        // If file doesn't exists, then create it
        if (! file.exists()) {
            file.createNewFile();
        }
        else {
            Logger.log(LogLevel.DEBUG, logPrefix_ 
                + "Cannot store resource. " 
                + "Another resource with the same name exists. "
                + "Resource: " + resourceFileName);
            return false;
        }

        // Retrieve the file and store it in a buffer
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;

        while (-1 != (n = in.read(buf))) {
           out.write(buf, 0, n);
        }

        out.close();
        in.close();

        FileOutputStream fos = new FileOutputStream(resourceFileName);
        fos.write(out.toByteArray());
        fos.close();
        return true;
    }

    /**
     * @brief Format filename 
     * @details Some characters make the OS to interpret the file with 
     * undesirable formats. We want to remove those characters
     * 
     * @param Filename to be formatted
     * @return Clean filename
     */
    private String createResourceFilename(URL url) {
        // FIXME: Find a prettier way to do this
        String filename = directory_ + url.toString().replace("/", "");
        filename = filename.replace(":", "");
        filename = filename.replace(",", "");
        filename = filename.replace("-", "");
        filename = filename.replace("_", "");
        return filename;
    }

    private BlockingQueue<MonitorEvent> monitorQueue_;
    private String urlLogPrefix_;
    private String resource_;
    private String directory_;
}
