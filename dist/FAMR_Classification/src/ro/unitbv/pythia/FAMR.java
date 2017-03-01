package ro.unitbv.pythia;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Title:        Modified Fuzzy Artmap
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

/**
 * This is a modified Fuzzy Artmap model implementation for classification. The
 * essential modification, compared to Carpenter's et al. model is in class
 * Mapfield, where the values w_ab have a completely different meaning and
 * formula.
 */
public class FAMR implements Serializable {

	private static final long serialVersionUID = 2L;
	private FuzzyArt art_a = null;
	private FuzzyVector art_b_classification = null;
	private MapField mapField = null;
	private int global_n_pairs = 0;
	private double rho_init_a;
	private double beta_a;
	private double rho_ab;
	private int epochs;
	private double iMin;
	private double iMax;
	private int outputDim;

	/**
	 * 
	 * @param rho_init_a
	 *            baseline vigilance parameter
	 * @param beta_a
	 *            used for category learning
	 * @param rho_ab
	 *            mapfield threshold
	 * @param epochs
	 *            how many iterations over the training set
	 * @param iMin
	 *            minimum input value
	 * @param iMax
	 *            maximum output value
	 * @param outputDim
	 *            maximum number of classes
	 */
	public FAMR(double rho_init_a, // needed in ART_a
			double beta_a, // needed in ART_a
			double rho_ab, // needed in MapField
			int epochs, // training epochs
			double iMin, //min input, for scaling in [0, 1] 
			double iMax, //max input, for scaling in [0, 1]
			int outputDim //how many classes; the classes must be 0..outputDim-1
			) {

		this.rho_init_a = rho_init_a;
		this.beta_a = beta_a;
		this.rho_ab = rho_ab;
		this.epochs = epochs;
		this.iMin = iMin;
		this.iMax = iMax;
		this.outputDim = outputDim;

		art_a = new FuzzyArt(this.rho_init_a, this.beta_a);
		art_b_classification = new FuzzyVector(this.outputDim);
		mapField = new MapField(this.rho_ab, this.outputDim);
	}

	/**
	 * Performs FAMR training based on list of input-output patterns
	 * 
	 * @param patterns
	 *            the training dataset
	 */
	public void train(List<Pattern> patterns) {
		int n_pairs = 0;
		int n_rejected_pairs = 0;
		
		Logger.println("Start training the classifier");
		
		Logger.println("this.iMin= " + this.iMin + "; this.iMax= " + this.iMax);
		
		for (Pattern pattern : patterns) {
			pattern.scaleInput(this.iMin, this.iMax);
		}
		
		Logger.saveToFile("train", patterns);

		for (int i = 0; i < epochs; i++) {
			for (Pattern pattern : patterns) {
				double scaledInput[] = pattern.getScaledInput();
				Util.checkScaled(scaledInput, "At train, the input part of the given patetrn is not between 0 and 1.");
				int classLabel = pattern.getClassIndex();
				double q_t = pattern.getWeight();

				if (!trainPair(scaledInput, classLabel, q_t)) {
					n_rejected_pairs++;
				}
				if (i == 0) {
					n_pairs++;
					global_n_pairs++;
				}
			}
		}
		Logger.println("End of " + epochs + " epochs of trainig. " + "We have obtained " + art_a.numCategories()
				+ " input categories and " + art_b_classification.length + " output categories\n" + "using " + n_pairs
				+ " training pairs\n" + "Number of rejected pairs is " + n_rejected_pairs + "\n"
				+ "Historical number of processed pairs: " + global_n_pairs + "\n");
	}

	/**
	 * Return true if training pair (input[], K), with relevance factor q_t was
	 * learned (correctly or not).
	 * 
	 * @param scaledInput
	 *            the input to be used for training
	 * @param K
	 *            the label associated with the current input
	 * @param q_t
	 *            the current relevance
	 * @return true if the current pattern could be learned, false otherwise
	 */
	private boolean trainPair(double[] scaledInput, int K, double q_t) {

		MapField old_mapField = mapField.clone(); // save MapField
		FuzzyArt old_art_a = art_a.clone(); // save art_a
		art_a.newInput(scaledInput);

		// the output is one-hot encoded
		FuzzyVector art_b = new FuzzyVector(outputDim);
		art_b.initValue(0);
		art_b.setValueAtPos(K, 1);

		art_a.restoreRho();
		while (true) {
			int J = art_a.findCategory();
			if (J == -1)
			// no suitable category found in art_a; create a new category
			{
				art_a.createNewCategory();
				mapField.addWeight_a();
				J = art_a.numCategories() - 1;
			}
			if (mapField.accept(art_b, J)) {
				// learn current pair
				art_a.learn(J);
				mapField.learn(J, K, q_t);
				return true;
			} else {
				art_a.increaseRho(J);
				if (art_a.getRho() > 1) {
					// reject current pair and restore art_a and mapField
					art_a = old_art_a;
					mapField = old_mapField;
					return false;
				} else {
					continue; // reiterate current input vector
				}
			}
		}
	}


