package ccd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import changechecker.ChangeChecker;
import changechecker.TimeCountChecker;
import clustree.ClusTree;
import environment.AccuracyEvaluator;
import environment.Evaluator;
import environment.Stopwatch;
import environment.Parameters.StreamSummarisation;
import environment.Parameters.SubspaceBuildup;
import fullsystem.Callback;
import fullsystem.Contrast;
import fullsystem.CorrelatedSubspacesChangeDetector;
import fullsystem.FullSpaceChangeDetector;
import fullsystem.StreamHiCS;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.trees.HoeffdingTree;
import streamdatastructures.CentroidsAdapter;
import streamdatastructures.CorrelationSummary;
import streamdatastructures.MicroclusteringAdapter;
import streamdatastructures.SummarisationAdapter;
import streams.SubspaceRandomRBFGeneratorDrift;
import subspacebuilder.AprioriBuilder;
import subspacebuilder.CliqueBuilder;
import subspacebuilder.ComponentBuilder;
import subspacebuilder.HierarchicalBuilderCutoff;
import subspacebuilder.SubspaceBuilder;
import weka.core.Instance;
import weka.core.Utils;

public class SubspaceRBFDriftTests {
	private SubspaceRandomRBFGeneratorDrift stream;
	private StreamHiCS streamHiCS;
	private int numInstances;
	private final int horizon = 5000;
	private final int m = 50;
	private final double alpha = 0.1;
	private double epsilon;
	private double aprioriThreshold;
	private double hierarchicalThreshold;
	private double connectedComponentsThreshold;
	private double cliqueThreshold;
	private int cutoff;
	private double pruningDifference;
	private int numberOfDimensions;
	private Callback callback = new Callback() {
		@Override
		public void onAlarm() {
			// System.out.println("StreamHiCS: onAlarm()");
		}
	};
	private static Stopwatch stopwatch;
	private Contrast contrastEvaluator;
	private static final int numberTestRuns = 1;
	private List<String> results;
	private String summarisationDescription = null;
	private String builderDescription = null;
	private CorrelatedSubspacesChangeDetector cscd;
	private FullSpaceChangeDetector refDetector;
	private Random random;

	@BeforeClass
	public static void setUpBeforeClass() {
		stopwatch = new Stopwatch();
	}

	@Before
	public void setup() {
		random = new Random();
	}

