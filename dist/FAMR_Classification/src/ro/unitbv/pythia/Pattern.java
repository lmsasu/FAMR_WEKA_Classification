package ro.unitbv.pythia;

import java.io.Serializable;
import java.util.Arrays;

public class Pattern implements Cloneable, Serializable {

	private static final long serialVersionUID = 2L;
	private double[] input;// original value
	private double[] scaledInput;// scaled input: each value is in interval [0,
									// 1]
	private int classIndex = -1;// for classification
	private double originalContinuousOutput = Double.NaN;// for regression
	private double scaledContinuousOutput = Double.NaN;// for regression
	private boolean isClassificationInstance = true;
	private boolean inputIsScaled = false;
	private boolean outputIsScaled = false;
	private double weight = 1.0;

	/**
	 * @return true if the current pattern has input class associated, false
	 *         otherwise
	 */
	public boolean hasClassIndex() {
		return classIndex >= 0;
	}

	/**
	 * Tells whether the instance has a non NaN value for output value
	 * 
	 * @return true if continuousOutput is not NaN, false otherwise
	 */
	private boolean hasContinuousOutput() {
		return !Double.isNaN(originalContinuousOutput);
	}

	/**
	 * Set the relevance for the current pattern
	 * 
	 * @param weight
	 *            the relevance value, greater than 0
	 */
	public void setWeight(double weight) {
		if (weight <= 0) {
			throw new IllegalArgumentException("The given relevance should be strictly greater than 0");
		}
		this.weight = weight;
	}

	/**
	 * @return The relevance associated to the current pattern
	 */
	public double getWeight() {
		return this.weight;
	}

	/**
	 * @return the inputIsScaled field value
	 */
	public boolean isInputScaled() {
		return inputIsScaled;
	}

	/**
	 * @param inputIsScaled
	 *            the normalizedInput to set
	 */
	public void setInputIsScaled(boolean inputIsScaled) {
		this.inputIsScaled = inputIsScaled;
	}

	/**
	 * @return the outputIsNormalized field value
	 */
	public boolean isScaledOutput() {
		return outputIsScaled;
	}

	/**
	 * @param outputIsNormalized
	 *            the value to set for field outputIsNormalized
	 */
	private void setScaledOutput(boolean outputIsNormalized) {
		this.outputIsScaled = outputIsNormalized;
	}

	/**
	 * @param input
	 *            the input to set
	 */
	public void setInput(double[] input) {
		this.input = input;
	}

	/**
	 * @return the classIndex
	 */
	public int getClassIndex() {
		return classIndex;
	}

	/**
	 * @param classIndex
	 *            the classIndex to set
	 */
	public void setClassIndex(int classIndex) {
		this.classIndex = classIndex;
	}

	/**
	 * @return the continuousOutput
	 */
	public double getContinuousOutput() {
		return originalContinuousOutput;
	}

	/**
	 * @param continuousOutput
	 *            the continuousOutput to set
	 */
	public void setContinuousOutput(double continuousOutput) {
		this.originalContinuousOutput = continuousOutput;
	}

	/**
	 * @return the isClassificationInstance
	 */
	public boolean isClassificationInstance() {
		return isClassificationInstance;
	}

	/**
	 * @param isClassificationInstance
	 *            the isClassificationInstance to set
	 */
	public void setClassificationInstance(boolean isClassificationInstance) {
		this.isClassificationInstance = isClassificationInstance;
	}

	/**
	 * Scales the contents to [0, 1]
	 * 
	 * @param inputMin
	 *            the minimum input value: input[i] >= inputMin, for all i
	 * @param inputMax
	 *            the maximum input value: input[i] <= inputMin, for all i
	 */
	public void scaleInput(double inputMin, double inputMax) {
		if (isInputScaled()) {
			return;
		}
		if (inputMax <= inputMin) {
			throw new RuntimeException("In scaleInput: inputMin should be less than inputMax, they are (" + inputMin
					+ ", " + inputMax + ")");
		}
		if (Util.min(this.input) < inputMin) {
			throw new RuntimeException("In scaleInput: inputMin is larger than min of passed vector");
		}
		if (Util.max(this.input) > inputMax) {
			throw new RuntimeException("In scaleInput: inputMax is larger than max of passed vector");
		}
		double range = inputMax - inputMin;
		scaledInput = new double[input.length];
		for (int i = 0; i < input.length; i++) {
			scaledInput[i] = (input[i] - inputMin) / range;
		}
		setInputIsScaled(true);
	}

