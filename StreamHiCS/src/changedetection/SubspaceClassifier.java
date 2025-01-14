package changedetection;

import java.util.ArrayList;

import moa.classifiers.trees.HoeffdingTree;
import moa.core.InstancesHeader;
import moa.core.ObjectRepository;
import moa.streams.InstanceStream;
import moa.tasks.TaskMonitor;
import subspace.Subspace;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class SubspaceClassifier extends HoeffdingTree implements SubspaceModel {
	/**
	 * The serial version ID.
	 */
	private static final long serialVersionUID = -2008369880916696270L;

	/**
	 * The subspace the {@link SubspaceChangeDetector} runs on.
	 */
	private Subspace subspace;

	/**
	 * The {@link InstancesHeader}.
	 */
	private InstancesHeader header;

	private double errorRate;

	private int numberInstances;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param subspace
	 *            The {@link Subspace} the {@link SubspaceChangeDetector} should
	 *            run on.
	 */
	public SubspaceClassifier(Subspace subspace) {
		this.subspace = subspace;
	}

	/**
	 * Returns the {@link Subspace} this {@link SubspaceChangeDetector} runs on.
	 * 
	 * @return The {@link Subspace} this {@link SubspaceChangeDetector} runs on.
	 */
	public Subspace getSubspace() {
		return this.subspace;
	}

	@Override
	public void prepareForUseImpl(TaskMonitor monitor, ObjectRepository repository) {
		errorRate = 1;
		numberInstances = 0;
		super.prepareForUseImpl(monitor, repository);
	}

	@Override
	public void resetLearningImpl() {
		errorRate = 1;
		numberInstances = 0;
		super.resetLearningImpl();
	}

	@Override
	public void trainOnInstanceImpl(Instance inst) {
		updateErrorRate(inst);
		super.trainOnInstanceImpl(projectInstance(inst));
	}

	@Override
	public double[] getVotesForInstance(Instance inst) {
		return super.getVotesForInstance(projectInstance(inst));
	}

	public int getClassPrediction(Instance instance) {
		return Utils.maxIndex(getVotesForInstance(instance));
	}

	@Override
	public double getAccuracy() {
		return 1 - errorRate;
	}

	@Override
	public Instance projectInstance(Instance instance) {
		int l = subspace.size();
		if (header == null) {
			ArrayList<Attribute> attributes = new ArrayList<Attribute>();
			for (int i = 0; i < l; i++) {
				attributes.add(new Attribute("projectedAtt" + subspace.getDimension(i)));
			}
			ArrayList<String> classLabels = new ArrayList<String>();
			for (int i = 0; i < instance.numClasses(); i++) {
				classLabels.add("class" + (i + 1));
			}
			attributes.add(new Attribute("class", classLabels));
			this.header = new InstancesHeader(new Instances(getCLICreationString(InstanceStream.class), attributes, 0));
			this.header.setClassIndex(l);

		}
		double[] subspaceData = new double[l + 1];
		for (int i = 0; i < l; i++) {
			subspaceData[i] = instance.value(subspace.getDimension(i));
		}
		// Setting the class label
		subspaceData[l] = instance.value(instance.classIndex());
		Instance subspaceInstance = new DenseInstance(instance.weight(), subspaceData);
		subspaceInstance.setDataset(header);
		return subspaceInstance;
	}

	// 0.0 = true
	// 1.0 = false
	private void updateErrorRate(Instance instance) {
		int trueClass = (int) instance.classValue();
		double prediction;
		if (getClassPrediction(instance) == trueClass) {
			prediction = 0.0;
		} else {
			prediction = 1.0;
		}
		numberInstances++;
		errorRate = errorRate + (prediction - errorRate) / numberInstances;
	}
}