	@Test
	public void test() {
		// Output
		results = new LinkedList<String>();
		SummarisationAdapter adapter;
		SubspaceBuilder subspaceBuilder;
		for (StreamSummarisation summarisation : StreamSummarisation.values()) {
			summarisationDescription = null;
			for (SubspaceBuildup buildup : SubspaceBuildup.values()) {
				builderDescription = null;
				if (summarisation == StreamSummarisation.ADAPTINGCENTROIDS && buildup == SubspaceBuildup.CONNECTED_COMPONENTS) {
					// int from = 1;
					// int to = 1;
					// int numberTests = from - to + 1;
					for (int test = 4; test <= 4; test++) {
						stopwatch.reset();
						double threshold = 1;

						ArrayList<Double> trueChanges = new ArrayList<Double>();
						int[] changePoints = null;
						int changeLength = 0;
						double speed = 0;
						int[] virtualDriftPoints = null;
						results.add("" + test);
						switch (test) {
						case 1:
							switch (summarisation) {
							case CLUSTREE_DEPTHFIRST:
								aprioriThreshold = 0.2;
								hierarchicalThreshold = 0.5;
								break;
							case ADAPTINGCENTROIDS:
								aprioriThreshold = 0.25;
								hierarchicalThreshold = 0.65;
								break;
							default:
								break;
							}
							switch (buildup) {
							case APRIORI:
								threshold = aprioriThreshold;
								connectedComponentsThreshold = 0.5;
								break;
							case HIERARCHICAL:
								threshold = hierarchicalThreshold;
								connectedComponentsThreshold = 0.5;
								break;
							case CONNECTED_COMPONENTS:
								threshold = connectedComponentsThreshold;
								connectedComponentsThreshold = 0.5;
								break;
							default:
								break;
							}
							epsilon = 0.15;
							numberOfDimensions = 6;
							numInstances = 25000;
							stream = new SubspaceRandomRBFGeneratorDrift();
							stream.numAttsOption.setValue(numberOfDimensions);
							stream.numClassesOption.setValue(2);
							stream.avgSubspaceSizeOption.setValue(numberOfDimensions / 2);
							stream.scaleIrrelevantDimensionsOption.setValue(4);
							stream.numCentroidsOption.setValue(10);
							stream.numSubspaceCentroidsOption.setValue(10);
							stream.sameSubspaceOption.setValue(true);
							stream.randomSubspaceSizeOption.setValue(false);
							stream.numDriftCentroidsOption.setValue(10);							
							// stream.modelRandomSeedOption.setValue(random.nextInt());
							// stream.instanceRandomSeedOption.setValue(random.nextInt());
							// stream.prepareForUse();
							changePoints = new int[4];
							changePoints[0] = 5000;
							changePoints[1] = 10000;
							changePoints[2] = 15000;
							changePoints[3] = 20000;
							changeLength = 1000;
							speed = 0.5;
							trueChanges.add(5000.0);
							trueChanges.add(10000.0);
							trueChanges.add(15000.0);
							trueChanges.add(20000.0);
							break;
						case 2:
							switch (summarisation) {
							case CLUSTREE_DEPTHFIRST:
								aprioriThreshold = 0.3;
								hierarchicalThreshold = 0.25;
								connectedComponentsThreshold = 0.5;
								break;
							case ADAPTINGCENTROIDS:
								aprioriThreshold = 0.25;
								hierarchicalThreshold = 0.3;
								connectedComponentsThreshold = 0.3;
								break;
							default:
								break;
							}
							switch (buildup) {
							case APRIORI:
								threshold = aprioriThreshold;
								break;
							case HIERARCHICAL:
								threshold = hierarchicalThreshold;
								break;
							case CONNECTED_COMPONENTS:
								threshold = connectedComponentsThreshold;
								break;
							default:
								break;
							}
							numberOfDimensions = 10;
							epsilon = 0.15;
							numInstances = 25000;
							stream = new SubspaceRandomRBFGeneratorDrift();
							stream.numAttsOption.setValue(numberOfDimensions);
							stream.numClassesOption.setValue(2);
							stream.avgSubspaceSizeOption.setValue(numberOfDimensions / 2);
							stream.numCentroidsOption.setValue(5);
							stream.scaleIrrelevantDimensionsOption.setValue(4);
							stream.numSubspaceCentroidsOption.setValue(5);
							stream.sameSubspaceOption.setValue(true);
							stream.randomSubspaceSizeOption.setValue(false);
							stream.numDriftCentroidsOption.setValue(5);
							// stream.modelRandomSeedOption.setValue(random.nextInt());
							// stream.instanceRandomSeedOption.setValue(random.nextInt());
							// stream.prepareForUse();
							changePoints = new int[4];
							changePoints[0] = 5000;
							changePoints[1] = 10000;
							changePoints[2] = 15000;
							changePoints[3] = 20000;
							changeLength = 500;
							speed = 0.5;
							trueChanges.add(5000.0);
							trueChanges.add(10000.0);
							trueChanges.add(15000.0);
							trueChanges.add(20000.0);
							break;
							
						case 3:
							switch (summarisation) {
							case CLUSTREE_DEPTHFIRST:
								aprioriThreshold = 0.35;
								hierarchicalThreshold = 0.5;
								connectedComponentsThreshold = 0.5;
								cliqueThreshold = 0.55;
								break;
							case ADAPTINGCENTROIDS:
								aprioriThreshold = 0.3;
								hierarchicalThreshold = 0.65;
								connectedComponentsThreshold = 0.3;
								cliqueThreshold = 0.4;
								break;
							default:
								break;
							}
							switch (buildup) {
							case APRIORI:
								threshold = aprioriThreshold;
								break;
							case HIERARCHICAL:
								threshold = hierarchicalThreshold;
								break;
							case CONNECTED_COMPONENTS:
								threshold = connectedComponentsThreshold;
								break;
							case CLIQUE:
								threshold = cliqueThreshold;
								break;
							}
							numberOfDimensions = 50;
							numInstances = 90000;
							epsilon = 0.1;
							stream = new SubspaceRandomRBFGeneratorDrift();
							stream.numAttsOption.setValue(numberOfDimensions);
							stream.numClassesOption.setValue(2);
							stream.avgSubspaceSizeOption.setValue(numberOfDimensions / 10);
							stream.scaleIrrelevantDimensionsOption.setValue(5);
							stream.numCentroidsOption.setValue(2);
							stream.numSubspaceCentroidsOption.setValue(2);
							stream.sameSubspaceOption.setValue(true);
							stream.randomSubspaceSizeOption.setValue(false);
							stream.numDriftCentroidsOption.setValue(2);
							changePoints = new int[4];
							changePoints[0] = 10000;
							changePoints[1] = 30000;
							changePoints[2] = 50000;
							changePoints[3] = 70000;

							changeLength = 1000;
							speed = 1;
							trueChanges.add(10000.0);
							trueChanges.add(30000.0);
							trueChanges.add(50000.0);
							trueChanges.add(70000.0);

							virtualDriftPoints = null;
							break;
						case 4:
							switch (summarisation) {
							case CLUSTREE_DEPTHFIRST:
								aprioriThreshold = 0.35;
								hierarchicalThreshold = 0.5;
								connectedComponentsThreshold = 0.45;
								cliqueThreshold = 0.55;
								break;
							case ADAPTINGCENTROIDS:
								aprioriThreshold = 0.3;
								hierarchicalThreshold = 0.65;
								connectedComponentsThreshold = 0.3;
								cliqueThreshold = 0.5;
								break;
							default:
								break;
							}
							switch (buildup) {
							case APRIORI:
								threshold = aprioriThreshold;
								break;
							case HIERARCHICAL:
								threshold = hierarchicalThreshold;
								break;
							case CONNECTED_COMPONENTS:
								threshold = connectedComponentsThreshold;
								break;
							case CLIQUE:
								threshold = cliqueThreshold;
								break;
							}
							numberOfDimensions = 50;
							numInstances = 90000;
							epsilon = 0.1;
							stream = new SubspaceRandomRBFGeneratorDrift();
							stream.numAttsOption.setValue(numberOfDimensions);
							stream.numClassesOption.setValue(2);
							stream.avgSubspaceSizeOption.setValue(numberOfDimensions / 10);
							stream.scaleIrrelevantDimensionsOption.setValue(5);
							stream.numCentroidsOption.setValue(2);
							stream.numSubspaceCentroidsOption.setValue(2);
							stream.sameSubspaceOption.setValue(true);
							stream.randomSubspaceSizeOption.setValue(false);
							stream.numDriftCentroidsOption.setValue(2);
							// stream.modelRandomSeedOption.setValue(random.nextInt());
							// stream.instanceRandomSeedOption.setValue(random.nextInt());
							// stream.prepareForUse();
							changePoints = new int[4];
							/*
							 * changePoints[0] = 5000; changePoints[1] = 10000;
							 * changePoints[2] = 15000; changePoints[3] = 20000;
							 */
							changePoints[0] = 10000;
							changePoints[1] = 30000;
							changePoints[2] = 50000;
							changePoints[3] = 70000;

							changeLength = 1000;
							speed = 10;
							trueChanges.add(10000.0);
							trueChanges.add(30000.0);
							trueChanges.add(50000.0);
							trueChanges.add(70000.0);
							/*
							 * virtualDriftPoints = new int[3];
							 * virtualDriftPoints[0] = 7500;
							 * virtualDriftPoints[1] = 12500;
							 * virtualDriftPoints[2] = 17500;
							 */

							virtualDriftPoints = new int[4];
							virtualDriftPoints[0] = 20000;
							virtualDriftPoints[1] = 40000;
							virtualDriftPoints[2] = 60000;
							virtualDriftPoints[3] = 80000;

							break;
						}

						// Creating the StreamHiCS system
						adapter = createSummarisationAdapter(summarisation);
						contrastEvaluator = new Contrast(m, alpha, adapter);
						CorrelationSummary correlationSummary = null;
						subspaceBuilder = createSubspaceBuilder(buildup, correlationSummary);
						ChangeChecker changeChecker = new TimeCountChecker(1000);
						streamHiCS = new StreamHiCS(epsilon, threshold, pruningDifference, contrastEvaluator,
								subspaceBuilder, changeChecker, callback, correlationSummary, stopwatch);
						changeChecker.setCallback(streamHiCS);

						cscd = new CorrelatedSubspacesChangeDetector(numberOfDimensions, streamHiCS);
						cscd.initOption.setValue(5000);
						cscd.prepareForUse();
						streamHiCS.setCallback(cscd);

						refDetector = new FullSpaceChangeDetector();
						AbstractClassifier baseLearner = new HoeffdingTree();
						baseLearner.prepareForUse();
						refDetector.baseLearnerOption.setCurrentObject(baseLearner);
						refDetector.prepareForUse();

						double[] cscdSums = new double[8];
						double[] refSums = new double[8];

						stopwatch.reset();
						for (int i = 0; i < numberTestRuns; i++) {
							// Reset the stream and the change detectors
							int seed = random.nextInt();
							// System.out.println(seed);
							stream.modelRandomSeedOption.setValue(seed);
							seed = random.nextInt();
							// System.out.println(seed);
							stream.instanceRandomSeedOption.setValue(seed);
							// stream.restart();
							stream.prepareForUse();
							cscd.resetLearning();
							refDetector.resetLearning();

							System.out.println("Run: " + (i + 1));
							double[][] performanceMeasures = testRun(changePoints, changeLength, speed, trueChanges,
									virtualDriftPoints);
							for (int j = 0; j < 5; j++) {
								cscdSums[j] += performanceMeasures[0][j];
								refSums[j] += performanceMeasures[1][j];
							}
						}

						// Calculate results
						cscdSums[5] = stopwatch.getTime("Evaluation");
						cscdSums[6] = stopwatch.getTime("Adding");
						cscdSums[7] = stopwatch.getTime("Total_CSCD");
						refSums[7] = stopwatch.getTime("Total_REF");

						for (int j = 0; j < 8; j++) {
							cscdSums[j] /= numberTestRuns;
							refSums[j] /= numberTestRuns;
						}

						System.out.println(
								"Test, MTFA, MTD, MDR, MTR, Error rate, Evaluation time, Adding time, Total time");
						String cscdMeasures = "CSCD," + test + "," + cscdSums[0] + ", " + cscdSums[1] + ", "
								+ cscdSums[2] + ", " + cscdSums[3] + ", " + cscdSums[4] + ", " + cscdSums[5] + ", "
								+ cscdSums[6] + ", " + cscdSums[7];
						String refMeasures = "REF," + test + "," + refSums[0] + ", " + refSums[1] + ", " + refSums[2]
								+ ", " + refSums[3] + ", " + refSums[4] + ", " + refSums[7];
						System.out.println(cscdMeasures);
						System.out.println(refMeasures);
						results.add(cscdMeasures);
						results.add(refMeasures);
					}
				}
			}
		}

		// Write the results
		String filePath = "D:/Informatik/MSc/IV/Masterarbeit Porto/Results/ConceptChangeDetection/SubspaceRBF/Tests.txt";

		try {
			Files.write(Paths.get(filePath), results, StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private double[][] testRun(int[] changePoints, int changeLength, double speed, ArrayList<Double> trueChanges,
			int[] virtualDriftPoints) {
		int numberSamples = 0;

		ArrayList<Double> cscdDetectedChanges = new ArrayList<Double>();
		ArrayList<Double> refDetectedChanges = new ArrayList<Double>();
		AccuracyEvaluator cscdAccuracy = new AccuracyEvaluator();
		AccuracyEvaluator refAccuracy = new AccuracyEvaluator();

		int changeCounter = 0;
		int driftStart = 0;
		boolean finishedDrifts = false;
		if (changePoints != null) {
			driftStart = changePoints[0];
		}

		int virtualDriftCounter = 0;
		boolean finishedVirtualDrifts = false;
		if (virtualDriftPoints != null) {
			driftStart = changePoints[0];
		}
		
		//cscd.onAlarm();
		while (stream.hasMoreInstances() && numberSamples < numInstances) {
			if (numberSamples % 1000 == 0) {
				//System.out.println(streamHiCS.getNumberOfElements());
				//System.out.println("Found: " + cscd.getCurrentlyCorrelatedSubspaces().toString());
			}

			// Carry out the concept drift at the relevant points
			if (changePoints != null && !finishedDrifts) {
				if (numberSamples == driftStart) {
					// Before 0.05
					// 5: best 0.5
					stream.speedChangeOption.setValue(speed);
					changeCounter++;
				} else if (numberSamples > driftStart + changeLength - 1) {
					stream.speedChangeOption.setValue(0.0);
					if (changeCounter < changePoints.length) {
						driftStart = changePoints[changeCounter];
					} else {
						finishedDrifts = true;
					}

				}
			}

			if (virtualDriftPoints != null && !finishedVirtualDrifts) {
				if (numberSamples == virtualDriftPoints[virtualDriftCounter]) {
					stream.subspaceChange(changeLength);
					virtualDriftCounter++;
					if (changeCounter >= changePoints.length - 1) {
						finishedVirtualDrifts = true;
					}
				}
			}

			Instance inst = stream.nextInstance();

			// For accuracy
			int trueClass = (int) inst.classValue();
			int prediction = cscd.getClassPrediction(inst);
			cscdAccuracy.addClassLabel(trueClass);
			cscdAccuracy.addPrediction(prediction);
			prediction = Utils.maxIndex(refDetector.getVotesForInstance(inst));
			refAccuracy.addClassLabel(trueClass);
			refAccuracy.addPrediction(prediction);

			stopwatch.start("Total_CSCD");
			cscd.trainOnInstance(inst);
			stopwatch.stop("Total_CSCD");
			stopwatch.start("Total_REF");
			refDetector.trainOnInstance(inst);
			stopwatch.stop("Total_REF");

			if (cscd.isWarningDetected()) {
				// System.out.println("cscd: WARNING at " + numberSamples);
			} else if (cscd.isChangeDetected()) {
				cscdDetectedChanges.add((double) numberSamples);
				System.out.println("cscd: CHANGE at " + numberSamples);
			}

			if (refDetector.isWarningDetected()) {
				// System.out.println("refDetector: WARNING at " +
				// numberSamples);
			} else if (refDetector.isChangeDetected()) {
				refDetectedChanges.add((double) numberSamples);
				// System.out.println("refDetector: CHANGE at " +
				// numberSamples);
			}

			numberSamples++;
		}

		double[] cscdPerformanceMeasures = Evaluator.evaluateConceptChange(trueChanges, cscdDetectedChanges,
				changeLength, numInstances);
		double[] refPerformanceMeasures = Evaluator.evaluateConceptChange(trueChanges, refDetectedChanges,
				changeLength, numInstances);
		double[][] performanceMeasures = new double[2][5];
		for (int i = 0; i < 4; i++) {
			performanceMeasures[0][i] = cscdPerformanceMeasures[i];
			performanceMeasures[1][i] = refPerformanceMeasures[i];
		}
		performanceMeasures[0][4] = cscdAccuracy.calculateOverallErrorRate();
		performanceMeasures[1][4] = refAccuracy.calculateOverallErrorRate();
		String cscdP = "CSCD, ";
		String refP = "REF, ";
		for (int i = 0; i < 5; i++) {
			cscdP += performanceMeasures[0][i] + ", ";
			refP += performanceMeasures[1][i] + ", ";
		}
		System.out.println(cscdP);
		System.out.println(refP);
		results.add(cscdP);
		results.add(refP);

		List<String> errorRatesList = new ArrayList<String>();
		double[] cscSmoothedErrorRates = cscdAccuracy.calculateSmoothedErrorRates(1000);
		double[] refSmoothedErrorRates = refAccuracy.calculateSmoothedErrorRates(1000);
		for (int i = 0; i < cscdAccuracy.size(); i++) {
			errorRatesList.add(i + "," + cscSmoothedErrorRates[i] + "," + refSmoothedErrorRates[i]);
		}

		String filePath = "C:/Users/Vincent/Desktop/ErrorRates.csv";

		try {
			Files.write(Paths.get(filePath), errorRatesList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return performanceMeasures;
	}

	private SummarisationAdapter createSummarisationAdapter(StreamSummarisation ss) {
		boolean addDescription = false;
		if (summarisationDescription == null) {
			addDescription = true;
		}
		SummarisationAdapter adapter = null;
		switch (ss) {
		case CLUSTREE_DEPTHFIRST:
			ClusTree clusTree = new ClusTree();
			clusTree.horizonOption.setValue(horizon);
			clusTree.prepareForUse();
			adapter = new MicroclusteringAdapter(clusTree);
			summarisationDescription = "ClusTree, horizon: " + horizon;
			break;
		case ADAPTINGCENTROIDS:
			double radius = 10* Math.sqrt(numberOfDimensions) - 1;
			double learningRate = 1;
			adapter = new CentroidsAdapter(horizon, radius, learningRate, "adapting");
			summarisationDescription = "Adapting centroids, horizon: " + horizon + ", radius: " + radius
					+ ", learning rate: " + learningRate;
			break;
		default:
			adapter = null;
		}
		if (addDescription) {
			results.add(summarisationDescription);
		}
		return adapter;
	}

	private SubspaceBuilder createSubspaceBuilder(SubspaceBuildup sb, CorrelationSummary correlationSummary) {
		cutoff = 3;
		pruningDifference = 0.15;
		boolean addDescription = false;
		if (builderDescription == null) {
			addDescription = true;
		}
		SubspaceBuilder builder = null;
		switch (sb) {
		case APRIORI:
			builder = new AprioriBuilder(numberOfDimensions, aprioriThreshold, cutoff, contrastEvaluator,
					correlationSummary);
			builderDescription = "Apriori, threshold: " + aprioriThreshold + ", cutoff: " + cutoff;
			break;
		case HIERARCHICAL:
			builder = new HierarchicalBuilderCutoff(numberOfDimensions, hierarchicalThreshold, cutoff,
					contrastEvaluator, correlationSummary, true);
			builderDescription = "Hierarchical, threshold: " + hierarchicalThreshold + ", cutoff: " + cutoff;
			break;
		case CONNECTED_COMPONENTS:
			pruningDifference = 0.15;
			builder = new ComponentBuilder(numberOfDimensions, connectedComponentsThreshold, contrastEvaluator, correlationSummary);
			builderDescription = "Connnected Components, threshold: " + connectedComponentsThreshold;
			break;
		case CLIQUE:
			pruningDifference = 0.15;
			builder = new CliqueBuilder(numberOfDimensions, cliqueThreshold, contrastEvaluator, correlationSummary);
			builderDescription = "CliqueBuilder, threshold: " + cliqueThreshold;
			break;
		default:
			builder = null;
		}
		if (addDescription) {
			results.add(builderDescription);
		}
		return builder;
	}
}