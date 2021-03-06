package cbr;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import mtree.*;
import mtree.utils.Pair;

public class DoublePointMTree extends MTree<DoublePoint> {
		
	private static final DistanceMeasure EUCLIDEAN = new EuclideanDistance(); 
	
	private static final DistanceFunction<DoublePoint> DISTANCE = new DistanceFunction<DoublePoint>() {
		
		@Override
		public double calculate(DoublePoint data1, DoublePoint data2) {
			return EUCLIDEAN.compute(data1.getPoint(), data2.getPoint());
		}
	};
	
	private static final PromotionFunction<DoublePoint> DETERMINISTIC = new PromotionFunction<DoublePoint>() {
		@Override
		public Pair<DoublePoint> process(Set<DoublePoint> dataSet, DistanceFunction<? super DoublePoint> distanceFunction) {
			return minMax(dataSet);
		}		
		
		int compareTo(DoublePoint d1, DoublePoint d2) {
			for(int i = 0; i < d1.getPoint().length; i++) {
				double v1 = d1.getPoint()[i];
				double v2 = d2.getPoint()[i];
				if(v1 > v2) {
					return +1;
				}
				if(v1 < v2) {
					return -1;
				}
			}		
			return 0;
		}
		
		Pair<DoublePoint> minMax(Iterable<DoublePoint> items) {
			Iterator<DoublePoint> iterator = items.iterator();
			if(!iterator.hasNext()) {
				return null;
			}
			
			DoublePoint min = iterator.next();
			DoublePoint max = min;
			
			while(iterator.hasNext()) {
				DoublePoint item = iterator.next();
				
				//double d1 = euclideanDistance.compute(item.getPoint(), min.getPoint());
				
				if(compareTo(item, min) < 0) {
					min = item;
				}
				if(compareTo(item, max) > 0) {
					max = item;
				}
			}
			
			return new Pair<DoublePoint>(min, max);
		}
	};
	
	public DoublePointMTree() {
		super(DISTANCE, new ComposedSplitFunction<DoublePoint>(DETERMINISTIC, new PartitionFunctions.BalancedPartition<DoublePoint>()));		
	}
}
