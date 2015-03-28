package cz.slahora.compling.gui.analysis.denotation;

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.swingViewer.LayerRenderer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePosition;

class ForeLayoutRenderer implements LayerRenderer {

	private final ConnectedComponents connectedComponents;
	private final int nodeSizePx;
	private final float fontSizePx;

	public ForeLayoutRenderer(ConnectedComponents connectedComponents, int nodeSizePx, int fontSizePx) {
		this.connectedComponents = connectedComponents;
		this.nodeSizePx = nodeSizePx;
		this.fontSizePx = fontSizePx;
	}

	@Override
	public void render(Graphics2D graphics2D, GraphicGraph graphicGraph, double px2Gu,
	                   int widthPx, int heightPx, double minXGu, double minYGu,
	                   double maxXGu, double maxYGu) {

		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Font font = graphics2D.getFont().deriveFont(fontSizePx);
		graphics2D.setFont(font);

		double width = px2Gu * (Math.abs(minXGu) + Math.abs(maxXGu));
		double height = px2Gu * (Math.abs(minYGu) + Math.abs(maxYGu));

		double leftEdge = 0.5 * (widthPx - width);
		double horizontalCenter = leftEdge + (px2Gu * Math.abs(minXGu));

		double topEdge = 0.5 * (heightPx - height);
		double verticalCenter = topEdge + (px2Gu * Math.abs(maxYGu));

		double nodeSizeGu = nodeSizePx / px2Gu;
		for (ConnectedComponents.ConnectedComponent component : connectedComponents) {
			Rectangle2D.Double bounds = findComponentsBounds(component, nodeSizeGu);

			String componentName = "K" + (component.id + 1);

			double nameWidthGu = graphics2D.getFontMetrics().stringWidth(componentName) / px2Gu;

			double xGu = bounds.x + ((bounds.width - nameWidthGu) / 2);

			int x = (int) Math.round(xGu * px2Gu + horizontalCenter);
			if (x > horizontalCenter) {
			 	x -= nodeSizePx;
			} else {
				x += nodeSizePx;
			}
			int y = (int) (verticalCenter - (bounds.y * px2Gu));
			if (bounds.y > 0) {
				y = (int) (y + fontSizePx / 4);
			} else {
				double pxSize = bounds.height * px2Gu;
				y = (int) (y - fontSizePx - (fontSizePx / 4));
			}
			graphics2D.drawString(componentName, x, y);
		}

	}

	private Rectangle2D.Double findComponentsBounds(ConnectedComponents.ConnectedComponent component, double nodeSizeGu) {

		Node extremeNode = null;
		double y = Double.NaN;
		boolean toTop = true;
		for (Node node : component) {
			if (extremeNode == null) {
				extremeNode = node;
				y = nodePosition(node)[1];
				toTop = y > 0;
			} else {
				double pos[] = nodePosition(node);
				if (toTop && pos[1] > y) {
					extremeNode = node;
					y = pos[1];
				} else if (!toTop && pos[1] < y) {
					extremeNode = node;
					y = pos[1];
				}
			}
		}

		double pos[] = nodePosition(extremeNode);

		double left = pos[0] - (0.5 * nodeSizeGu);
		double top = pos[1] - (0.5 * nodeSizeGu);

		return new Rectangle2D.Double(left, top, nodeSizeGu, nodeSizeGu);
	}
}
