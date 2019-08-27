package ccd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.vincentbecker.changechecker.ChangeChecker;
import com.github.vincentbecker.changechecker.TimeCountChecker;
import com.github.vincentbecker.changedetection.FullSpaceChangeDetector;
import com.github.vincentbecker.changedetection.SubspaceChangeDetectors;
import com.github.vincentbecker.changedetection.SubspaceClassifiersChangeDetector;
import com.github.vincentbecker.clustree.ClusTree;
import com.github.vincentbecker.fullsystem.Contrast;
import com.github.vincentbecker.fullsystem.StreamHiCS;
import com.github.vincentbecker.streamdatastructures.CorrelationSummary;
import com.github.vincentbecker.streamdatastructures.MCAdapter;
import com.github.vincentbecker.streamdatastructures.MicroclusteringAdapter;
import com.github.vincentbecker.streamdatastructures.SummarisationAdapter;
import com.github.vincentbecker.subspacebuilder.AprioriBuilder;
import com.github.vincentbecker.subspacebuilder.ComponentBuilder;
import com.github.vincentbecker.subspacebuilder.HierarchicalBuilderCutoff;
import com.github.vincentbecker.subspacebuilder.SubspaceBuilder;

import environment.AccuracyEvaluator;
import environment.Parameters.StreamSummarisation;
import environment.Parameters.SubspaceBuildup;
import environment.Stopwatch;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.trees.HoeffdingTree;
import moa.core.InstancesHeader;
import moa.streams.ArffFileStream;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class ForestCovertype {

	private String path;
	private ArffFileStream stream;
	private static Stopwatch stopwatch1;
	private static Stopwatch stopwatch2;
	private static final int numberTestRuns = 1;
	private SubspaceChangeDetectors scd;
	private SubspaceClassifiersChangeDetector sccd;
	private FullSpaceChangeDetector refDetector;
	private HoeffdingTree refClassifier;
	private String summarisationDescription = null;
	private String builderDescription = null;
	private List<String> results;
	private int numberOfDimensions;

	@BeforeClass
	public static void setUpBeforeClass() {
		stopwatch1 = new Stopwatch();
		stopwatch2 = new Stopwatch();
	}

	@AfterClass
	public static void afterClass() {
		// System.out.println(stopwatch.toString());
	}

	@Before
	public void setup() {
		results = new LinkedList<String>();
	}

	@After
	public void after() {
		String filePath = "D:/Informatik/MSc/IV/Masterarbeit Porto/Results/ConceptChangeDetection/RealWorldData/ForestCovertype_Results.txt";

		try {
			Files.write(Paths.get(filePath), results, StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void forestCovertypeUnsorted() {
		path = "Tests/RealWorldData/covertypeNorm_filtered.arff";
		// Class index is last attribute but not relevant for this task
		stream = new ArffFileStream(path, -1);

		StreamSummarisation summarisation = StreamSummarisation.ADAPTINGCENTROIDS;
		SubspaceBuildup buildup = SubspaceBuildup.APRIORI;

		numberOfDimensions = 50;
		double threshold = 0;
		switch (summarisation) {
		case CLUSTREE_DEPTHFIRST:
			threshold = 0.55;
			break;
		case ADAPTINGCENTROIDS:
			if(numberOfDimensions == 10){
				threshold = 0.3;
			}else{
				threshold = 0.25;
			}
			break;
		case RADIUSCENTROIDS:
			threshold = 0.35;
			break;
		default:
			break;
		}

		int m = 50;
		double alpha;
		double epsilon;
		int cutoff;
		double pruningDifference;
		int horizon = 10000;
		int checkCount = 10000;
		if(numberOfDimensions == 10){
			alpha = 0.1;
			epsilon = 0.2;
			cutoff = 8;
			pruningDifference = 0.15;
		}else{
			alpha = 0.1;
			epsilon = 0.2;
			cutoff = 8;
			pruningDifference = 0.15;
		}

		System.out.println("ForestCovertype filtered unsorted");
		carryOutTest(numberOfDimensions, m, alpha, epsilon, threshold, cutoff, pruningDifference, horizon, checkCount,
				summarisation, buildup);
	}

	private void carryOutTest(int numberOfDimensions, int m, double alpha, double epsilon, double threshold, int cutoff,
			double pruningDifference, int horizon, int checkCount, StreamSummarisation summarisation,
			SubspaceBuildup buildup) {

		// Creating the SCD system
		SummarisationAdapter adapter1 = createSummarisationAdapter(summarisation, numberOfDimensions, horizon);
		Contrast contrastEvaluator1 = new Contrast(m, alpha, adapter1);
		// CorrelationSummary correlationSummary1 = new CorrelationSummary(numberOfDimensions, horizon);
		CorrelationSummary correlationSummary1 = null;
		SubspaceBuilder subspaceBuilder1 = createSubspaceBuilder(buildup, numberOfDimensions, contrastEvaluator1,
				correlationSummary1, threshold, cutoff);
		ChangeChecker changeChecker1 = new TimeCountChecker(checkCount);
		StreamHiCS streamHiCS1 = new StreamHiCS(epsilon, threshold, pruningDifference, contrastEvaluator1,
				subspaceBuilder1, changeChecker1, null, correlationSummary1, stopwatch1);
		changeChecker1.setCallback(streamHiCS1);
		scd = new SubspaceChangeDetectors(numberOfDimensions, streamHiCS1);
		scd.useRestspaceOption.setValue(false);
		scd.initOption.setValue(5000);
		scd.prepareForUse();
		streamHiCS1.addCallback(scd);

		// Creating the SCCD system
		/*
		SummarisationAdapter adapter2 = createSummarisationAdapter(summarisation, numberOfDimensions, horizon);
		Contrast contrastEvaluator2 = new Contrast(m, alpha, adapter2);
		//CorrelationSummary correlationSummary2 = new CorrelationSummary(numberOfDimensions, horizon);
		CorrelationSummary correlationSummary2 = null;
		SubspaceBuilder subspaceBuilder2 = createSubspaceBuilder(buildup, numberOfDimensions, contrastEvaluator2,
				correlationSummary2, threshold, cutoff);
		ChangeChecker changeChecker2 = new TimeCountChecker(checkCount);
		StreamHiCS streamHiCS2 = new StreamHiCS(epsilon, threshold, pruningDifference, contrastEvaluator2,
				subspaceBuilder2, changeChecker2, null, correlationSummary2, stopwatch2);
		changeChecker2.setCallback(streamHiCS2);
		*/
		sccd = new SubspaceClassifiersChangeDetector(numberOfDimensions, streamHiCS1);
		sccd.useRestspaceOption.setValue(false);
		sccd.initOption.setValue(5000);
		sccd.prepareForUse();
		streamHiCS1.addCallback(sccd);

		// Creating the reference detector
		refDetector = new FullSpaceChangeDetector();
		AbstractClassifier baseLearner = new HoeffdingTree();
		baseLearner.prepareForUse();
		refDetector.baseLearnerOption.setCurrentObject(baseLearner);
		refDetector.prepareForUse();
		
		refClassifier = new HoeffdingTree();
		refClassifier.prepareForUse();
		
		stopwatch1.reset();
		stopwatch2.reset();
		double[][] totalMeasures = new double[4][2];
		for (int i = 0; i < numberTestRuns; i++) {
			System.out.println("Run: " + (i + 1));
			double[][] measures = testRun();
			for(int j = 0; j < 4; j++){
				for(int k = 0; k < 2; k++){
					totalMeasures[j][k] += measures[j][k];
				}
			}
		}

		for(int j = 0; j < 4; j++){
			for(int k = 0; k < 2; k++){
				totalMeasures[j][k] /= numberTestRuns;
			}
		}

		double evaluationTime = stopwatch1.getTime("Evaluation") / numberTestRuns;
		double addingTime = stopwatch1.getTime("Adding") / numberTestRuns;
		double scdRuntime = stopwatch1.getTime("Total_SCD") / numberTestRuns;
		double sccdRuntime = stopwatch2.getTime("Total_SCCD") / numberTestRuns;
		double refRuntime = stopwatch1.getTime("Total_REF") / numberTestRuns;
		double refClassifierRuntime = stopwatch1.getTime("Total_REFCLASSIFIER") / numberTestRuns;
		
		System.out.println(
				"Number changes, error rate, Evaluation time, Adding time, Total time");
		String scdMeasures = "SCD, " + totalMeasures[0][0] + ", " + totalMeasures[0][1] + ", " + evaluationTime + ", " + addingTime + ", " + scdRuntime;
		String sccdMeasures = "SCCD, " + totalMeasures[1][0] + ", " + totalMeasures[1][1] + ", " + evaluationTime + ", " + addingTime + ", " + (sccdRuntime + evaluationTime + addingTime);
		String refMeasures = "REF, " + totalMeasures[2][0] + ", " + totalMeasures[2][1] + ", " + 0 + ", " + 0 + ", " + refRuntime;
		String refClassifierMeasures = "REF classifier, " + 0 + ", " + totalMeasures[3][1] + ", " + 0 + ", " + 0 + ", " + refClassifierRuntime;
		System.out.println(scdMeasures);
		System.out.println(sccdMeasures);
		System.out.println(refMeasures);
		System.out.println(refClassifierMeasures);
		results.add(scdMeasures);
		results.add(sccdMeasures);
		results.add(refMeasures);
		results.add(refClassifierMeasures);

	}

	private double[][] testRun() {
		stream.restart();
		scd.resetLearning();
		sccd.resetLearning();
		refDetector.resetLearning();

		int numberSamples = 0;

		AccuracyEvaluator scdAccuracy = new AccuracyEvaluator();
		AccuracyEvaluator sccdAccuracy = new AccuracyEvaluator();
		AccuracyEvaluator refAccuracy = new AccuracyEvaluator();
		AccuracyEvaluator refClassifierAccuracy = new AccuracyEvaluator();

		int numberChangesSCD = 0;
		int numberChangesSCCD = 0;
		int numberChangesREF = 0;

		InstancesHeader header = null;
		Random rand = new Random();
		while (stream.hasMoreInstances()) {
			Instance inst = stream.nextInstance();

			if (numberSamples % 10000 == 0) {
				System.out.println(scd.getNumberOfElements());
			}
			
			if (header == null) {
				ArrayList<Attribute> attributes = new ArrayList<Attribute>();
				for (int i = 0; i < 10; i++) {
					Attribute a = new Attribute("original" + i);
					attributes.add(a);
				}
				for (int i = 10; i < numberOfDimensions; i++) {
					Attribute a = new Attribute("noise" + i);
					attributes.add(a);
				}
				ArrayList<String> classLabels = new ArrayList<String>();
				classLabels.add("1");
				classLabels.add("2");
				classLabels.add("3");
				classLabels.add("4");
				classLabels.add("5");
				classLabels.add("6");
				classLabels.add("7");
				attributes.add(new Attribute("class", classLabels));
				header = new InstancesHeader(new Instances("AugmentedStream", attributes, 0));
				header.setClassIndex(numberOfDimensions);
			}
			double[] newData = new double[numberOfDimensions + 1];
			for (int i = 0; i < 10; i++) {
				newData[i] = inst.value(i);
			}
			for (int i = 10; i < numberOfDimensions; i++) {
				newData[i] = rand.nextDouble();
			}
			// Setting the class label
			newData[numberOfDimensions] = inst.value(inst.classIndex());
			inst = new DenseInstance(inst.weight(), newData);
			inst.setDataset(header);

			// For accuracy
			int trueClass = (int) inst.classValue();
			int scdPrediction = scd.getClassPrediction(inst);
			scdAccuracy.addClassLabel(trueClass);
			scdAccuracy.addPrediction(scdPrediction);
			int sccdPrediction = sccd.getClassPrediction(inst);
			sccdAccuracy.addClassLabel(trueClass);
			sccdAccuracy.addPrediction(sccdPrediction);
			int refPrediction = Utils.maxIndex(refDetector.getVotesForInstance(inst));
			refAccuracy.addClassLabel(trueClass);
			refAccuracy.addPrediction(refPrediction);
			int refClassifierPrediction = Utils.maxIndex(refDetector.getVotesForInstance(inst));
			refClassifierAccuracy.addClassLabel(trueClass);
			refClassifierAccuracy.addPrediction(refClassifierPrediction);
			
			if(scdPrediction != 0 || sccdPrediction != 0){
				//System.out.println("SCD: " + scdPrediction + ", SCCD: " + sccdPrediction + ", REF: " + refPrediction);
			}
			
			stopwatch1.start("Total_SCD");
			scd.trainOnInstance(inst);
			stopwatch1.stop("Total_SCD");
			stopwatch2.start("Total_SCCD");
			sccd.trainOnInstance(inst);
			stopwatch2.stop("Total_SCCD");
			stopwatch1.start("Total_REF");
			refDetector.trainOnInstance(inst);
			stopwatch1.stop("Total_REF");
			stopwatch1.start("Total_REFCLASSIFIER");
			refClassifier.trainOnInstance(inst);
			stopwatch1.stop("Total_REFCLASSIFIER");
			
			if (scd.isWarningDetected()) {
				// System.out.println("cscd: WARNING at " + numberSamples);
			} else if (scd.isChangeDetected()) {
				numberChangesSCD++;
				// cscdDetectedChanges.add((double) numberSamples);
				// System.out.println("cscd: CHANGE at " + numberSamples);
			}

			if (sccd.isWarningDetected()) {
				// System.out.println("cscd: WARNING at " + numberSamples);
			} else if (sccd.isChangeDetected()) {
				numberChangesSCCD++;
				// cscdDetectedChanges.add((double) numberSamples);
				// System.out.println("cscd: CHANGE at " + numberSamples);
			}

			if (refDetector.isWarningDetected()) {
				// System.out.println("refDetector: WARNING at " +
				// numberSamples);
			} else if (refDetector.isChangeDetected()) {
				numberChangesREF++;
				// refDetectedChanges.add((double) numberSamples);
				// System.out.println("refDetector: CHANGE at " +
				// numberSamples);
			}

			numberSamples++;
		}

		double[][] measures = new double[4][2];
		measures[0][0] = numberChangesSCD;
		measures[1][0] = numberChangesSCCD;
		measures[2][0] = numberChangesREF;
		
		measures[0][1] = scdAccuracy.calculateOverallErrorRate();
		measures[1][1] = sccdAccuracy.calculateOverallErrorRate();
		measures[2][1] = refAccuracy.calculateOverallErrorRate();
		measures[3][1] = refClassifierAccuracy.calculateOverallErrorRate();

		String scdP = "SCD, " + measures[0][0] + ", " + measures[0][1];
		String sccdP = "SCCD, " + measures[1][0] + ", " + measures[1][1];
		String refP = "REF, " + + measures[2][0] + ", " + measures[2][1];
		String refClassifierP = "REF classifier, " + measures[3][0] + ", " + measures[3][1];
		System.out.println(scdP);
		System.out.println(sccdP);
		System.out.println(refP);
		System.out.println(refClassifierP);
		results.add(scdP);
		results.add(sccdP);
		results.add(refP);
		results.add(refClassifierP);
		
		List<String> errorRatesList = new ArrayList<String>();
		double[] cscdSmoothedErrorRates = scdAccuracy.calculateSmoothedErrorRates(1000);
		double[] sccdSmoothedErrorRates = sccdAccuracy.calculateSmoothedErrorRates(1000);
		double[] refSmoothedErrorRates = refAccuracy.calculateSmoothedErrorRates(1000);
		double[] refClassifierSmoothedErrorRates = refClassifierAccuracy.calculateSmoothedErrorRates(1000);
		for (int i = 0; i < scdAccuracy.size(); i++) {
			errorRatesList.add(i + "," + cscdSmoothedErrorRates[i] + "," + sccdSmoothedErrorRates[i] + ","
					+ refSmoothedErrorRates[i] + "," + refClassifierSmoothedErrorRates[i]);
		}

		String filePath = "C:/Users/Vincent/Desktop/ErrorRates.csv";

		try {
			Files.write(Paths.get(filePath), errorRatesList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return measures;
	}

	private SummarisationAdapter createSummarisationAdapter(StreamSummarisation ss, int numberOfDimensions,
			int horizon) {
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
			double radius;
			if(numberOfDimensions == 10){
				radius = 14 * Math.sqrt(numberOfDimensions);

			}else{
				radius = 14 * Math.sqrt(numberOfDimensions);
			}
			double learningRate = 1;
			adapter = new MCAdapter(horizon, radius, learningRate, "adapting");
			summarisationDescription = "Adapting centroids, horizon: " + horizon + ", radius: " + radius
					+ ", learning rate: " + learningRate;
			break;
		case RADIUSCENTROIDS:
			//double radius = 4 * Math.sqrt(numberOfDimensions);
			radius = 0.25;
			learningRate = 1;
			adapter = new MCAdapter(horizon, radius, learningRate, "radius");
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

	private SubspaceBuilder createSubspaceBuilder(SubspaceBuildup sb, int numberOfDimensions,
			Contrast contrastEvaluator, CorrelationSummary correlationSummary, double threshold, int cutoff) {
		boolean addDescription = false;
		if (builderDescription == null) {
			addDescription = true;
		}
		SubspaceBuilder builder = null;
		switch (sb) {
		case APRIORI:
			builder = new AprioriBuilder(numberOfDimensions, threshold, cutoff, contrastEvaluator, correlationSummary);
			builderDescription = "Apriori, threshold: " + threshold + ", cutoff: " + cutoff;
			break;
		case HIERARCHICAL:
			builder = new HierarchicalBuilderCutoff(numberOfDimensions, threshold, cutoff, contrastEvaluator,
					correlationSummary, true);
			builderDescription = "Hierarchical, threshold: " + threshold + ", cutoff: " + cutoff;
			break;
		case CONNECTED_COMPONENTS:
			builder = new ComponentBuilder(numberOfDimensions, threshold, contrastEvaluator, correlationSummary);
			builderDescription = "Connnected Components, threshold: " + threshold;
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