package webcrawler.url.downloader;

// Java imports
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

// Project imports
import concurrent.Worker;
import logger.Logger;
import logger.LogLevel;
import webcrawler.url.Depot;


public class Downloader extends Worker<URL> {
    private final String USER_AGENT = "Googlebot/2.1 (+http://www.google.com/bot.html)";

    public Downloader(long threadId, 
                      String logPrefix, 
                      BlockingQueue<URL> downloadQueue,
                      BlockingQueue<String> parseQueue,
                      Depot depot) {
        super(threadId, logPrefix, downloadQueue);
        parseQueue_ = parseQueue;
        depot_ = depot;

        logPrefix_ += "[DOWNLOADER] ";
    }

    public void execute() throws InterruptedException {
        // TODO: Add logic
        URL url = queue_.take();
        urlLogPrefix_ = logPrefix_ + "[URL: " + url.toString() + "] ";

        // Open connection
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
            Logger.log(LogLevel.DEBUG, urlLogPrefix_ + "Connection was established succesfully");

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
        }
        catch (IOException e) {
            Logger.log(LogLevel.ERROR, urlLogPrefix_ + "Connection error: " + e.toString());
            depot.alter(url.toString(), Depot.URLArchiveState.UNREACHABLE);
            return;
        }

        // Retrieve the URL body
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                Logger.log(LogLevel.ERROR, urlLogPrefix_ + "HTTP GET Response arrived with errors. Code: " + responseCode);
                depot.alter(url.toString(), Depot.URLArchiveState.UNREACHABLE);
                return;
            }
            Logger.log(LogLevel.DEBUG, urlLogPrefix_ + "Response Code: " + responseCode);
        }
        catch (IOException e) {
            Logger.log(LogLevel.ERROR, urlLogPrefix_ + "Error while getting response: " + e.toString());
            depot.alter(url.toString(), Depot.URLArchiveState.UNREACHABLE);
        }

        // Send the body to the parser
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            parseQueue_.put(response.toString());
        }
        catch (IOException e) {
            Logger.log(LogLevel.ERROR, urlLogPrefix_ + "Error while getting response: " + e.toString());
        }
    }

    private BlockingQueue<String> parseQueue_;
    private Depot depot_;
    private String urlLogPrefix_;
}
