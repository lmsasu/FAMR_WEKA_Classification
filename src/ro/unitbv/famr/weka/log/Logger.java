package ro.unitbv.famr.weka.log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import ro.unitbv.famr.weka.general.Settings;

/**
 * 
 * @author Lucian Sasu
 * Hand-made logger
 * Writes to a text file, if the enableLog field is set to true
 * The default path is given by the field defaultLogPath
 * The actual path is given by the field logPath
 * The logged data: [current datetime] followed by the message passed to the log method 
 */
public class Logger {
//	private static String logPath;
//	public static final String defaultLogPath = "d:\\temp\\BA_wekalog.txt";
	public static final String defaultLogPath = Settings.logPath;
	private static boolean enableLog = true;

	private static BufferedWriter out;

	/**
	 * Writes a message to the file whose path is specified by the logPath field
	 * The data is flushed after each message
	 * @param message the message to be written
	 */
	public static void log(String message) {
		if (enableLog == false) {
			return;
		}
//		if (logPath == null) {
//			logPath = defaultLogPath;
//		}
		try {
			if (out == null) {
				open();
			}
			out.write("[" + new Date().toString() + "]: " + message + "\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void open() throws IOException {
		out = new BufferedWriter(new FileWriter(Settings.logPath, true));
	}

//	public static void setLogPath(String str) {
//		logPath = str;
//	}

	public static void setEnabled(boolean enableLog) {
		Logger.enableLog = enableLog;
	}
	
	public static void close()
	{
		if (out != null)
		{
			try {
				out.close();
				out = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
