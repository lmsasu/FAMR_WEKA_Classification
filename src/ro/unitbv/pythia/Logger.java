/**
 * 
 */
package ro.unitbv.pythia;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author ro1v0393
 *
 */
public class Logger {

	public static void debug_print(String filename, Pattern pattern) {
		if (Settings.debugMode == false)
		{
			return;
		}
		String path = getUserHomePath();
		String fullPath = path + filename + ".debug.txt";
		try(FileWriter fw = new FileWriter(fullPath, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
			    out.println(pattern.toString_scaled());
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * Prints a message on stderr, if the debug mode is enabled
	 * @param message
	 */
	public static void println(String message) {
		if (Settings.debugMode)
		{
			System.err.println(message);
		}
	}
	
	public static void saveToFile(String filename, List<Pattern> patterns) {
		if (Settings.debugMode == false)
		{
			return;
		}
		String path = getUserHomePath();
		String fullPath = path + filename + ".debug.txt";
		try(BufferedWriter out = new BufferedWriter(new FileWriter(fullPath)))
		{
			for(Pattern pattern : patterns)
			{
				out.write(pattern.toString_scaled() + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the user home path with a trailing / or \
	 */
	private static String getUserHomePath() {
		return System.getProperty("user.home") + File.separator;
	}

	
}
