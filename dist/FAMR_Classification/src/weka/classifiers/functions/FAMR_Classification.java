/*
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    BayesianArtmapChinezi.java
 *    Copyright (C) 2011 Lucian Sasu & Razvan Andonie
 *
 */
package weka.classifiers.functions;

import weka.classifiers.AbstractClassifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import ro.unitbv.famr.weka.general.Settings;
import ro.unitbv.famr.weka.log.Logger;
import ro.unitbv.pythia.FAMR;
import ro.unitbv.pythia.Pattern;

/**
 * <!-- globalinfo-start --> Implements a Bayesian ARTMAP.<br/>
 * <br/>
 * <!-- globalinfo-end -->
 * 
 * <!-- technical-bibtex-start --> BibTeX:
 * 
 * <pre>
 *@article{
 * 	author = {Boaz Vigdor and Boaz Lerner},
 * 	title = {The Bayesian ARTMAP},
 * 	journal = {IEEE Transactions on Neural Networks},
 * 	volume = {18},
 * 	year = {2007},
 * 	pages = {1628--1644},
 * 	doi = {10.1109/TNN.2007.900234},
 * 	masid = {4432118}
 * }
 * 
 * </pre>
 * 
 * <!-- technical-bibtex-end -->
 * 
 * <!-- options-start --> Valid options are:
 * 
 * 
 * <pre>
 * -a &lt;double&gt;
 *  log base 10 of SMaxA
 *  (default 0)
 * </pre>
 * 
 * <pre>
 * -b &lt;double&gt;
 *  log base 10 of SMaxB
 *  (default -2)
 * </pre>
 * 
 * <pre>
 * -p &lt;double&gt;
 *  PMin
 *  (default 0.4)
 * </pre>
 * 
 * <pre>
 * -logPath &lt;String&gt;
 *  logPath
 *  (default as set in Settings.logPath)
 * </pre>
 * 
 * <pre>
 * -i &lt;int&gt;
 *  iterations
 *  (default 1)
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * @author Lucian Sasu (lmsasu at yahoo.com)
 * @version $Revision: 1 $
 */
