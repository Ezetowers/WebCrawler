package webcrawler.resource;

// Java imports
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;

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
import webcrawler.url.Depot;
import webcrawler.url.URLData;


public class ResourceDownloader extends Worker<String> {
    private final String USER_AGENT = 
        "Googlebot/2.1 (+http://www.google.com/bot.html)";

    public ResourceDownloader(long threadId, 
                              String logPrefix,
                              BlockingQueue<String> downloadQueue,
                              String resource) {
        super(threadId, logPrefix, downloadQueue);
        resource_ = resource;

        directory_ = ConfigParser.get("RESOURCE-PARAMS", "directory", "/tmp");
        directory_ += "/" + resource + "/";
        logPrefix_ += "[" + resource_.toUpperCase() + " DOWNLOADER] ";

    }

    public void execute() throws InterruptedException {
        String urlName = queue_.take();

        try {
            // Open connection
            URL url = new URL(urlName);
            HttpURLConnection connection;
            connection = (HttpURLConnection) url.openConnection();
            Logger.log(LogLevel.DEBUG, logPrefix_
                + "Connection was established succesfully");

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                Logger.log(LogLevel.ERROR, logPrefix_ 
                    + "HTTP GET Response arrived with errors. Code: " 
                    + responseCode);
                return;
            }
            Logger.log(LogLevel.DEBUG, logPrefix_ 
                + "Resource sucessfully downloaded");

            // Send the body to the parser
            BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine + "\n");
            }

            in.close();

            // Store the resource
            File file = new File(directory_ + url.toString().replace('/', '_'));

            // if file doesnt exists, then create it
            if (! file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(response.toString());
            bw.close();

        }
        catch (MalformedURLException e) {
            Logger.log(LogLevel.WARNING, logPrefix_ 
                + "Error forming URL: " + urlName);
        }
        catch (IOException e) {
            Logger.log(LogLevel.WARNING, logPrefix_ 
                + "Error while getting response: " + e.toString());
            Logger.log(LogLevel.NOTICE, "[RESOURCE] " + directory_ + urlName);
        }
    }

    private String urlLogPrefix_;
    private String resource_;
    private String directory_;
}
