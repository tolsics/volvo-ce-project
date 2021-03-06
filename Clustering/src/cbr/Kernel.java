package cbr;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import mtree.MTree;

import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

public class Kernel implements KernelIF {
	
	private final String[] attributeNames;
	
	private final NaiveBayesClassifier classifier;
	
	private final DoublePointMTree tree;
	
	// Create more constructors, e.g., initialize from URL?!
	
	/**
	 *
	 * @param dataSet	The file containing the data points.
	 */
	public Kernel(File dataSet) {
		// TODO implement
		throw new RuntimeException("not implemented yet");
	}
	
	public Kernel() {
		// TODO make real
		attributeNames = new String[]{"hello"};
		
		final Random RANDOM = new Random();
		final int SIZE = 100;
		Set<DoublePoint> dataPoints = new HashSet<>();
		for (int i = 0; i < SIZE; i++) {
			dataPoints.add(new DoublePoint(new double[]{RANDOM.nextDouble(), RANDOM.nextDouble()}));
		}
		
		// TODO how to configure these values
		double eps = 0.12;
		int minPts = 4;
		DBSCANClusterer<DoublePoint> dbScan = new DBSCANClusterer<>(eps, minPts);
		
		classifier = new NaiveBayesClassifier(dbScan, dataPoints);
		
		tree = new DoublePointMTree();
	}
	
	@Override
	public String[] attributeNames() {
		return attributeNames;
	}

	@Override
	public double[][] kNNQuery(int k, double... attributes) {
		DoublePoint queryData = new DoublePoint(attributes);
		
		MTree<DoublePoint>.Query query = tree.getNearestByLimit(queryData, k);
		List<DoublePoint> result = new ArrayList<>();
		for (MTree<DoublePoint>.ResultItem item : query) {
			result.add(item.data);
		}
		
		return result.toArray(new double[0][]);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
