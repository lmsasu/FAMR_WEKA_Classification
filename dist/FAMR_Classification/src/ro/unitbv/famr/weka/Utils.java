/**
 * 
 */
package ro.unitbv.famr.weka;

import java.util.ArrayList;
import java.util.List;

import ro.unitbv.famr.weka.log.Logger;
import ro.unitbv.pythia.Pattern;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author ro1v0393
 *
 */
public class Utils {
	public static List<Pattern> getPatternsFromInstances(Instances instances)
	{
		List<Pattern> patterns = new ArrayList<Pattern>(instances.numInstances());
		
		for(Instance instance : instances)
		{
			Pattern pattern = getPatternFromInstance(instance);
			patterns.add(pattern);
			Logger.log(pattern.toString());
		}
		
		return patterns;
	}

	private static double[] getInput(Instance instance) {
		double[] result = new double[instance.numAttributes() - 1];
		
		for(int i=0; i<result.length; i++)
		{
			result[i] = instance.value(i);
		}
		
		return result;
	}

	public static Pattern getPatternFromInstance(Instance instance) {
		Pattern pattern = new Pattern();
		
		pattern.setClassificationInstance(instance.classAttribute().isNominal());
		pattern.setClassIndex((int)instance.classValue());
		
		pattern.setInput(getInput(instance));
		pattern.setWeight(instance.weight());
		
		return pattern;
	}
}
