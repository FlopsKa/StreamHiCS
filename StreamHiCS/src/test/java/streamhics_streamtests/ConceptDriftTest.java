package streamhics_streamtests;

import java.util.ArrayList;

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
import com.github.vincentbecker.streamdatastructures.MicroclusteringAdapter;
import com.github.vincentbecker.streamdatastructures.SummarisationAdapter;
import com.github.vincentbecker.streams.GaussianStream;
import com.github.vincentbecker.streams.UncorrelatedStream;
import com.github.vincentbecker.subspace.Subspace;
import com.github.vincentbecker.subspace.SubspaceSet;
import com.github.vincentbecker.subspacebuilder.AprioriBuilder;
import com.github.vincentbecker.subspacebuilder.SubspaceBuilder;

import environment.CSVReader;
import environment.Evaluator;
import environment.Stopwatch;
import moa.clusterers.clustree.ClusTree;
import moa.streams.ConceptDriftStream;
import weka.core.Instance;

public class ConceptDriftTest {

	private static CSVReader csvReader;
	private static final String path = "Tests/CovarianceMatrices/";
	private Callback callback = new Callback() {
		@Override
		public void onAlarm() {
			// System.out.println("onAlarm().");
		}
	};
	private ConceptDriftStream conceptDriftStream;
	private StreamHiCS streamHiCS;
	private ArrayList<SubspaceSet> correctResults;
	private static int testCounter = 0;
	private final int numInstances = 60000;
	private int numberSamples = 0;
	private static Stopwatch stopwatch;
	private static double tpVSfpSum = 0;
	private static double amjsSum = 0;
	private static double amssSum = 0;

	@BeforeClass
	public static void setUpBeforeClass() {
		csvReader = new CSVReader();
		stopwatch = new Stopwatch();
	}

	@AfterClass
	public static void calculateAverageScores() {
		System.out.println("Average TPvsFP-score: " + tpVSfpSum / testCounter);
		System.out.println("Average AMJS-score: " + amjsSum / testCounter);
		System.out.println("Average AMSS-score: " + amssSum / testCounter);
		System.out.println(stopwatch.toString());
	}

	@Before
	public void setUp() throws Exception {
		numberSamples = 0;
	}

