package streamhics_contrast;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import environment.CSVReader;
import environment.Stopwatch;
import fullsystem.Contrast;
import streamdatastructures.SlidingWindowAdapter;
import streamdatastructures.SummarisationAdapter;
import streams.GaussianStream;
import subspace.Subspace;
import weka.core.DenseInstance;
import weka.core.Instance;

public class SizeVariation {

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
	private static final int m = 100;
	private static double alpha;
	private GaussianStream stream;
	private CSVReader csvReader;
	private Stopwatch stopwatch;
	
	@Before
	public void setUp() throws Exception {
		alpha = 0.1;
		
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
		for (int windowLength = 100; windowLength <= 10000; windowLength += 100) {
			adapter = new SlidingWindowAdapter(5, windowLength);
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
			System.out.println(windowLength + "," + contrast + "," + stopwatch.getTime("Evaluation"));
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
