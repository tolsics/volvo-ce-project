package cbr;

import java.awt.Color;
import java.util.*;

import javax.swing.JFrame;

import org.apache.commons.math3.ml.clustering.*;
import org.math.plot.Plot2DPanel;

/**
 * Classifies cases via a clustering algorithm and a Naive Bayesian algorithm.
 * <p>
 * Refer to the actual member methods for a more detailed documentation 
 * of how to use this class.
 */
public class NaiveBayesClassifier {

	private final Set<DoublePoint> dataPoints;
	private Set<DoublePoint> noise;
	private final List<NaiveBayesCluster> clusters = new ArrayList<>();
	
	/**
	 * Creates a NaiveBayesClassifier from the given parameters.
	 * 
	 * @param clusterer the clustering strategy to use
	 * @param dataPoints the cases to be classified
	 */
	public NaiveBayesClassifier(Clusterer<DoublePoint> clusterer, Collection<DoublePoint> dataPoints) {
		this.dataPoints = Collections.unmodifiableSet(new HashSet<>(dataPoints));
		this.noise = new HashSet<>(dataPoints);
				
		int clusterOrdinal = 1;
		for (Cluster<DoublePoint> cluster : clusterer.cluster(dataPoints)) {						
			clusters.add(new NaiveBayesCluster(clusterOrdinal++, cluster));
		}
		
		for (Cluster<DoublePoint> c : clusters) {
			noise.removeAll(c.getPoints());
		}
		noise = Collections.unmodifiableSet(noise);
	}
	
	/**
	 * Returns the formed (via the given clustering algorithm) clusters.
	 */
	public List<NaiveBayesCluster> clusters() {
		return clusters;
	}
	
	/**
	 * Returns an unmodifiable view of the cases used by this classifier.
	 */
	public Set<DoublePoint> dataPoints() {
		return dataPoints;
	}
	
	/**
	 * The non clustered cases.
	 */
	public Set<DoublePoint> noise() {
		return noise;
	}
	/**
	 * Calculates probability for a given case (aka feature vector).
	 * <pre>
	 * p(x) = [ ( p(x1|c1) * p(x2|c1) * ... * p(xK|c1) ) * p(c1) ] 
	 *        +
	 *        [ ( p(x1|c2) * p(x2|c2) * ... * p(xK|c2) ) * p(c2) ]
	 *        +
	 *        .
	 *        .
	 *        .
	 *        +
	 *        [ ( p(x1|cM) * p(x2|cM) * ... * p(xK|cM) ) * p(cM) ]
	 *  
	 * </pre>
	 * This probability value is to be indexed by the M-tree. 
	 */
	public double caseProbability(DoublePoint dataPoint) {
		double result = 0.0;
		for (NaiveBayesCluster cluster : clusters) {
			int totalClusteredDataPoints = dataPoints.size() - noise.size();
			result += cluster.conditionalProbability(dataPoint, totalClusteredDataPoints);
		}
		return result;
	}
	
	/**
	 * Calculates the conditional class probability for every cluster.
	 * @param dataPoint the target case
	 * @return same dimension as the number of clusters 
	 */
	public double[] conditionalClassProbability(DoublePoint dataPoint) {
		double[] result = new double[clusters().size()];
		
		double sum = 0.0;
		for (int i = 0; i < clusters.size(); i++) {
			
			int totalClusteredDataPoints = dataPoints.size() - noise.size();
			result[i] = clusters.get(i).conditionalProbability(dataPoint, totalClusteredDataPoints);
			sum += result[i];
		}
		for (int i = 0; i < clusters.size(); i++)
			result[i] /= sum;
		
		return result;
	}
	
	/**
	 * Should only be used for test purposes
	 * @param args not used
	 */
	public static void main(String[] args) {
		
		final Random RANDOM = new Random();
		final int SIZE = 100;
		Set<DoublePoint> dataPoints = new HashSet<>();
		for (int i = 0; i < SIZE; i++) {
			dataPoints.add(new DoublePoint(new double[]{RANDOM.nextDouble(), RANDOM.nextDouble()}));
		}
		double eps = 0.12;
		int minPts = 4;
		DBSCANClusterer<DoublePoint> dbScan = new DBSCANClusterer<>(eps, minPts);
		
		NaiveBayesClassifier classifier = new NaiveBayesClassifier(dbScan, dataPoints);
		
//		try (FileWriter writer = new FileWriter("cluster.csv")) {
//			
//			for (Cluster<DoublePoint> cluster : dbScan.cluster(dataPoints)) {
//				
//				NaiveBayesCluster nb = new NaiveBayesCluster(cluster);			
//				naiveBayes.add(nb);
//				 
//				writer.write(nb.name() + "," + Arrays.toString(nb.mean()) + "\n");			
//				writer.write(nb.name() + "," + Arrays.toString(nb.variance()) + "\n");
//			}
//		}
		
		Plot2DPanel plot = new Plot2DPanel();
		plot.setFixedBounds(0, 0.0, 1.0);
		plot.setFixedBounds(1, 0.0, 1.0);
			
		for (NaiveBayesCluster c : classifier.clusters()) {			
			plot(plot, c.name(), Color.RED, c.getPoints());
		}

		plot(plot, "Noise", Color.BLUE, classifier.noise());
		
		DoublePoint lookup = new DoublePoint(new double[]{RANDOM.nextDouble(), RANDOM.nextDouble()});
		plot(plot, "Lookup", Color.PINK, Collections.singleton(lookup));
		
		double[] probability = classifier.conditionalClassProbability(lookup);
		for (int i = 0; i < probability.length; i++) {			
			System.out.println("probability for cluster: " + i + ": " + probability[i]); 
		}
		
		// put the PlotPanel in a JFrame, as a JPanel
		JFrame frame = new JFrame("AI");
		frame.add(plot);
		frame.setSize(1000, 750);
		frame.setLocationRelativeTo(null);	// Center on screen
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	private static void plot(Plot2DPanel plot, String name, Color color, Collection<DoublePoint> dataPoints) {
		double xy[][] = new double[dataPoints.size()][];
		int i = 0;
		for (DoublePoint dp : dataPoints) {
			xy[i++] = dp.getPoint();
		}
		if (xy.length > 0)
			plot.addScatterPlot(name, color, xy);
	}
	
	/*
	public double[] conditionalProbability(DoublePoint dataPoint, int index) {
		double[] result = new double[clusters.size()];
		double sumOfNormalizedValues = 0.0;
		
		for (int i = 0; i < clusters.size(); i++) {
			NaiveBayesCluster cluster = clusters.get(i); 
			result[i] = cluster.conditionalFeatureProbability(dataPoint, index);
			sumOfNormalizedValues += result[i];
			
			double clusterProbability = (double)cluster.getPoints().size() / ( dataPoints.size() - noise.size() );
			
			result[i] *= clusterProbability;
		}
		for (int i = 0; i < result.length; i++) {
			result[i] /= sumOfNormalizedValues;
		}
		return result;
	}
	*/
}
