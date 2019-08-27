package streamhics_streamtests;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.vincentbecker.changechecker.ChangeChecker;
import com.github.vincentbecker.changechecker.TimeCountChecker;
import com.github.vincentbecker.fullsystem.Callback;
import com.github.vincentbecker.fullsystem.Contrast;
import com.github.vincentbecker.fullsystem.StreamHiCS;
import com.github.vincentbecker.streamdatastructures.CorrelationSummary;
import com.github.vincentbecker.streamdatastructures.MCAdapter;
import com.github.vincentbecker.streamdatastructures.MicroclusteringAdapter;
import com.github.vincentbecker.streamdatastructures.SlidingWindowAdapter;
import com.github.vincentbecker.streamdatastructures.SummarisationAdapter;
import com.github.vincentbecker.streams.GaussianStream;
import com.github.vincentbecker.subspace.Subspace;
import com.github.vincentbecker.subspace.SubspaceSet;
import com.github.vincentbecker.subspacebuilder.AprioriBuilder;
import com.github.vincentbecker.subspacebuilder.SubspaceBuilder;

import environment.Evaluator;
import environment.Stopwatch;
import moa.clusterers.clustree.ClusTree;
import moa.clusterers.denstream.WithDBSCAN;
import weka.core.Instance;

public class StreamTest {

	private static GaussianStream stream;
	private static StreamHiCS streamHiCS;
	private static SummarisationAdapter adapter;
	private static Contrast contrastEvaluator;
	private static final int numInstances = 10000;
	private static final int m = 20;
	private static double alpha;
	private static double epsilon;
	private static double threshold;
	private static int cutoff;
	private static double pruningDifference;
	private static double[][][] covarianceMatrices = {
			{ { 1, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0 }, { 0, 0, 1, 0, 0 }, { 0, 0, 0, 1, 0 }, { 0, 0, 0, 0, 1 } },
			{ { 1, 0.2, 0.1, 0, 0 }, { 0.2, 1, 0.1, 0, 0 }, { 0.1, 0.1, 1, 0.1, 0.1 }, { 0, 0, 0.1, 1, 0.1 },
					{ 0, 0, 0, 0.1, 1 } },
			{ { 1, 0.4, 0.2, 0, 0 }, { 0.4, 1, 0.2, 0, 0 }, { 0.2, 0.2, 1, 0.2, 0.2 }, { 0, 0, 0.2, 1, 0.2 },
					{ 0, 0, 0, 0.2, 1 } },
			{ { 1, 0.6, 0.4, 0, 0 }, { 0.6, 1, 0.4, 0, 0 }, { 0.4, 0.4, 1, 0.4, 0.4 }, { 0, 0, 0.4, 1, 0.4 },
					{ 0, 0, 0, 0.4, 1 } },
			{ { 1, 0.8, 0.6, 0, 0 }, { 0.8, 1, 0.6, 0, 0 }, { 0.6, 0.6, 1, 0.6, 0.6 }, { 0, 0, 0.6, 1, 0.6 },
					{ 0, 0, 0, 0.6, 1 } },
			{ { 1, 0.9, 0.8, 0.2, 0.2 }, { 0.9, 1, 0.8, 0.2, 0.2 }, { 0.8, 0.8, 1, 0.8, 0.8 },
					{ 0.2, 0.2, 0.8, 1, 0.8 }, { 0.2, 0.2, 0.2, 0.8, 1 } } };
	private static SubspaceSet correctResult;
	private static final String method = "ClusTreeMC";
	private static Callback callback = new Callback() {

		@Override
		public void onAlarm() {
			System.out.println("StreamHiCS: onAlarm()");
		}

	};
	private static Stopwatch stopwatch;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		stopwatch = new Stopwatch();
		stream = new GaussianStream(null, covarianceMatrices[0], 1);

