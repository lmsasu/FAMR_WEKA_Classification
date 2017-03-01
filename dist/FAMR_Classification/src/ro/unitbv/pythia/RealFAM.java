//package ro.unitbv.pythia;
//
//import java.io.*;
//import java.util.UUID;
//
///**
// * Title:        Modified Fuzzy Artmap
// * Description:
// * Copyright:    Copyright (c) 2002
// * Company:
// * @author
// * @version 1.0
// */
//
///**
// * This is a modified Fuzzy Artmap model implementation for function
// * approximation. The essential modification, compared to Carpenter's et al.
// * model is in class Mapfield, where the values w_ab have a completely different
// * meaning and formula.
// */
//public class RealFAM implements Serializable {
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = -4519822438420924311L;
//
//	private String serializationFile;
//	private FAMRComponents famrDeserialized = null;
//
//	public RealFAM() {
//		setSerializationFilePath();
//	}
//
//	/**
//	 * Train a Fuzzy Artmap network with the given set of input-output examples
//	 */
//	public void train(int inputDim, int outputDim, double imin, double imax, double omin, double omax,
//			String inputFileName, double rho_init_a, // needed in ART_a
//			double beta_a, // needed in ART_a
//			double rho_init_b, // needed in ART_b
//			double beta_b, // needed in ART_b
//			double rho_ab, // needed in MapField
//			int EPOCHS, // EPOCHS
//			boolean history // continue a previous clustering
//	) {
//		DataInput Data = null;
//		FuzzyArt art_a = null;
//		FuzzyArt art_b = null;
//		MapField mapField = null;
//		int global_n_pairs = 0;
//
//		if (history) {
//			// deserialize
//			try {
//				FileInputStream in = new FileInputStream(getSerializationFile());
//				ObjectInputStream objectInStr = new ObjectInputStream(in);
//				Data = (DataInput) objectInStr.readObject();
//				art_a = (FuzzyArt) objectInStr.readObject();
//				art_b = (FuzzyArt) objectInStr.readObject();
//				mapField = (MapField) objectInStr.readObject();
//				global_n_pairs = objectInStr.readInt();
//				objectInStr.close();
//			} catch (ClassNotFoundException ex) {
//				ex.printStackTrace();
//			} catch (FileNotFoundException ex) {
//				ex.printStackTrace();
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//		} else {
//			// start from scratch
//			Data = new DataInput(inputDim, outputDim, imin, imax, omin, omax);
//			art_a = new FuzzyArt(rho_init_a, beta_a);
//			art_b = new FuzzyArt(rho_init_b, beta_b);
//			mapField = new MapField(rho_ab);
//		}
//
//		int n_pairs = 0;
//		int n_rejected_pairs = 0;
//		System.out.println("Start training the function approximator");
//		for (int i = 0; i < EPOCHS; i++) {
//			Data.open(inputFileName);
//			while (true) {
//				double input[] = Data.giveInput();
//				if (input == null) {
//					break;
//				}
//				double output[] = Data.giveRealOutput();
//				// double q_t = Data.give_q();
//				double q_t = 1;
//				if (!trainPair(art_a, art_b, mapField, input, output, q_t)) {
//					n_rejected_pairs++;
//				}
//				if (i == 0) {
//					n_pairs++;
//					global_n_pairs++;
//				}
//			}
//			Data.close();
//		}
//		System.out.println("End of " + EPOCHS + " epochs of trainig. " + "We have obtained " + art_a.numCategories()
//				+ " input categories and " + art_b.numCategories() + " output categories\n" + "using " + n_pairs
//				+ " training pairs\n" + "Number of rejected pairs is " + n_rejected_pairs + "\n"
//				+ "Historical number of processed pairs: " + global_n_pairs + "\n");
//		serialize(Data, art_a, art_b, mapField, global_n_pairs);
//
//		// writeArtACentroids( art_a );
//
//		// try
//		// {
//		// BufferedWriter out = new BufferedWriter(new FileWriter(
//		// "probabilitati.txt" ) );
//		// out.write( mapField.toString() );
//		// out.close();
//		// }
//		// catch( Exception e )
//		// {
//		// e.printStackTrace();
//		// }
//	}
//
//	public void train(int inputDim, int outputDim, double imin, double imax, double omin, double omax,
//			Instances instances, double rho_init_a, // needed in ART_a
//			double beta_a, // needed in ART_a
//			double rho_init_b, // needed in ART_b
//			double beta_b, // needed in ART_b
//			double rho_ab, // needed in MapField
//			int EPOCHS, // EPOCHS
//			boolean history // continue a previous clustering
//	) {
//		DataInput Data = null;
//		FuzzyArt art_a = null;
//		FuzzyArt art_b = null;
//		MapField mapField = null;
//		int global_n_pairs = 0;
//
//		if (history) {
//			// deserialize
//			try {
//				FileInputStream in = new FileInputStream(getSerializationFile());
//				ObjectInputStream objectInStr = new ObjectInputStream(in);
//				Data = (DataInput) objectInStr.readObject();
//				art_a = (FuzzyArt) objectInStr.readObject();
//				art_b = (FuzzyArt) objectInStr.readObject();
//				mapField = (MapField) objectInStr.readObject();
//				global_n_pairs = objectInStr.readInt();
//				objectInStr.close();
//			} catch (ClassNotFoundException ex) {
//				ex.printStackTrace();
//			} catch (FileNotFoundException ex) {
//				ex.printStackTrace();
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//		} else {
//			// start from scratch
//			Data = new DataInput(inputDim, outputDim, imin, imax, omin, omax);
//			art_a = new FuzzyArt(rho_init_a, beta_a);
//			art_b = new FuzzyArt(rho_init_b, beta_b);
//			mapField = new MapField(rho_ab);
//		}
//
//		int n_pairs = 0;
//		int n_rejected_pairs = 0;
//		System.out.println("Start training the function approximator");
//		for (int i = 0; i < EPOCHS; i++) {
//			for(Instance instance : instances)
//			{
//				double input[] = FAMRUtil.getInputVectorFromInstance(instance, imin, imax);
//				double output[] = FAMRUtil.getOutputValue(instance, omin, omax);
//				double q_t = instance.weight();
//				System.err.println("q_t= " + q_t);
//				if (!trainPair(art_a, art_b, mapField, input, output, q_t)) {
//					n_rejected_pairs++;
//				}
//				if (i == 0) {
//					n_pairs++;
//					global_n_pairs++;
//				}
//			}
//		}
//		System.out.println("End of " + EPOCHS + " epochs of trainig. " + "We have obtained " + art_a.numCategories()
//				+ " input categories and " + art_b.numCategories() + " output categories\n" + "using " + n_pairs
//				+ " training pairs\n" + "Number of rejected pairs is " + n_rejected_pairs + "\n"
//				+ "Historical number of processed pairs: " + global_n_pairs + "\n");
//		serialize(Data, art_a, art_b, mapField, global_n_pairs);
//
//		// writeArtACentroids( art_a );
//
//		// try
//		// {
//		// BufferedWriter out = new BufferedWriter(new FileWriter(
//		// "probabilitati.txt" ) );
//		// out.write( mapField.toString() );
//		// out.close();
//		// }
//		// catch( Exception e )
//		// {
//		// e.printStackTrace();
//		// }
//	}
//
//	private class FAMRComponents implements Serializable {
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = -793008995747633316L;
//
//		public DataInput Data = null;
//		public FuzzyArt art_a = null;
//		public FuzzyArt art_b = null;
//		public MapField mapField = null;
//	}
//
//	/**
//	 * Compute the root-mean-square and the mean absolute normalized errors of
//	 * the function approximation on a set of input-output pairs
//	 */
//	public void computeApproxError(String testFileName) {
//		deserialize();
//
//		famrDeserialized.art_a.setRhoToZero();
//		System.out.println("Start testing");
//		famrDeserialized.Data.open(testFileName);
//		double RMSError = 0;
//		double MAXAError = 0;
//		int considered = 0;
//		int not_considered = 0;
//		int outDim = famrDeserialized.Data.giveOutputDimension();
//		while (true) {
//			double[] input = famrDeserialized.Data.giveInput();// scaled input
//			if (input == null) {
//				break;
//			}
//			double[] output = famrDeserialized.Data.giveRealOutput();
//			double[] approx = approximate(input);
//			// approx is the approximation of f(x)
//			if (approx == null) {
//				not_considered++;
//			} else {
//				considered++;
//				for (int i = 0; i < outDim; i++) {
//					RMSError += Math.pow(output[i] - approx[i], 2);// as defined
//																	// in
//																	// PROBART
//				}
//				double sum = 0;
//				for (int i = 0; i < outDim; i++) {
//					sum += Math.pow(output[i] - approx[i], 2);
//				}
//				sum = Math.sqrt(sum);
//				if (MAXAError < sum) {
//					MAXAError = sum;
//				}
//			}
//		}
//		famrDeserialized.Data.close();
//		if (considered != 0) {
//			RMSError = Math.sqrt(RMSError * 1.0 / considered);
//			// MAError = (MAError * Data.outputDiff()) / (outDim * considered);
//		}
//		System.out.println("In " + considered + " test" + " pairs the root-mean-square normalized error is " + RMSError
//				+ "\nand the mean absolute normalized error is " + MAXAError + "\n" + not_considered
//				+ " pairs were omitted");
//		System.out.println("End of testing\n");
//
//	}
//
//	/**
//	 * Use the trained Fuzzy Artmap network for approximating a real function
//	 * f(x), where x is the new input vector.
//	 */
//	public void functionApproximation(String inputFileName, String outputFileName) {
//		deserialize();
//
//		famrDeserialized.art_a.setRhoToZero();
//		System.out.println("Begin function approximation");
//		famrDeserialized.Data.open(inputFileName);
//		BufferedWriter out = null;
//		try {
//			out = new BufferedWriter(new FileWriter(outputFileName));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		while (true) {
//			double[] input = famrDeserialized.Data.giveInput();
//			if (input == null) {
//				break;
//			}
//			StringBuffer sb = new StringBuffer();
//			// sb.append( "x = " +
//			// FuzzyVector.toString(Data.unscaleInput(input)));
//			sb.append(Util.toString(famrDeserialized.Data.unscaleInput(input)));
//			// sb.append( "\n" );
//			// sb.append( "f(x) = " );
//			double[] approx = approximate(input);
//			// approx is the approximation of f(x)
//			if (approx == null) {
//				sb.append("unable to evaluate\n\n");
//			} else {
//				sb.append(Util.toString(famrDeserialized.Data.unscaleOutput(approx)));
//				// sb.append( "\n\n" );
//				sb.append("\n");
//				try {
//					String s = sb.toString().replace(' ', ',');
//					// out.write( sb.toString() );
//					out.write(s);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		famrDeserialized.Data.close();
//		try {
//			out.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("End of function approximation\n");
//	}
//
//	/**
//	 * Return true if training pair (input[], classNo), with relevance factor
//	 * q_t was learned (correctly or not).
//	 */
//	public boolean trainPair(FuzzyArt art_a, FuzzyArt art_b, MapField mapField, double[] input, double[] output,
//			double q_t) {
//		MapField old_mapField = (MapField) mapField.clone(); // save MapField
//		FuzzyArt old_art_a = (FuzzyArt) art_a.clone(); // save art_a
//		FuzzyArt old_art_b = (FuzzyArt) art_b.clone(); // save art_b
//		art_a.newInput(input);
//		art_b.newInput(output);
//		int K = art_b.findCategory();
//		if (K == -1)
//		// no suitable category found in art_b; create a new category
//		{
//			art_b.createNewCategory();
//			mapField.addWeight_b();
//			K = art_b.numCategories() - 1;
//		}
//		FuzzyVector y_b = new FuzzyVector(art_b.numCategories());
//		y_b.initValue(0);
//		y_b.setValueAtPos(K, 1);
//		art_a.restoreRho();
//		while (true) {
//			int J = art_a.findCategory();
//			if (J == -1)
//			// no suitable category found in art_a; create a new category
//			{
//				art_a.createNewCategory();
//				mapField.addWeight_a();
//				J = art_a.numCategories() - 1;
//			}
//			if (mapField.accept(y_b, J)) {
//				// learn current pair
//				art_a.learn(J);
//				art_b.learn(K);
//				mapField.learn(J, K, q_t);
//				return true;
//			} else {
//				art_a.increaseRho(J);
//				if (art_a.getRho() > 1) {
//					// reject current pair
//					art_a = old_art_a; // restore art_a
//					art_b = old_art_b; // restore art_b
//					mapField = old_mapField; // restore mapField
//					return false;
//				} else {
//					continue; // reiterate current input vector
//				}
//			}
//		}
//	}
//
//	/**
//	 * Return the average of the centroids in art_b assigned to input[]. The
//	 * idea can be found in Marriott & Lim's PROBART
//	 */
//	private double[] approximate(double input[]) {
//		famrDeserialized.art_a.newInput(input);
//		int J = famrDeserialized.art_a.findCategory();
//		int nb = famrDeserialized.art_b.numCategories();
//		double result[] = new double[famrDeserialized.Data.giveOutputDimension()];
//		FuzzyVector aux = new FuzzyVector(nb);
//		if (J == -1) {
//			return null;
//		} else {
//			for (int i = 0; i < famrDeserialized.Data.giveOutputDimension(); i++) {
//				for (int k = 0; k < nb; k++) {
//					aux.setValueAtPos(k, famrDeserialized.art_b.giveCentroid(k)[i]);
//				}
//				result[i] = famrDeserialized.mapField.getw_ab(J).prod(aux);
//			}
//			return result;
//		}
//	}
//
//	private double[] approximateUnscaledReturnScaled(double input[]) {
//		double[] scaledInput = famrDeserialized.Data.scaleInput(input);
//		return approximate(scaledInput);
//	}
//
//	public double[] approximateUnscaledReturnUnscaled(double input[]) {
//		double[] scaledOutput = approximateUnscaledReturnScaled(input);
//		return famrDeserialized.Data.unscaleOutput(scaledOutput);
//	}
//
//	public void serialize(DataInput Data, FuzzyArt art_a, FuzzyArt art_b, MapField mapField, int global_n_pairs) {
//		try {
//			FileOutputStream fileStream = new FileOutputStream(getSerializationFile());
//			ObjectOutputStream objInStr = new ObjectOutputStream(fileStream);
//			objInStr.writeObject(Data);
//			objInStr.writeObject(art_a);
//			objInStr.writeObject(art_b);
//			objInStr.writeObject(mapField);
//			objInStr.writeInt(global_n_pairs);
//			objInStr.close();
//			fileStream.close();
//		} catch (FileNotFoundException ex) {
//			ex.printStackTrace();
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
//	}
//
//	public void writeArtACentroids(FuzzyArt art_a) {
//		try {
//			BufferedWriter out = new BufferedWriter(new FileWriter("centroids.txt"));
//			for (int i = 0; i < art_a.numCategories(); i++) {
//				out.write(new FuzzyVector(art_a.giveCentroid(i)) + "\n");
//			}
//			out.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void setSerializationFilePath() {
//		String directoryFiles = System.getProperty("user.home") + "/" + "famr";
//		File dir = new File(directoryFiles);
//		dir.mkdir();
//		serializationFile = directoryFiles + "/" + UUID.randomUUID().toString() + ".serialized_RFAM";
//	}
//
//	private String getSerializationFile() {
//		return serializationFile;
//	}
//
//	public void deleteSerializedFile() {
//		File f = new File(serializationFile);
//		f.deleteOnExit();
//	}
//
//	public double[] estimateRegression(double[] input) {
//		return new double[] { 0 };
//	}
//
//	public void deserialize() {
//		famrDeserialized = new FAMRComponents();
//
//		try {
//			FileInputStream in = new FileInputStream(getSerializationFile());
//			ObjectInputStream objectInStr = new ObjectInputStream(in);
//			famrDeserialized.Data = (DataInput) objectInStr.readObject();
//			famrDeserialized.art_a = (FuzzyArt) objectInStr.readObject();
//			famrDeserialized.art_b = (FuzzyArt) objectInStr.readObject();
//			famrDeserialized.mapField = (MapField) objectInStr.readObject();
//			objectInStr.close();
//			in.close();
//		} catch (ClassNotFoundException ex) {
//			ex.printStackTrace();
//		} catch (FileNotFoundException ex) {
//			ex.printStackTrace();
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
//	}
//
//	public int getInputCategoriesNo() {
//		if (famrDeserialized != null) {
//			return famrDeserialized.art_a.numCategories();
//		} else {
//			return -1;
//		}
//	}
//
//	public int getOutputCategoriesNo() {
//		if (famrDeserialized != null) {
//			return famrDeserialized.art_b.numCategories();
//		} else {
//			return -1;
//		}
//	}
//}