public class FAMR_Classification extends AbstractClassifier implements
		TechnicalInformationHandler, Serializable {

	/** for serialization */
	static final long serialVersionUID = 2L;

	protected FAMR famr;
	protected double iMin = 0.1;//min input value
	protected double iMax = 7.9;//max input value
	protected double rhoInitA = 0.8;
	protected double betaA = 1;
	protected double rhoAB = 0.0;
	protected int epochs = 1;
	
	protected static final String defaultLogPath = Settings.logPath; 
	protected String logPath = defaultLogPath;
	protected boolean enableLog = true;

	private boolean isRegressionProblem = false;
	
	public FAMR_Classification()
	{
	}

	/**
	 * Returns a string describing classifier
	 * 
	 * @return a description suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return "Implements a FAMR network for classification. For constant relevance factor (defaulted to 1 in this implementation), FAMR is equivalent to PROBART";
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing detailed
	 * information about the technical background of this class, e.g., paper
	 * reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;

		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "Andonie Razvan and Sasu Lucian");
		result.setValue(Field.YEAR, "2006");
		result.setValue(Field.TITLE, "Fuzzy ARTMAP with Relevance Factor");
		result.setValue(Field.JOURNAL, "IEEE Transactions on Neural Networks");
		result.setValue(Field.VOLUME, "17(4)");
//		result.setValue(Field.PAGES, "1628-1644");

		return result;
	}

	/**
	 * Returns an enumeration describing the available options
	 * 
	 * @return an enumeration of all the available options
	 */
	public Enumeration<Option> listOptions() {
		Vector<Option> newVector = new Vector<Option>(7);

		newVector.addElement(new Option("\tiMin.\n" + "\t(default " + iMin + ")",
				"iMin", 1, "-iMin <double>"));
		newVector.addElement(new Option("\tiMax.\n" + "\t(default " + iMax + ")",
				"iMax", 1, "-iMax <double>"));
		
//		newVector.addElement(new Option("\toMin.\n" + "\t(default " + oMin + ")",
//				"oMin", 1, "-oMin <int>"));
//		newVector.addElement(new Option("\toMax.\n" + "\t(default " + oMax + ")",
//				"oMax", 1, "-oMax <int>"));
		
		newVector.addElement(new Option("\ta.\n" + "\t(default " + rhoInitA + ")",
				"a", 1, "-a <double>"));
		newVector.addElement(new Option("\tc.\n" + "\t(default " + betaA + ")",
				"c", 1, "-c <double>"));
		
		newVector.addElement(new Option("\tr.\n" + "\t(default " + rhoAB + ")",
				"r", 1, "-r <double>"));
		
		newVector.addElement(new Option("\tlogPath.\n" + "\t(default " + defaultLogPath +" )",
				"logPath", 1, "-logPath <string>"));
		newVector.addElement(new Option("\tenableLog.\n" + "\t(default " + enableLog + ")",
				"enableLog", 1, "-enableLog <boolean>"));
		
		newVector.addElement(new Option("\ti.\n" + "\t(default " + epochs + ")",
				"i", 1, "-i <int>"));
		
		return newVector.elements();
	}

	/**
	 * <p>
	 * Parses a given list of options.
	 * 
	 * 
	 * <p>
	 * <!-- options-start --> Valid options are:
	 * 
	 * 
	 * <pre>
	 * -p &lt;double&gt;
	 *  the pmin threshold used in mapfield.
	 *  (default 0.4)
	 * </pre>
	 * 
	 * <pre>
	 * -a &lt;double&gt;
	 *  the logarithm in base 10 of smaxa threshold used in art_a.
	 *  (default 0)
	 * </pre>
	 * 
	 * <pre>
	 * -b &lt;double&gt;
	 *  the logarithm in base 10 of smaxb threshold used in art_b.
	 *  (default -2)
	 * </pre>
	 * 
	 * 	<pre>
	 * -logPath &lt;String&gt;
	 *  the absolute path up to the log file
	 *  (default Settings.logPath)
	 * </pre>
	 * 
	 * 	<pre>
	 * -enableLog &lt;boolean&gt;
	 *  whether the log is enabled
	 *  (default false)
	 * </pre>
	 * 
	 *  <pre>
	 * -i &lt;int&gt;
	 *  number of iterations on the training set
	 *  (default 1)
	 * </pre>
	 * 
	 * <!-- options-end -->
	 * 
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {
		String path = Utils.getOption("logPath", options);
		if (path.length() != 0)
		{
			setLogPath(path);
		}
		
		String strEnableLog = Utils.getOption("enableLog", options);
		if (strEnableLog.length() != 0)
		{
			boolean enableLog = Boolean.valueOf(strEnableLog);
			setEnableLog(enableLog);
		}
		
		String iMinString = Utils.getOption("iMin", options);
		if (iMinString.length() != 0) {
			double iMinDouble = (new Double(iMinString)).doubleValue();
			setIMin(iMinDouble);
		}

		String iMaxString = Utils.getOption("iMax", options);
		if (iMaxString.length() != 0) {
			double iMaxDouble = (new Double(iMaxString)).doubleValue();
			setIMax(iMaxDouble);
		}
		
//		String oMinString = Utils.getOption("oMin", options);
//		if (oMinString.length() != 0) {
//			int oMinInteger = (new Integer(oMinString)).intValue();
//			setOMin(oMinInteger);
//		}

//		String oMaxString = Utils.getOption("oMax", options);
//		if (oMaxString.length() != 0) {
//			int oMaxInteger = (new Integer(oMaxString)).intValue();
//			setOMax(oMaxInteger);
//		}
		
		String rhoInitAString = Utils.getOption("a", options);
		if (rhoInitAString.length() != 0) {
			double rhoInitADouble = (new Double(rhoInitAString)).doubleValue();
			setRhoInitA(rhoInitADouble);
		}

		String betaAString = Utils.getOption("c", options);
		if (betaAString.length() != 0) {
			double betaADouble = (new Double(betaAString)).doubleValue();
			setBetaA(betaADouble);
		}

		String rhoABString = Utils.getOption("r", options);
		if (rhoABString.length() != 0) {
			double rhoABDouble = (new Double(rhoABString)).doubleValue();
			setRhoAB(rhoABDouble);
		}
		
		String iterationsStr = Utils.getOption("i", options);
		if (iterationsStr.length() != 0) {
			int iters = (new Integer(iterationsStr)).intValue();
			setIterations(iters);
		}
	}

	/**
	 * Gets the current settings of the classifier.
	 * 
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String[] getOptions() {

		Vector<String> result = new Vector<String>();
		
		result.add("-iMin");
		result.add("" + getIMin());
		
		result.add("-iMax");
		result.add("" + getIMax());
		
//		result.add("-oMin");
//		result.add("" + getOMin());
		
//		result.add("-oMax");
//		result.add("" + getOMax());
		
		result.add("-a");
		result.add("" + getRhoInitA());
		
		result.add("-c");
		result.add("" + getBetaA());
		
		result.add("-r");
		result.add("" + getRhoAB());
		
		result.add("-logPath");
		result.add("" + getLogPath());
		
		result.add("-enableLog");
		result.add("" + getEnableLog());
		
		result.add("-i");
		result.add("" + getIterations());

		result.addAll(Arrays.asList(super.getOptions())); // super class' options
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Returns default capabilities of the classifier.
	 * 
	 * @return the capabilities of this classifier
	 */
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();

		// attributes
		result.enable(Capability.NUMERIC_ATTRIBUTES);

		// class
//		result.enable(Capability.NUMERIC_CLASS);
		result.enable(Capability.NOMINAL_CLASS);

		// instances
		result.setMinimumNumberInstances(0);

		return result;
	}

	/**
	 * Builds the classifier
	 * 
	 * @param trainingSet
	 *            the data to train the classifier with
	 * @throws Exception
	 *             if something goes wrong during building
	 */
	public void buildClassifier(Instances trainingSet) throws Exception {
		
		// can classifier handle the data?
		getCapabilities().testWithFail(trainingSet);

		// remove instances with missing class
		trainingSet = new Instances(trainingSet);
		trainingSet.deleteWithMissingClass();
		
//		String fileName = createFile(insts);
		
		famr = new FAMR(this.getRhoInitA(), this.getBetaA(), this.getRhoAB(), this.getIterations(), this.getIMin(), this.getIMax(), trainingSet.numClasses());
		
		List<Pattern> patterns = ro.unitbv.famr.weka.Utils.getPatternsFromInstances(trainingSet);
		
		famr.train(patterns);
		
		isRegressionProblem = trainingSet.classAttribute().isNumeric();
		
		Logger.log("insts.classAttribute().isNumeric()= " + trainingSet.classAttribute().isNumeric());
		
		Logger.log("rho_a= " + getRhoInitA());
		Logger.log("rho_ab= " + getRhoAB());
		Logger.log("beta_a= " + getBetaA());
		
		Logger.log("after training: ");
		Logger.log("input categories= " + famr.getInputCategoriesNo());
	}

	/**
	 * Outputs the prediction for the given instance.
	 * 
	 * @param instance
	 *            the instance for which prediction is to be computed
	 * @return the prediction
	 * @throws Exception
	 *             if something goes wrong
	 */
	public double classifyInstance(Instance instance) throws Exception {
//		Logger.log("****input categories= " + bayesianArtmap.getInputCategoriesCount() + " outputCategories= " + bayesianArtmap.getOutputCategoriesCount());
		if(isRegressionProblem)
		{
//			double[] input = new double[instance.numAttributes() - 1];
//			for(int i=0; i<input.length; i++)
//			{
//				input[i] = instance.value(i);
//			}
//			//regression
//			double[] estimation = famr.approximateUnscaledReturnUnscaled(input);
////			System.out.println("to be estimated: " + instance.toString());
////			System.out.println("estimation: " + estimation[0] + "\r\n");
//			return estimation[0];
			throw new RuntimeException("not yet implemented");
		}
		else
		{
//			//eroare uriasa....
////			double[] estimatedConditionalProbabilities = bayesianArtmap.estimateClassification(patternsPair.getInputPattern());
////			int estimatedClass = ro.unitbv.general.Utils.getPosMax(estimatedConditionalProbabilities); 
//			
//			int estimatedClass = famr.estimateOutestimateOutputClassputClass(patternsPair.getInputPattern());
//			return estimatedClass;
//			throw new RuntimeException("not yet implemented");
			Pattern pattern = ro.unitbv.famr.weka.Utils.getPatternFromInstance(instance);
			int estimatedClass= famr.classifySingleInstance(pattern);
			return estimatedClass;
		}
	}
	
	@Override