		int horizon = 0;
		if (method.equals("slidingWindow")) {
			alpha = 0.05;
			epsilon = 0;
			threshold = 0.1;
			cutoff = 6;
			pruningDifference = 0.1;

			adapter = new SlidingWindowAdapter(covarianceMatrices[0].length, 2000);
		} else if (method.equals("adaptiveCentroids")) {
			alpha = 0.1;
			epsilon = 0;
			threshold = 0.23;
			cutoff = 8;
			pruningDifference = 0.15;

			horizon = 1000;
			double radius = 0.2;
			double learningRate = 0.1;

			adapter = new MCAdapter(horizon, radius, learningRate, "adapting");
		} else if (method.equals("DenStreamMC")) {
			alpha = 0.1;
			epsilon = 0;
			threshold = 0.3;
			cutoff = 8;
			pruningDifference = 0.15;

			WithDBSCAN mcs = new WithDBSCAN();
			mcs.speedOption.setValue(100);
			mcs.epsilonOption.setValue(0.5);
			mcs.betaOption.setValue(0.005);
			mcs.lambdaOption.setValue(0.005);
			mcs.resetLearningImpl();
			adapter = new MicroclusteringAdapter(mcs);

		} else if (method.equals("ClusTreeMC")) {
			alpha = 0.1;
			epsilon = 0.05;
			threshold = 0.25;
			cutoff = 8;
			pruningDifference = 0.1;

			ClusTree mcs = new ClusTree();
			mcs.horizonOption.setValue(horizon);
			mcs.prepareForUse();
			adapter = new MicroclusteringAdapter(mcs);

		} else {
			adapter = null;
		}

		contrastEvaluator = new Contrast(m, alpha, adapter);
		CorrelationSummary correlationSummary = new CorrelationSummary(covarianceMatrices[0].length, horizon);
		SubspaceBuilder subspaceBuilder = new AprioriBuilder(covarianceMatrices[0].length, threshold, cutoff,
				contrastEvaluator, correlationSummary);
		//SubspaceBuilder subspaceBuilder = new HierarchicalBuilderCutoff(covarianceMatrices[0].length, threshold, cutoff, contrastEvaluator, true);
		ChangeChecker changeChecker = new TimeCountChecker(numInstances);
		stopwatch = new Stopwatch();
		streamHiCS = new StreamHiCS(epsilon, threshold, pruningDifference, contrastEvaluator, subspaceBuilder,
				changeChecker, callback, correlationSummary, stopwatch);
		changeChecker.setCallback(streamHiCS);

		correctResult = new SubspaceSet();
	}

	@AfterClass
	public static void calculateAverageScores() {
		// System.out.println("Average TPvsFP-score: " + tpVSfpSum /
		// testCounter);
		// System.out.println("Average AMJS-score: " + amjsSum / testCounter);
		System.out.println(stopwatch.toString());
	}
	
	@Before
	public void setUp() {
		correctResult.clear();
	}

	@Test
	public void test1() {
		carryOutTest(0);
	}

	@Test
	public void test2() {
		correctResult.addSubspace(new Subspace(0, 1));
		carryOutTest(1);
	}

	@Test
	public void test3() {
		correctResult.addSubspace(new Subspace(0, 1));
		correctResult.addSubspace(new Subspace(1, 2));
		correctResult.addSubspace(new Subspace(0, 1, 2));
		correctResult.addSubspace(new Subspace(2, 3, 4));
		carryOutTest(2);
	}

	@Test
	public void test4() {
		correctResult.addSubspace(new Subspace(0, 1));
		correctResult.addSubspace(new Subspace(1, 2));
		correctResult.addSubspace(new Subspace(0, 1, 2));
		correctResult.addSubspace(new Subspace(2, 3, 4));
		carryOutTest(3);
	}

	@Test
	public void test5() {
		correctResult.addSubspace(new Subspace(0, 1));
		correctResult.addSubspace(new Subspace(0, 1, 2));
		correctResult.addSubspace(new Subspace(0, 1, 2, 3, 4));
		carryOutTest(4);
	}

	@Test
	public void test6() {
		correctResult.addSubspace(new Subspace(0, 1));
		correctResult.addSubspace(new Subspace(0, 1, 2));
		correctResult.addSubspace(new Subspace(0, 1, 2, 3, 4));
		carryOutTest(5);
	}

	private void carryOutTest(int iteration) {
		int numberSamples = 0;
		System.out.println("Iteration: " + (iteration + 1));
		stream.setCovarianceMatrix(covarianceMatrices[iteration]);
		numberSamples = 0;
		while (stream.hasMoreInstances() && numberSamples < numInstances) {
			Instance inst = stream.nextInstance();
			streamHiCS.add(inst);
			numberSamples++;
		}

		System.out.println("Number of elements: " + contrastEvaluator.getNumberOfElements());

		Evaluator.displayResult(streamHiCS.getCurrentlyCorrelatedSubspaces(), correctResult);
		assertTrue(Evaluator.evaluateTPvsFP(streamHiCS.getCurrentlyCorrelatedSubspaces(), correctResult) >= 0.75);
	}
}
