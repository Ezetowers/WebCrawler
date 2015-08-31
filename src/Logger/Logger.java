package Logger;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import Logger.LogLevel;


public class Logger {
	private Logger() {}

	public static Logger getInstance() {
		return logger_;
	}

	public static void init(String filePath, LogLevel verbosity) {
		verbosity_ = verbosity;
		try {
			// Open file in append mode
			fstream_ = new FileWriter(filePath, true);
			Logger.log(LogLevel.INFO, "Starting WebCrawler...");
		}
		catch(IOException e) {
			System.err.println("[LOGGER] Error calling init() method.");
			System.err.println(e);
		}
	}

	public static void terminate() {
		Logger.log(LogLevel.INFO, "Stopping WebCrawler...");
		try {
			fstream_.close();
		}
		catch(IOException e) {
			System.err.println("[LOGGER] Error calling terminate() method.");
			System.err.println(e);
			System.exit(-1);
		}		
	}

	public static void log(LogLevel verbosity, String msg) {
		if (verbosity_.level() >= verbosity.level()) {
			Logger.write(verbosity_.prefix(verbosity) + " " + msg);
		}
	}

	private static void write(String msg) {
		try {
			Date date = new Date();
			fstream_.write(dateFormat_.format(date) + " - " + msg + "\n");
			fstream_.flush();
		}
		catch(IOException e) {
			System.err.println("[LOGGER] Error calling write() method.");
			System.err.println(e);
			System.exit(-1);
		}
	}

	private static Logger logger_ = new Logger();
	private static DateFormat dateFormat_ = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); 
	private static LogLevel verbosity_;
	private static FileWriter fstream_;
}