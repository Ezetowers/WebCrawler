package webcrawler.resource;

// Java imports
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
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
        String resourceFileName = "";

        try {
            MonitorEvent.sendStatusEvent(monitorQueue_,
                                         resource_ + "-RESOURCE-" + threadId_, 
                                         "CONNECTING");
            // Open connection
            URL url = new URL(urlName);
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
                return;
            }
            Logger.log(LogLevel.DEBUG, logPrefix_ 
                + "Resource sucessfully downloaded");

            MonitorEvent.sendResourceMsg(monitorQueue_, resource_);
            MonitorEvent.sendStatusEvent(monitorQueue_,
                                         resource_ + "-RESOURCE-" + threadId_, 
                                         "STORING");

            InputStream in = new BufferedInputStream(
                connection.getInputStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;

            while (-1 != (n = in.read(buf))) {
               out.write(buf, 0, n);
            }

            out.close();
            in.close();
            byte[] response = out.toByteArray();

            // Send the body to the parser
            /* BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();*/

            // FIXME: Find a prettier way to do this
            resourceFileName = directory_ + url.toString().replace("/", "");
            resourceFileName = resourceFileName.replace(":", "");
            resourceFileName = resourceFileName.replace(",", "");
            resourceFileName = resourceFileName.replace("-", "");
            resourceFileName = resourceFileName.replace("_", "");
            File file = new File(resourceFileName);

            // if file doesnt exists, then create it
            if (! file.exists()) {
                file.createNewFile();
            }
            else {
                Logger.log(LogLevel.DEBUG, logPrefix_ 
                    + "Cannot store resource. " 
                    + "Another resource with the same name exists. "
                    + "Resource: " + resourceFileName);
            }

            FileOutputStream fos = new FileOutputStream(resourceFileName);
            fos.write(response);
            fos.close();

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

    private BlockingQueue<MonitorEvent> monitorQueue_;
    private String urlLogPrefix_;
    private String resource_;
    private String directory_;
}
