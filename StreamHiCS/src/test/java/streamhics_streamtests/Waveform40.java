package streamhics_streamtests;

import static org.junit.Assert.fail;

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
import com.github.vincentbecker.subspace.Subspace;
import com.github.vincentbecker.subspace.SubspaceSet;
import com.github.vincentbecker.subspacebuilder.AprioriBuilder;
import com.github.vincentbecker.subspacebuilder.SubspaceBuilder;

import environment.Stopwatch;
import moa.clusterers.clustree.ClusTree;
import moa.streams.generators.WaveformGenerator;
import weka.core.Instance;

public class Waveform40 {

	private WaveformGenerator stream;
	private StreamHiCS streamHiCS;
	private Callback callback = new Callback() {
		@Override
		public void onAlarm() {
			// System.out.println("onAlarm().");
		}
	};
	private int numInstances = 100000;
	private int checkInterval = 10000;
	private int numberSamples = 0;
	private int numberOfDimensions = 40;
	private static Stopwatch stopwatch;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		stopwatch = new Stopwatch();
	}
	
	@AfterClass
	public static void afterClass() {
		// System.out.println("Average TPvsFP-score: " + tpVSfpSum /
		// testCounter);
		// System.out.println("Average AMJS-score: " + amjsSum / testCounter);
		System.out.println(stopwatch.toString());
	}
	
	@Before
	public void setUp() throws Exception {
		stream = new WaveformGenerator();
		stream.addNoiseOption.set();
		stream.prepareForUse();
		
		int horizon = 1100;
		ClusTree mcs = new ClusTree();
		mcs.horizonOption.setValue(horizon);
		mcs.resetLearningImpl();
		
		double alpha = 0.25;
		double epsilon = 0;
		double threshold = 0.55;
		int cutoff = 8;
		double pruningDifference = 0.15;

		SummarisationAdapter adapter = new MicroclusteringAdapter(mcs);
		Contrast contrastEvaluator = new Contrast(50, alpha, adapter);
		ChangeChecker changeChecker = new TimeCountChecker(checkInterval);
		
		CorrelationSummary correlationSummary = new CorrelationSummary(numberOfDimensions, horizon);
		SubspaceBuilder subspaceBuilder = new AprioriBuilder(numberOfDimensions, threshold, cutoff, contrastEvaluator, correlationSummary);
		this.streamHiCS = new StreamHiCS(epsilon, threshold, pruningDifference, contrastEvaluator, subspaceBuilder,
				changeChecker, callback, correlationSummary, stopwatch);
		changeChecker.setCallback(streamHiCS);
	}

	@Test
	public void test() {
		int totalDimensions = 0;
		int noiseDimensions = 0;
		boolean noiseSubspace = false;
		int noiseSubspaces = 0;

		int numberOfSubspaces = 0;
		while (stream.hasMoreInstances() && numberSamples < numInstances) {
			Instance inst = stream.nextInstance();
			streamHiCS.add(inst);
			numberSamples++;
			if (numberSamples != 0 && numberSamples % checkInterval == 0) {
				System.out.println("Number of elements: " + streamHiCS.getNumberOfElements());
				SubspaceSet correlatedSubspaces = streamHiCS.getCurrentlyCorrelatedSubspaces();
				numberOfSubspaces += correlatedSubspaces.size();
				System.out.println("Correlated: " + correlatedSubspaces.toString());
				if (!correlatedSubspaces.isEmpty()) {
					for (Subspace s : correlatedSubspaces.getSubspaces()) {
						noiseSubspace = false;
						totalDimensions += s.size();
						for (int i = 0; i < s.size(); i++) {
							if (s.getDimension(i) > 21) {
								noiseDimensions++;
								noiseSubspace = true;
							}
						}
						if (noiseSubspace) {
							noiseSubspaces++;
						}
						System.out.print(s.getContrast() + ", ");
					}
					System.out.println();
				}
			}
		}

		System.out.println("Number of subspaces found: " + numberOfSubspaces + ", noise subspaces: " + noiseSubspaces
				+ ", noise subspace ratio: " + ((double) noiseSubspaces) / numberOfSubspaces + ", total dimensions: "
				+ totalDimensions + ", noise dimensions: " + noiseDimensions + ", noise dimension ratio: "
				+ ((double) noiseDimensions) / totalDimensions);
		fail("Not implemented yet. ");
	}

}
