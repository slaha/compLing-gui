package cz.slahora.compling.gui.analysis.denotation;

import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class VertexInfoPanel extends JPanel{

	public VertexInfoPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	public void onGraphChanged(Graph graph, ConnectedComponents connectedComponents, double alpha) {
		List<ComponentInfoModel> componentModels = new ArrayList<ComponentInfoModel>(connectedComponents.getConnectedComponentsCount());
		for (ConnectedComponents.ConnectedComponent connectedComponent : connectedComponents) {
			final ComponentInfoModel componentInfo = new ComponentInfoModel(connectedComponent.id);
			componentModels.add(componentInfo);

			for (Node node : connectedComponent) {
				componentInfo.add(node);
				String name = node.getId();
				APSP.APSPInfo info = graph.getNode(name).getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
				for (Node anotherNode : connectedComponent) {
					if (node == anotherNode) {
						continue;
					}
					String anotherName = anotherNode.getId();
					final int distance = info.getShortestPathTo(anotherName).size() - 1;
					componentInfo.add(node, anotherNode, distance);
				}
			}
		}
		Collections.sort(componentModels);
		removeAll();
		for (ComponentInfoModel componentsModel : componentModels) {
			add(new ComponentInfoPanel(componentsModel));
		}
	}
}
