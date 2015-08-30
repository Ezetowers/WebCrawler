package WebCrawler;

import org.ini4j.Ini;
import Logger.Logger;
import Logger.LogLevel;
import java.io.*;

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
    	Ini configFile = MainClass.initConfigFile();
    	MainClass.initLogger(configFile);


    	Logger.getInstance().terminate();
    }
}