	@Test
	public void test1() {
		correctResults = new ArrayList<SubspaceSet>();
		UncorrelatedStream s1 = new UncorrelatedStream();
		s1.dimensionsOption.setValue(5);
		s1.scaleOption.setValue(10);
		s1.prepareForUse();

		GaussianStream s2 = new GaussianStream(null, csvReader.read(path + "Test1.csv"), 1);

		GaussianStream s3 = new GaussianStream(null, csvReader.read(path + "Test5.csv"), 1);

		GaussianStream s4 = new GaussianStream(null, csvReader.read(path + "Test2.csv"), 1);

		GaussianStream s5 = new GaussianStream(null, csvReader.read(path + "Test3.csv"), 1);

		// GaussianStream s6 = new GaussianStream(csvReader.read(path +
		// "Test4.csv"));
		GaussianStream s6 = new GaussianStream(null, csvReader.read(path + "Test2.csv"), 1);

		ConceptDriftStream cds1 = new ConceptDriftStream();
		cds1.streamOption.setCurrentObject(s1);
		cds1.driftstreamOption.setCurrentObject(s2);
		cds1.positionOption.setValue(10000);
		cds1.widthOption.setValue(500);
		cds1.prepareForUse();

		ConceptDriftStream cds2 = new ConceptDriftStream();
		cds2.streamOption.setCurrentObject(cds1);
		cds2.driftstreamOption.setCurrentObject(s3);
		cds2.positionOption.setValue(20000);
		cds2.widthOption.setValue(500);
		cds2.prepareForUse();

		ConceptDriftStream cds3 = new ConceptDriftStream();
		cds3.streamOption.setCurrentObject(cds2);
		cds3.driftstreamOption.setCurrentObject(s4);
		cds3.positionOption.setValue(30000);
		cds3.widthOption.setValue(500);
		cds3.prepareForUse();

		ConceptDriftStream cds4 = new ConceptDriftStream();
		cds4.streamOption.setCurrentObject(cds3);
		cds4.driftstreamOption.setCurrentObject(s5);
		cds4.positionOption.setValue(40000);
		cds4.widthOption.setValue(500);
		cds4.prepareForUse();

		conceptDriftStream = new ConceptDriftStream();
		conceptDriftStream.streamOption.setCurrentObject(cds4);
		conceptDriftStream.driftstreamOption.setCurrentObject(s6);
		conceptDriftStream.positionOption.setValue(50000);
		conceptDriftStream.widthOption.setValue(500);
		conceptDriftStream.prepareForUse();

		// Adding the expected results for evaluation
		SubspaceSet cr5000 = new SubspaceSet();
		correctResults.add(cr5000);

		SubspaceSet cr10000 = new SubspaceSet();
		correctResults.add(cr10000);

		SubspaceSet cr15000 = new SubspaceSet();
		correctResults.add(cr15000);

		SubspaceSet cr20000 = new SubspaceSet();
		correctResults.add(cr20000);

		SubspaceSet cr25000 = new SubspaceSet();
		cr25000.addSubspace(new Subspace(0, 1, 2));
		correctResults.add(cr25000);

		SubspaceSet cr30000 = new SubspaceSet();
		cr30000.addSubspace(new Subspace(0, 1, 2));
		correctResults.add(cr30000);

		SubspaceSet cr35000 = new SubspaceSet();
		cr35000.addSubspace(new Subspace(0, 1));
		cr35000.addSubspace(new Subspace(2, 3, 4));
		correctResults.add(cr35000);

		SubspaceSet cr40000 = new SubspaceSet();
		cr40000.addSubspace(new Subspace(0, 1));
		cr40000.addSubspace(new Subspace(2, 3, 4));
		correctResults.add(cr40000);

		SubspaceSet cr45000 = new SubspaceSet();
		cr45000.addSubspace(new Subspace(0, 1, 2, 3, 4));
		correctResults.add(cr45000);

		SubspaceSet cr50000 = new SubspaceSet();
		cr50000.addSubspace(new Subspace(0, 1, 2, 3, 4));
		correctResults.add(cr50000);

		SubspaceSet cr55000 = new SubspaceSet();
		cr55000.addSubspace(new Subspace(0, 1));
		cr55000.addSubspace(new Subspace(2, 3, 4));
		correctResults.add(cr55000);

		SubspaceSet cr60000 = new SubspaceSet();
		cr60000.addSubspace(new Subspace(0, 1));
		cr60000.addSubspace(new Subspace(2, 3, 4));
		correctResults.add(cr60000);

		int horizon = 2000;
		ClusTree mcs = new ClusTree();
		mcs.horizonOption.setValue(2000);
		mcs.prepareForUse();

		double alpha = 0.1;
		double epsilon = 0;
		double threshold = 0.35;
		int cutoff = 8;
		double pruningDifference = 0.2;

		SummarisationAdapter adapter = new MicroclusteringAdapter(mcs);
		Contrast contrastEvaluator = new Contrast(50, alpha, adapter);
		ChangeChecker changeChecker = new TimeCountChecker(1000);
		CorrelationSummary correlationSummary = new CorrelationSummary(5, horizon);
		//CorrelationSummary correlationSummary = null;
		SubspaceBuilder subspaceBuilder = new AprioriBuilder(5, threshold, cutoff, contrastEvaluator,
				correlationSummary);
		// SubspaceBuilder subspaceBuilder = new FastBuilder(5, threshold,
		// pruningDifference, contrastEvaluator);
		this.streamHiCS = new StreamHiCS(epsilon, threshold, pruningDifference, contrastEvaluator, subspaceBuilder,
				changeChecker, callback, correlationSummary, stopwatch);
		changeChecker.setCallback(streamHiCS);

		while (conceptDriftStream.hasMoreInstances() && numberSamples < numInstances) {
			Instance inst = conceptDriftStream.nextInstance();
			streamHiCS.add(inst);
			numberSamples++;
			/*
			 * if (numberSamples % 1000 == 0) { System.out.println("Time: " +
			 * numberSamples); System.out.println("Number of elements: " +
			 * streamHiCS.getNumberOfElements()); }
			 */
			if (numberSamples != 0 && numberSamples % 5000 == 0) {
				evaluate();
			}
		}
	}

	private void evaluate() {
		System.out.println("Number of samples: " + numberSamples);
		SubspaceSet correctResult = correctResults.get(testCounter);
		SubspaceSet result = streamHiCS.getCurrentlyCorrelatedSubspaces();
		Evaluator.displayResult(result, correctResult);
		double tpVSfp = Evaluator.evaluateTPvsFP(result, correctResult);
		tpVSfpSum += tpVSfp;
		double amjs = Evaluator.evaluateJaccardIndex(result, correctResult);
		amjsSum += amjs;
		double amss = Evaluator.evaluateStructuralSimilarity(result, correctResult);
		amssSum += amss;
		System.out.println("TPvsFP: " + tpVSfp + "; AMJS: " + amjs + "; AMSS: " + amss);
		testCounter++;
	}

}