	/**
	 * Use the trained Fuzzy Artmap network for classifying a set of input
	 * patterns. This is a maximum-likelihood Bayesian classificator, using the
	 * maximum of the estimated conditional probabilities.
	 */

	/**
	 * Classifies the current pattern
	 * 
	 * @param toBeClassified
	 *            reference to the pattern to be classified
	 * @return the estimated label
	 */
	public int classifySingleInstance(Pattern toBeClassified) {
		this.art_a.setRhoToZero();
		toBeClassified.scaleInput(this.iMin, this.iMax);
		Logger.debug_print("test", toBeClassified);
		Util.checkScaled(toBeClassified.getScaledInput(), "At classification: the input part of the pattern is not scaled in [0, 1].");
		return classifyInputVector(toBeClassified.getScaledInput());
	}

	

	/**
	 * Tells whether the current pattern is correctly classified
	 * 
	 * @param pattern
	 *            the input to be classified
	 * @return true if the pattern is correctly classified, false otherwise
	 */
	public boolean correctlyClassifiesPattern(Pattern pattern) {
		return pattern.getClassIndex() == classifySingleInstance(pattern);
	}

	/**
	 * Return the index of the class assigned to pattern input[]. This is a
	 * maximum-likelihood Bayesian classificator, using the maximum of the
	 * estimated conditional probabilities
	 * 
	 * @param scaledInput
	 *            the input to be classified
	 * @return the inferred label
	 */
	private int classifyInputVector(double[] scaledInput) {
		art_a.newInput(scaledInput);
		int J = art_a.findCategory();
		if (J == -1) {
			System.err.println("In classifyInputVector: will return -1");
			return -1;
		} else {
			return mapField.getw_ab(J).posMax();
		}
	}

	/**
	 * Return the conditional probabilities P( output class | input[] ) for all
	 * output classes.
	 * 
	 * @param scaledInput
	 *            the scaled input value
	 * @param outputDim
	 *            the number of classes
	 * @return a vector of conditional probabilities
	 */
	private double[] getProbVector(double scaledInput[]) {
		Util.checkScaled(scaledInput, "In getProbVector: the scaledInput vector is not between 0 and 1.");
		art_a.newInput(scaledInput);
		int J = art_a.findCategory();
		double result[] = new double[this.outputDim];
		if (J == -1) {
			Arrays.fill(result, 1.0 / this.outputDim);
		} else {
			System.arraycopy(mapField.getw_ab(J).v, 0, result, 0, this.outputDim);
		}
		return result;
	}

	// /**
	// * TODO: Find the most connected category in art_a to output class K
	// */

	/**
	 * @return Returns the art_a.
	 */
	public FuzzyArt getArt_a() {
		return art_a;
	}

	/**
	 * @return Returns the art_b.
	 */
	public FuzzyVector getArt_b() {
		return art_b_classification;
	}

	/**
	 * @return Returns the epochs value.
	 */
	public int getEpochs() {
		return this.epochs;
	}

	/**
	 * @return Returns the global_n_pairs.
	 */
	public int getGlobal_n_pairs() {
		return global_n_pairs;
	}

	/**
	 * @return Returns the mapField.
	 */
	public MapField getMapField() {
		return mapField;
	}

	/**
	 * Computes the accuracy for the given set
	 * 
	 * @param patterns
	 *            the test set
	 * @return percentage of correctly classified patterns
	 */
	public double computeAccuracy(List<Pattern> patterns) {
		int correctlyClassified = 0;
		for (Pattern pattern : patterns) {
			if (correctlyClassifiesPattern(pattern)) {
				++correctlyClassified;
			}
		}
		return (double) correctlyClassified / patterns.size();
	}

	/**
	 * Return the number of input categories
	 * 
	 * @return the number of input categories
	 */
	public int getInputCategoriesNo() {
		if (art_a == null) {
			return -1;
		}
		return art_a.numCategories();
	}
}