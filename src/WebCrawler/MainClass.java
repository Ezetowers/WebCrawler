package webcrawler;

// Java imports
import java.lang.Thread;
import java.io.*;

// External imports
import org.ini4j.Ini;

// Project imports
import concurrent.Workers_Pool;
import logger.Logger;
import logger.LogLevel;
import webcrawler.url.URL;
import webcrawler.url.analyzer.URL_Analyzer_Factory;

public class MainClass {
    public static Ini initConfigFile() {
    	// TODO: Receive the config file from an argument
    	String configFileName = "configuration.ini";
    	Ini configFile = new Ini();
    	try {
    		configFile.load(new FileReader(configFileName));
    	}
		catch(IOException e) {
			System.err.println("[MAIN CLASS] Could not open config file.");
			System.err.println(e);
			System.exit(-1);
		} 
    	return configFile;
    }

   	public static void initLogger(Ini configFile) {
   		String logFileName = configFile.get("BASIC-PARAMS", "log_file");
    	String logLevel = configFile.get("BASIC-PARAMS", "log_level");

        Logger logger = Logger.getInstance();
        logger.init(logFileName, LogLevel.parse(logLevel));
   	}

    public static void main(String[] args) {
        // Init logger and config file
    	Ini configFile = MainClass.initConfigFile();
    	MainClass.initLogger(configFile);

        // Create a Thread Pool
        URL_Analyzer_Factory analyzer_factory = new URL_Analyzer_Factory();
        Workers_Pool<URL> analyzer_pool = new Workers_Pool<URL>(5, analyzer_factory);

        analyzer_pool.start();
        try {
            Thread.sleep(10000);
        }
        catch (InterruptedException e) {            
        }

        analyzer_pool.stop();
    	Logger.getInstance().terminate();
    }
}
