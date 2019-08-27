package streamhics_contrast;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.github.vincentbecker.fullsystem.Contrast;
import com.github.vincentbecker.streamdatastructures.MCAdapter;
import com.github.vincentbecker.streamdatastructures.SummarisationAdapter;
import com.github.vincentbecker.streams.GaussianStream;
import com.github.vincentbecker.subspace.Subspace;

import environment.CSVReader;
import environment.Stopwatch;
import weka.core.DenseInstance;
import weka.core.Instance;

public class MVariation {

	private static Contrast contrastEvaluator;
	private static SummarisationAdapter adapter;
	/**
	 * The number of instances used in the test.
	 */
	private static final int numInstances = 10000;
	/**
	 * The {@link Subspace} which should be tested.
	 */
	private static Subspace subspace;
	private static double alpha;
	private GaussianStream stream;
	private CSVReader csvReader;
	private Stopwatch stopwatch;
	
	@Before
	public void setUp() throws Exception {
		alpha = 0.1;
		/*
		ClusTree mcs = new ClusTree();
		mcs.horizonOption.setValue(1000);
		mcs.resetLearning();
		adapter = new MicroclusteringAdapter(mcs);
		*/
		int horizon = 2000;
		double radius = 0.3;
		double learningRate = 0.1;

		adapter = new MCAdapter(horizon, radius, learningRate, "radius");
		
		subspace = new Subspace();
		for(int i = 0; i < 5; i++){
			subspace.addDimension(i);
		}
		stopwatch = new Stopwatch();
		csvReader = new CSVReader();
		double[][] covarianceMatrix = csvReader.read("Tests/CovarianceMatrices/Test3.csv");
		stream = new GaussianStream(null, covarianceMatrix, 1);
	}

	@Test
	public void mVariation() {
		for (int m = 1; m <= 100; m++) {
			adapter.clear();
			contrastEvaluator = new Contrast(m, alpha, adapter);
			stopwatch.reset();
			for (int i = 0; i < numInstances; i++) {
				Instance inst = stripClassLabel(stream.nextInstance());
				contrastEvaluator.add(inst);
			}
			//System.out.println(contrastEvaluator.getNumberOfElements());
			stopwatch.start("Evaluation");
			double contrast = contrastEvaluator.evaluateSubspaceContrast(subspace);
			stopwatch.stop("Evaluation");
			System.out.println(m + "," + contrast + "," + stopwatch.getTime("Evaluation"));
		}
		fail();
	}

	private Instance stripClassLabel(Instance instance) {
		int numAtts = instance.numAttributes() - 1;
		Instance inst = new DenseInstance(numAtts);
		for (int i = 0; i < numAtts; i++) {
			inst.setValue(i, instance.value(i));
		}
		return inst;
	}
}
