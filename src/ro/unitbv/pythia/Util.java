/**
 * 
 */
package ro.unitbv.pythia;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lucian Sasu
 *
 */
public class Util {
	/**
	 * creates a string containing all values separated by space
	 */
	public static String toString(double[] vector, String separator) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < vector.length; i++) {
			result.append(vector[i] + separator);
		}
		return result.toString();
	}
	
	/**
	 * This method makes a "deep clone" of any Java object it is given.
	 */
	 public static Object deepClone(Object object) {
	   try {
	     ByteArrayOutputStream baos = new ByteArrayOutputStream();
	     ObjectOutputStream oos = new ObjectOutputStream(baos);
	     oos.writeObject(object);
	     ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	     ObjectInputStream ois = new ObjectInputStream(bais);
	     return ois.readObject();
	   }
	   catch (Exception e) {
	     e.printStackTrace();
	     return null;
	   }
	 }

	/**
	 * Performs a deep copy of the given source
	 * @param source the source list to be cloned
	 * @return a deep clone of the source list
	 */
	 public static List<FuzzyVector> copyListOfFuzzyVector(List<FuzzyVector> source) {
		if (source == null)
		{
			return null;
		}
		List<FuzzyVector> result = new ArrayList<FuzzyVector>(source.size());
		for(FuzzyVector item : source)
		{
			result.add(item.clone());
		}
		return result;
	}
	
	/**
	 * Performs a safe caopy of an array
	 * @param src source object
	 * @param src_position from where to start copying
	 * @param dest destination object
	 * @param dest_pos destination start
	 * @param length how many items to copy
	 */
	 public static void safeArrayCopy( Object src, int src_position, Object dest,
            int dest_pos, int length )
	{
		if (src != null && dest != null) {
			System.arraycopy(src, src_position, dest, dest_pos, length);
		}
	}
	
	/**
	 * Generic shallow copy method
	 * @param source the source list
	 * @return a shallow copy of source list
	 */
	 public static <T> List<T> copyList(List<T> source) {
		if (source == null)
		{
			return null;
		}
		List<T> result = new ArrayList<T>(source.size());
		for(T item : source)
		{
			result.add(item);
		}
		return result;
	}
	 
	 /**
	  * Computes the minimum of a vector
	  * @param vector a non-null vector
	  * @return vector's minimum
	  */
	 public static double min(double[] vector)
	 {
		 double min = Double.POSITIVE_INFINITY;
		 for(double value : vector)
		 {
			 min = Math.min(value,  min);
		 }
		 return min;
	 }
	 
	 /**
	  * Computes the minimum of a vector
	  * @param vector a non-null vector
	  * @return vector's maximum
	  */
	 public static double max(double[] vector)
	 {
		 double max = Double.NEGATIVE_INFINITY;
		 for(double value : vector)
		 {
			 max = Math.max(value,  max);
		 }
		 return max;
	 }

	 /**
	  * Throws runtime exception if at least one of the vector's items is outside the interval [0, 1]
	  * @param vector the non-null vector to be scanned
	 * @param exceptionMessage 
	  */
	public static void checkScaled(double[] vector, String exceptionMessage) {
		if (Settings.debugMode == false)
		{
			return;
		}
		for(double value : vector)
		{
			if (value < 0.0 || value > 1.0)
			{
				throw new RuntimeException(exceptionMessage);
			}
		}
		
	}
}
