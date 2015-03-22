package cz.slahora.compling.gui.analysis.denotation;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.List;

public class NodeDegreesModel extends TIntObjectHashMap<List<Node>> {

	public void put(Node node) {
		final int degree = node.getDegree();
		List<Node> nodeList = get(degree);
		if (nodeList == null) {
			nodeList = new ArrayList<Node>();
			put(degree, nodeList);
		}
		nodeList.add(node);
	}
}
