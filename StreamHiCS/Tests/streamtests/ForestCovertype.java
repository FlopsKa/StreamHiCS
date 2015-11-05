package streamtests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import changechecker.ChangeChecker;
import changechecker.TimeCountChecker;
import contrast.Contrast;
import contrast.MicroclusterContrast;
import fullsystem.Callback;
import fullsystem.StreamHiCS;
import moa.clusterers.clustree.ClusTree;
import moa.streams.ArffFileStream;
import subspace.Subspace;
import subspacebuilder.AprioriBuilder;
import subspacebuilder.SubspaceBuilder;
import weka.core.Instance;

/**
 * The change points in the data are: 211840, 495141, 530895, 533642, 543135, 560502
 * 
 * @author Vincent
 *
 */
public class ForestCovertype {

	private static final String path = "Tests/RealWorldData/Covertype_sorted.arff";
	private ArffFileStream stream;
	private int numberSamples = 0;
	private Callback callback = new Callback() {
		@Override
		public void onAlarm() {
			System.out.println("StreamHiCS: onAlarm() at " + numberSamples);
		}
	};
	private StreamHiCS streamHiCS;

	@Before
	public void setUp() throws Exception {
		// Class index is last attribute but not relevant for this task
		stream = new ArffFileStream(path, -1);
	}

	@Test
	public void test() {
		int numberOfDimensions = 54;
		int m = 20;
		double alpha = 0.2;
		double epsilon = 0.2;
		double threshold = 0.6;
		int cutoff = 8;
		double pruningDifference = 0.15;

		ClusTree mcs = new ClusTree();
		mcs.resetLearningImpl();
		Contrast contrastEvaluator = new MicroclusterContrast(m, alpha, mcs);

		SubspaceBuilder subspaceBuilder = new AprioriBuilder(numberOfDimensions, threshold, cutoff, pruningDifference,
				contrastEvaluator);

		// SubspaceBuilder subspaceBuilder = new
		// FastBuilder(covarianceMatrix.length, threshold, pruningDifference,
		// contrastEvaluator);

		ChangeChecker changeChecker = new TimeCountChecker(10000);
		streamHiCS = new StreamHiCS(epsilon, threshold, contrastEvaluator, subspaceBuilder, changeChecker, callback);
		changeChecker.setCallback(streamHiCS);

		while (stream.hasMoreInstances()) {
			Instance inst = stream.nextInstance();
			streamHiCS.add(inst);
			numberSamples++;
			if (numberSamples % 10000 == 0) {
				System.out.println("Correlated: " + streamHiCS.toString());
				if (!streamHiCS.getCurrentlyCorrelatedSubspaces().isEmpty()) {
					for (Subspace s : streamHiCS.getCurrentlyCorrelatedSubspaces().getSubspaces()) {
						System.out.print(s.getContrast() + ", ");
					}
					System.out.println();
				}
			}
		}

		fail("Not yet implemented");
	}

}
