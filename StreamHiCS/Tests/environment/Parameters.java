package environment;

public class Parameters {

	// SLIDINGWINDOW,, DENSTREAM, CLUSTREE, ADAPTINGCENTROIDS, RADIUSCENTROIDS
	public enum StreamSummarisation {
		 SLIDINGWINDOW, CLUSTREAM, DENSTREAM, CLUSTREE, ADAPTINGCENTROIDS, RADIUSCENTROIDS
	}

	// 
	public enum SubspaceBuildup {
		APRIORI, HIERARCHICAL
	}
}
