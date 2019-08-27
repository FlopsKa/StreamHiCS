package subspacebuilder;

import fullsystem.Contrast;
import subspace.Subspace;
import subspace.SubspaceSet;

/**
 * This class represents a {@link SubspaceBuilder} using a top-down procedure to
 * find the correlated {@link Subspace}s. It starts with the full space and
 * recursively splits the {@link Subspace}s until correlated {@link Subspace}s
 * are found.
 * 
 * @author Vincent
 *
 */
public class HierarchicalBuilder extends SubspaceBuilder {

	/**
	 * A {@link SubspaceSet} containing the candidates for correlated
	 * {@link Subspace}s.
	 */
	private SubspaceSet correlatedSubspaces;

	/**
	 * A {@link SubspaceSet} containing the {@link Subspace}s which were
	 * assessed as not correlated.
	 */
	private SubspaceSet notCorrelatedSubspaces;

	/**
	 * The number of dimensions of the full space.
	 */
	private int numberOfDimensions;

	/**
	 * The minimum contrast value a {@link Subspace} must have to be a candidate
	 * for the correlated subspaces.
	 */
	private double threshold;

	/**
	 * The @link{Contrast} instance.
	 */
	private Contrast contrastEvaluator;

	/**
	 * Holds the contrast of all two-dimensional {@link Subspace}s.
	 */
	private double[][] contrastMatrix;

	/**
	 * A flag determining if a two {@link Subspace}s can overlap in dimensions
	 * or not.
	 */
	private boolean partition;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param numberOfDimensions
	 *            The number of dimensions of the full space
	 * @param threshold
	 *            The threshold
	 * @param contrastEvaluator
	 *            The {@link Contrast} instance
	 * @param partition
	 *            The flag determining whether to partition a {@link Subspace}
	 *            in the splitting process
	 */
	public HierarchicalBuilder(int numberOfDimensions, double threshold, Contrast contrastEvaluator,
			boolean partition) {
		this.correlatedSubspaces = new SubspaceSet();
		this.notCorrelatedSubspaces = new SubspaceSet();
		this.numberOfDimensions = numberOfDimensions;
		this.threshold = threshold;
		this.contrastEvaluator = contrastEvaluator;
		this.partition = partition;
	}

	@Override
	public SubspaceSet buildCorrelatedSubspaces() {
		correlatedSubspaces.clear();
		notCorrelatedSubspaces.clear();
		contrastMatrix = new double[numberOfDimensions][numberOfDimensions];
		double contrast = 0;
		// Calculate the contrast for all two dimensional subspaces and store
		// them in a lookup matrix since they are needed for splitting.
		Subspace s;
		for (int i = 0; i < numberOfDimensions - 1; i++) {
			for (int j = i + 1; j < numberOfDimensions; j++) {
				s = new Subspace();
				s.addDimension(i);
				s.addDimension(j);
				contrast = contrastEvaluator.evaluateSubspaceContrast(s);
				s.setContrast(contrast);
				contrastMatrix[i][j] = contrast;
				contrastMatrix[j][i] = contrast;
			}
		}

		// Create the full space
		Subspace fullSpace = new Subspace();
		for (int i = 0; i < numberOfDimensions; i++) {
			fullSpace.addDimension(i);
		}

		// Start the recursive procedure
		hierarchicalBuildup(fullSpace);

		return correlatedSubspaces;
	}

	private void hierarchicalBuildup(Subspace s) {
		double contrast = 0;
		int size = s.size();
		boolean finished = false;
		if (size < 2) {
			finished = true;
		} else if (size == 2) {
			contrast = contrastMatrix[s.getDimension(0)][s.getDimension(1)];
		} else {
			if (correlatedSubspaces.contains(s) || notCorrelatedSubspaces.contains(s)) {
				finished = true;
			} else {
				contrast = contrastEvaluator.evaluateSubspaceContrast(s);
			}
		}
		// System.out.println(s.toString() + " : " + contrast);

		// If we have already checked the subspace in another branch of the tree
		// we are finished, otherwise we carry on with calculating the contrast
		if (!finished) {
			s.setContrast(contrast);
			if (contrast >= threshold) {
				s.sort();
				correlatedSubspaces.addSubspace(s);
			} else {
				notCorrelatedSubspaces.addSubspace(s);
				if (size > 2) {
					SubspaceSet splittingResult = split(s);
					hierarchicalBuildup(splittingResult.getSubspace(0));
					hierarchicalBuildup(splittingResult.getSubspace(1));
				}
			}
		}
	}

	/**
	 * Split a {@link Subspace} by using the two dimensions with the lowest
	 * common contrast as seeds for the two new {@link Subspace}s.
	 * 
	 * @param s
	 *            The {@link Subspace} to be split
	 * @return A {@link SubspaceSet} containing the two new {@link Subspace}s.
	 */
	private SubspaceSet split(Subspace s) {
		int size = s.size();
		double minContrast = Double.MAX_VALUE;
		double contrast = 0;
		int minI = 0;
		int minJ = 0;
		for (int i = 0; i < size; i++) {
			for (int j = i + 1; j < size; j++) {
				contrast = contrastMatrix[s.getDimension(i)][s.getDimension(j)];
				if (contrast < minContrast) {
					minContrast = contrast;
					minI = i;
					minJ = j;
				}
			}
		}
		// minI and minJ are the splitting seeds
		SubspaceSet result = new SubspaceSet();
		Subspace sI = new Subspace();
		Subspace sJ = new Subspace();
		sI.addDimension(s.getDimension(minI));
		sJ.addDimension(s.getDimension(minJ));

		if (partition) {
			// We form new subspaces by adding dimensions to the seed which they
			// share a higher two dimensional contrast with. That means the
			// seeds attract the dimensions by contrast
			double iContrast = 0;
			double jContrast = 0;
			int iDim = s.getDimension(minI);
			int jDim = s.getDimension(minJ);
			int dim;
			for (int i = 0; i < s.size(); i++) {
				if (i != minI && i != minJ) {
					dim = s.getDimension(i);
					iContrast = contrastMatrix[iDim][dim];
					jContrast = contrastMatrix[jDim][dim];
					if (iContrast >= jContrast) {
						sI.addDimension(dim);
					} else {
						sJ.addDimension(dim);
					}
				}
			}
		} else {
			// We form two new subspaces by adding adding one seed to either of
			// them and add all the other dimensions
			int dim = 0;
			for (int i = 0; i < s.size(); i++) {
				if (i != minI && i != minJ) {
					dim = s.getDimension(i);
					sI.addDimension(dim);
					sJ.addDimension(dim);
				}
			}
		}
		result.addSubspace(sI);
		result.addSubspace(sJ);
		return result;
	}
}
