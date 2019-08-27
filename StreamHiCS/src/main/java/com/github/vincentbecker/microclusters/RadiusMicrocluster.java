package com.github.vincentbecker.microclusters;

/**
 * This class implements a micro-clustering approach similar to DenStream, but
 * without an extra pruning routine. The radius is adapted to the incoming
 * {@link Instance}s.
 * 
 * @author Vincent
 *
 */
public class RadiusMicrocluster extends Microcluster {
	private static final double RADIUS_FACTOR = 1.8;
	private double[] LS;
	private double[] SS;
	private double maxRadius;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param vector
	 *            The initial centre of the new {@link Microcluster}
	 * @param negLambda
	 *            The negative lambda for fading
	 * @param currentTime
	 *            The current time
	 * @param maxRadius
	 *            The maximum radius of a {@link Microcluster}
	 */
	public RadiusMicrocluster(double[] vector, double negLambda, int currentTime, double maxRadius) {
		super(negLambda, currentTime);
		this.LS = vector;
		int l = LS.length;
		this.SS = new double[l];
		for (int i = 0; i < l; i++) {
			SS[i] = Math.pow(LS[i], 2);
		}
		this.maxRadius = maxRadius;
	}

	@Override
	public boolean addPointImpl(double[] point) {

		// Adding the point and testing the maximum radius
		int d = LS.length;
		double[] newLS = new double[d];
		double[] newSS = new double[d];
		double newWeight = weight + 1;

		double val;
		for (int i = 0; i < d; i++) {
			val = point[i];
			newLS[i] = LS[i] + val;
			newSS[i] = SS[i] + Math.pow(val, 2);
		}

		double newRadius = calculateRadius(newLS, newSS, newWeight);

		if (newRadius <= maxRadius) {
			// Add the new point
			LS = newLS;
			SS = newSS;
			weight = newWeight;

			return true;
		}

		return false;
	}

	@Override
	public void fadeImpl(int currentTime) {
		double fadingFactor = Math.pow(2.0, negLambda * (currentTime - lastUpdate));
		for (int i = 0; i < LS.length; i++) {
			LS[i] = LS[i] * fadingFactor;
			SS[i] = SS[i] * fadingFactor;
		}
	}

	@Override
	public double[] getCentre() {
		int l = LS.length;
		double[] centre = new double[l];
		for (int i = 0; i < l; i++) {
			centre[i] = LS[i] / weight;
		}
		return centre;
	}

	@Override
	public double getRadiusImpl() {
		return calculateRadius(LS, SS, weight);
	}

	/**
	 * Calculate the radius of a {@link Microcluster}.
	 * @param LS Linear sums cluster feature
	 * @param SS Square sums cluster feature
	 * @param weight The weight
	 * @return The radius of the {@link Microcluster} with the given characteristics. 
	 */
	private double calculateRadius(double[] LS, double[] SS, double weight) {
		double max = 0;
		double r = 0;
		for (int i = 0; i < LS.length; i++) {
			r = Math.sqrt(SS[i] / weight - Math.pow(LS[i] / weight, 2));
			if (r > max) {
				max = r;
			}
		}
		return RADIUS_FACTOR * max;
	}
}
