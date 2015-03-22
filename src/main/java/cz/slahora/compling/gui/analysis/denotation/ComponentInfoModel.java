package cz.slahora.compling.gui.analysis.denotation;

import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentInfoModel implements Comparable<ComponentInfoModel> {

	private final int componentId;

	private final Map<Node, List<Distance>> distances;

	public ComponentInfoModel(Integer componentId) {
		this.componentId = componentId + 1;
		distances = new HashMap<Node, List<Distance>>();
	}

	int getEccentricity(Node node) {
		final List<Distance> distanceList = distances.get(node);
		if (distanceList == null) {
			throw new IllegalArgumentException("No distances for node " + node);
		}
		Distance eccentricity = distanceList.get(0);
		for (int i = 1; i < distanceList.size(); i++) {
			final Distance distance = distanceList.get(i);
			if (eccentricity.distance < distance.distance) {
				eccentricity = distance;
			}
		}
		return eccentricity.distance;
	}

	Node getComponentCenter() {
		Node center = null;
		int max = Integer.MAX_VALUE;
		for (Node node : distances.keySet()) {
			if (center == null) {
				center = node;
			} else {

				int current = getEccentricity(node);
				if (current < max){
					center = node;
					max = current;
				}
			}
		}
		return center;
	}


	int getComponentDiameter() {
		int diameter = -1;
		for (Node node : distances.keySet()) {
			int current = getEccentricity(node);
			if (current > diameter) {
				diameter = current;
			}
		}
		return diameter;
	}

	int getComponentDistancesSum() {
		int distanceSum = 0;
		for (List<Distance> distanceList : distances.values()) {
			for (Distance distance :distanceList){
				distanceSum += distance.distance;
			}
		}
		return distanceSum;
	}

	double computeCentralIndex(int distancesSum) {
		int nodeCount = nodeCount();
		return ((double) distancesSum) / ( ((double)nodeCount) * (nodeCount-1d));
	}

	double computeRelativeCentrality(int distancesSum) {
		int nodeCount = nodeCount();
		double nominator = (nodeCount + 1d) * nodeCount * (nodeCount - 1d);
		nominator = nominator - 3d * distancesSum;
		double denominator = nodeCount * (nodeCount -1d) * (nodeCount - 2d);
		return nominator / denominator;
	}

	int nodeCount() {
		return distances.size();
	}

	@Override
	public int compareTo(ComponentInfoModel o) {
		return componentId - o.componentId;
	}

	public void add(Node node, Node anotherNode, int distance) {
		add(node);
		distances.get(node).add(new Distance(anotherNode, distance));
	}

	public String getComponentName() {
		return "K" + componentId;
	}

	public void add(Node node) {
		List<Distance> distances = this.distances.get(node);
		if (distances == null) {
			distances = new ArrayList<Distance>();
			this.distances.put(node, distances);
		}
	}

	private class Distance {
		private final Node targetNode;
		private final int distance;

		public Distance(Node targetNode, int distance) {
			this.targetNode = targetNode;
			this.distance = distance;
		}
	}
}
