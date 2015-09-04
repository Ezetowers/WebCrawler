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
	private final String USER_AGENT = "Mozilla/5.0";

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
        Logger.log(LogLevel.DEBUG, logPrefix_ + "Proceed to process an URL " + url.toString());

        HttpURLConnection connection;
        try {
        	connection = (HttpURLConnection) url.openConnection();
        	connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", USER_AGENT);
        }
        catch (IOException e) {
        	Logger.log(LogLevel.ERROR, logPrefix_ + "Connection error: " + e.toString());
        	return;
        }

        try {
        	int responseCode = connection.getResponseCode();
        	if (responseCode != 200) {
        		Logger.log(LogLevel.ERROR, logPrefix_ + "HTTP GET Response arrived with errors. Code: " + responseCode);
        		return;
        	}
        	Logger.log(LogLevel.DEBUG, logPrefix_ + "Response Code: " + responseCode);
        }
        catch (IOException e) {
        	Logger.log(LogLevel.ERROR, logPrefix_ + "Error while getting response: " + e.toString());
        }

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
        	Logger.log(LogLevel.ERROR, logPrefix_ + "Error while getting response: " + e.toString());
        }
    }

    private BlockingQueue<String> parseQueue_;
    private Depot depot_;
}