	/**
	 * Scales the output
	 * 
	 * @param outputMin
	 *            the min output
	 * @param outputMax
	 *            the max output
	 */
	public void scaleContinuousOutput(double outputMin, double outputMax) {
		if (isScaledOutput()) {
			return;
		}
		if (isClassificationInstance()) {
			throw new RuntimeException("The current pattern is set for classification");
		}
		if (hasContinuousOutput() == false) {
			throw new RuntimeException("No output value to normalize");
		}
		if (outputMax <= outputMin) {
			throw new RuntimeException("In scaleContinuousOutput: outputMin should be less than outputMin, they are ("
					+ outputMin + ", " + outputMax + ")");
		}
		if (originalContinuousOutput < outputMin) {
			throw new RuntimeException(
					"In scaleContinuousOutput: outputMin is larger than current output " + originalContinuousOutput);
		}
		if (originalContinuousOutput > outputMax) {
			throw new RuntimeException(
					"In scaleContinuousOutput: outputMax is less than current output " + originalContinuousOutput);
		}
		originalContinuousOutput = (originalContinuousOutput - outputMin) / (outputMax - outputMin);
		setScaledOutput(true);
	}

	/**
	 * @return the input size
	 */
	public int getInputDimension() {
		if (input == null) {
			return 0;
		}
		return input.length;
	}

	/**
	 * @return the scaleInput (possible null, if no scaling was done)
	 */
	public double[] getScaledInput() {
		return scaledInput;
	}

	/**
	 * 
	 * @return the scaled output
	 */
	public double getScaledContinuousOutput() {
		return scaledContinuousOutput;
	}

	/**
	 * Cloning
	 */
	@Override
	protected Pattern clone() {
		Pattern pattern = new Pattern();
		pattern.classIndex = this.classIndex;
		pattern.input = this.input == null ? null : Arrays.copyOf(this.input, this.input.length);
		pattern.inputIsScaled = this.inputIsScaled;
		pattern.isClassificationInstance = this.isClassificationInstance;
		pattern.originalContinuousOutput = this.originalContinuousOutput;
		pattern.outputIsScaled = this.outputIsScaled;
		pattern.scaledContinuousOutput = this.scaledContinuousOutput;
		pattern.scaledInput = this.scaledInput == null ? null
				: Arrays.copyOf(this.scaledInput, this.scaledInput.length);
		pattern.weight = this.weight;

		return pattern;
	}

	/**
	 * produces string representation of this pattern
	 */
	public String toString_unscaled() {
		StringBuilder stringBuilder = new StringBuilder(Util.toString(input, ","));
		if (isClassificationInstance) {
			stringBuilder.append(getClassIndex());
		} else {
			stringBuilder.append(getContinuousOutput());
		}
		stringBuilder.append("; q_t= " + getWeight());
		return stringBuilder.toString();
	}
	
	/**
	 * @return a string containing the scaled values
	 */
	public String toString_scaled() {
		if (!isInputScaled() || (isClassificationInstance == false && !isScaledOutput()))
		{
			throw new RuntimeException("The current pattern is not scaled");
		}
		StringBuilder stringBuilder = new StringBuilder(Util.toString(scaledInput, ","));
		if (isClassificationInstance) {
			stringBuilder.append(getClassIndex());
		} else {
			stringBuilder.append(getScaledContinuousOutput());
		}
		stringBuilder.append("; q_t= " + getWeight());
		return stringBuilder.toString();
	}
}
