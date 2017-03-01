package ro.unitbv.pythia;

import java.io.Serializable;
import java.util.LinkedList;
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
 * This is a standard implementation of the Fuzzy ART model, as introduced by
 * Gail Carpenter et al.
 */
public class FuzzyArt implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	private final double rho_init;
	// choice parameter: a small positive value
	private static final double ALPHA = 0.0001;
	private static final double DELTA = 0.0001; // a small positive value
	private final double beta;
	private List<FuzzyVector> w = new LinkedList<FuzzyVector>();
	private FuzzyVector normalizedInput = null;
	private FuzzyVector unNormalizedInput = null;
	private double rho = 0;
	private List<Double> T = new LinkedList<Double>();
	private List<Boolean> eligible = new LinkedList<Boolean>();
	private List<FuzzyVector> centroids = new LinkedList<FuzzyVector>();
	private List<Integer> category_size = new LinkedList<Integer>(); // number of representants per category

	/**
	 * @param rho_init
	 *            the baseline rho value
	 * @param beta
	 *            used during learning
	 */
	public FuzzyArt(double rho_init, double beta) {
		this.rho = this.rho_init = rho_init;
		this.beta = beta;
	}

	/**
	 * Take a new (unnormalized) input of the network and compute the normalized
	 * form of input in normalizedInput.
	 * @param scaledInput the scaled input to be learned or tested
	 */
	void newInput(double[] scaledInput) {
		Util.checkScaled(scaledInput, "In newInput: the scaledInput vector is not between 0 and 1.");
		unNormalizedInput = new FuzzyVector(scaledInput);
		normalizedInput = unNormalizedInput.normalize();
		for (int i = 0; i < w.size(); i++) {
			eligible.set(i, true);
		}
	}

	/**
	 * Computes activation function T called by findCategory.
	 */
	private void computeT() {
		for (int j = 0; j < w.size(); j++) {
			double valueT = (normalizedInput.and(w.get(j))).norm() / (ALPHA + w.get(j).norm());
			T.set(j, valueT);
		}
	}

	/**
	 * Find and return category J for which T[J] is maximum and threshold test
	 * is passed. If no such category exists, this method returns -1
	 * @return the index of winning category; -1 if no winner is found
	 */
	public int findCategory() {
		computeT();
		while (true) {
			int J = -1;
			double TMax = -1;
			for (int j = 0; j < w.size(); j++) {
				if (eligible.get(j) && T.get(j) > TMax) {
					J = j;
					TMax = T.get(j);
				}
			}

			if (J == -1) {
				return J; // no category could be matched
			}

			FuzzyVector x_a = normalizedInput.and(w.get(J));
			if (x_a.norm() >= rho * normalizedInput.norm()) {
				return J;
			} else {
				eligible.set(J, false); // this category is inhibited fro the
										// current pattern
			}
		}
	}

	/**
	 * Add a new category to the network
	 */
	public void createNewCategory() {
		// copy the old values of w into the larger array auxW
		w.add(normalizedInput.clone());
		T.add(-1.0);// extend T by one element
		eligible.add(true);// extend eligible by one element

		/**
		 * Initialize centroid of new category. Copy the old values of centroids
		 * into the larger array auxW.
		 */
		// a new centroid value is added
		centroids.add(unNormalizedInput.clone());

		/**
		 * Initialize category_size (number of representants). The new category
		 * will have size 1 when created.
		 */
		category_size.add(1);
	}

	/**
	 * Update w[J], based on the old value and the current input.
	 * @param J the index of input category for which learning occurs
	 */
	public void learn(int J) {
		FuzzyVector term1 = (normalizedInput.and(w.get(J))).times(beta);
		FuzzyVector term2 = w.get(J).times(1 - beta);
		w.set(J, term1.sum(term2));

		category_size.set(J, category_size.get(J) + 1); // increment number of
														// representants

		// update centroid of node J using Kohonen's learning rule
		// and an the idea of Lim and Harrison
		FuzzyVector term = (unNormalizedInput.diff(centroids.get(J))).times(1. / category_size.get(J));
		centroids.set(J, term.sum(centroids.get(J)));
	}

	/**
	 * Called in match tracking phase.
	 * @param J the index of winning category
	 */
	public void increaseRho(int J) {
		rho = (normalizedInput.and(w.get(J))).norm() / normalizedInput.norm() + DELTA;
	}

	/**
	 * Used to restore value of rho at the baseline value. Called when match
	 * tracking takes place.
	 */
	public void restoreRho() {
		rho = rho_init;
	}

	/**
	 * @return the current vigilance value
	 */
	public double getRho() {
		return rho;
	}

	/**
	 * Returns the list of how many training patterns were associated with each input category
	 * @return The list of how many training patterns were associated with each input category
	 */
	public List<Integer> giveNumberInCategories() {
		/**
		 * Return number of vectors in each FuzzyArt category. OBSERVATION: This
		 * function is 100% accurate only when FuzzyArt is used as a clustering
		 * procedure (not in FuzzyArtMap)
		 */
		return category_size;
	}

	/**
	 * Returns the centroid corresponding to category j
	 * @param j the index of the category whose centroid is to be returned
	 * @return the centroid
	 */
	public double[] giveCentroid(int j) {
		/**
		 * Return the centroid of category j in FuzzyArt. OBSERVATION: This
		 * function is 100% accurate only when FuzzyArt is used as a clustering
		 * procedure (not in FuzzyArtMap)
		 */
		return centroids.get(j).getValue();
	}

	/**
	 * Return number of categories
	 * @return number of categories
	 */
	public int numCategories() {
		return w.size();
	}

	/**
	 * clones the current object
	 */
	public FuzzyArt clone() {
		FuzzyArt result = new FuzzyArt(this.rho_init, this.beta);
		result.w = Util.copyListOfFuzzyVector(this.w);
		result.normalizedInput = this.normalizedInput == null ? null : this.normalizedInput.clone();
		result.unNormalizedInput = this.unNormalizedInput == null ? null : this.unNormalizedInput.clone();
		result.rho = this.rho;
		result.T = Util.copyList(this.T);
		result.eligible = Util.copyList(this.eligible);
		result.centroids = Util.copyListOfFuzzyVector(this.centroids);
		result.category_size = Util.copyList(this.category_size); // number of representants per category
		
		return result;
	}

	/**
	 * sets the current vigilance threshold to 0
	 */
	public void setRhoToZero() {
		rho = 0;
	}

	/**
	 * TODO: create clusters for training patterns contained in a list
	*/
}