//	public double[] distributionForInstance(Instance instance) throws Exception {
//		Pattern pattern = ro.unitbv.famr.weka.Utils.getPatternFromInstance(instance);
//		pattern.scaleInput(this.iMin, this.iMax);
//		return famr.getProbVector(pattern);
//	}

	/**
	 * Returns textual description of the classifier.
	 * 
	 * @return textual description of the classifier
	 */
	public String toString() {
		return "Fuzzy ARTMAP with relevance factor\n\n";
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String iMinTipText() {
		return "iMin parameter.";
	}

	/**
	 * Get the value of iMin.
	 * 
	 * @return Value of iMin.
	 */
	public double getIMin() {

		return iMin;
	}

	/**
	 * Set the value of iMin.
	 * 
	 * @param a
	 *            Value to assign to iMin.
	 */
	public void setIMin(double a) {
		iMin = a;
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String oMinTipText() {
		return "oMin parameter.";
	}
	
//	/**
//	 * Get the value of oMin.
//	 * 
//	 * @return Value of oMin.
//	 */
//	public int getOMin() {
//
//		return oMin;
//	}

//	/**
//	 * Set the value of oMin.
//	 * 
//	 * @param a
//	 *            Value to assign to oMin.
//	 */
//	public void setOMin(int a) {
//		oMin = a;
//	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String iMaxTipText() {
		return "iMax parameter.";
	}

	/**
	 * Get the value of iMax.
	 * 
	 * @return Value of iMax.
	 */
	public double getIMax() {
		return iMax;
	}

	/**
	 * Set the value of iMax.
	 * 
	 * @param a
	 *            Value to assign to iMax.
	 */
	public void setIMax(double a) {
		iMax = a;
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String oMaxTipText() {
		return "oMax parameter.";
	}
	
//	/**
//	 * Get the value of oMax.
//	 * 
//	 * @return Value of oMax.
//	 */
//	public int getOMax() {
//		return oMax;
//	}

//	/**
//	 * Set the value of oMax.
//	 * 
//	 * @param a
//	 *            Value to assign to oMax.
//	 */
//	public void setOMax(int a) {
//		oMax = a;
//	}
//	
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String rhoInitATipText() {
		return "rhoInitA parameter.";
	}

	/**
	 * Get the value of rhoInitA.
	 * 
	 * @return Value of rhoInitA.
	 */
	public double getRhoInitA() {
		return rhoInitA;
	}

	/**
	 * Set the value of rhoInitA.
	 * 
	 * @param a
	 *            Value to assign to rhoInitA.
	 */
	public void setRhoInitA(double a) {
		rhoInitA = a;
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String rhoInitBTipText() {
		return "rhoInitB parameter.";
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String betaATipText() {
		return "betaA parameter.";
	}

	/**
	 * Get the value of betaA.
	 * 
	 * @return Value of betaA.
	 */
	public double getBetaA() {

		return betaA;
	}

	/**
	 * Set the value of betaA.
	 * 
	 * @param a
	 *            Value to assign to betaA.
	 */
	public void setBetaA(double a) {
		betaA = a;
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String betaBTipText() {
		return "betaB parameter.";
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String rhoABTipText() {
		return "rhoAB parameter.";
	}

	/**
	 * Get the value of rhoAB.
	 * 
	 * @return Value of rhoAB.
	 */
	public double getRhoAB() {

		return rhoAB;
	}

	/**
	 * Set the value of rhoAB.
	 * 
	 * @param a
	 *            Value to assign to rhoAB.
	 */
	public void setRhoAB(double a) {
		rhoAB = a;
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return the path for storing a log file
	 */
	public String logPathTipText() {
		return "The path where the log file resides.";
	}

	/**
	 * Set the value of pathForLog.
	 * 
	 * @param path
	 *            Value to assign to pathForLog.
	 */
	public void setLogPath(String path) {
		logPath = path;
//		Logger.setLogPath(path);
		Settings.logPath = path;
	}

	/**
	 * Get the value of logPath.
	 * 
	 * @return Value of logPath.
	 */
	public String getLogPath() {
		return logPath;
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return the path for storing a log file
	 */
	public String enableLogTipText() {
		return "Whether logging is enabled or not.";
	}

	/**
	 * Set the value of pathForLog.
	 * 
	 * @param enableLog
	 *            Value to assign to pathForLog.
	 */
	public void setEnableLog(boolean enableLog) {
		this.enableLog = enableLog;
	}

	/**
	 * Get the value of enableLog.
	 * 
	 * @return Value of enableLog.
	 */
	public boolean getEnableLog() {
		return enableLog;
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return the number of iterations to be performed on the training test
	 */
	public String iterationsTipText() {
		return "The number of iterations.";
	}

	/**
	 * Set the value of iterations.
	 * 
	 * @param iterations
	 *            Value to assign to iterations.
	 */
	public void setIterations(int iterations) {
		this.epochs = iterations;
	}
	
	/**
	 * Get the value of iterations.
	 * 
	 * @return Value of iterations.
	 */
	public int getIterations() {
		return epochs;
	}

	/**
	 * Returns the revision string.
	 * 
	 * @return the revision
	 */
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 2$");
	}
}