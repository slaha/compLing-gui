package cz.slahora.compling.gui.analysis.denotation;

import com.jidesoft.swing.MultilineLabel;
import cz.slahora.compling.gui.analysis.ToggleHeader;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.graphstream.graph.Node;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXTable;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;

public class ComponentInfoPanel extends JPanel {

	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.###");
	private final JXCollapsiblePane collapsiblePane;
	private int y;

	public ComponentInfoPanel(ComponentInfoModel componentsModel) {
		setBackground(Color.white);
		setLayout(new BorderLayout());

		final String name = componentsModel.getComponentName();
		final JLabel headline = new HtmlLabelBuilder().hx(2, "Komponenta " + name).build();

		collapsiblePane = new JXCollapsiblePane();
		collapsiblePane.setCollapsed(true);
		collapsiblePane.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
		collapsiblePane.setLayout(new GridBagLayout());

		ToggleHeader header = new ToggleHeader(collapsiblePane, headline.getText());

		String message = "Komponenta " + name + " obsahuje " + getVertexCountDesc(componentsModel.nodeCount()) + '.';
		final MultilineLabel label = new MultilineLabel();
		addToPanel(label);

		if (componentsModel.nodeCount() > 1) {

			message += " Diametr komponenty d(" + name + ") = " + componentsModel.getComponentDiameter() + ". Centrem komponenty je hřeb " +
				componentsModel.getComponentCenter() + '.';

			final int componentDistancesSum = componentsModel.getComponentDistancesSum();
			addToPanel(new JLabel("Součet všech vzdáleností z(" + name + ")=" + componentDistancesSum));
			addToPanel(new JLabel("Centrální index (průměrná vzdálenost) d=" + DECIMAL_FORMAT.format(componentsModel.computeCentralIndex(componentDistancesSum))));
			if (componentsModel.nodeCount() > 2) {
				addToPanel(new JLabel("Relativní míra centrality Z(" + name + ")=" + DECIMAL_FORMAT.format(componentsModel.computeRelativeCentrality(componentDistancesSum))));
			}

			JXTable nodesTable = new JXTable(new NodesTableModel(componentsModel));
			addToPanel(nodesTable.getTableHeader());
			addToPanel(nodesTable);
		}

		label.setText(message);

		add(header, BorderLayout.NORTH);
		add(collapsiblePane, BorderLayout.CENTER);
	}

	private String getVertexCountDesc(int nodeCount) {
		switch (nodeCount) {
			case 1:
				return "jeden vrchol";
			case 2:
			case 3:
			case 4:
				return nodeCount + " vrcholy";
			default:
				return nodeCount + " vrcholů";
		}
	}

	public void addToPanel(JComponent comp) {
		comp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		comp.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		collapsiblePane.add(comp, new GridBagConstraintBuilder().gridXY(0, y++).anchor(LINE_START).weightX(1).fill(HORIZONTAL).build());
	}

	private class NodesTableModel extends AbstractTableModel {

		private final ComponentInfoModel componentsModel;
		private final java.util.List<Node> nodes;

		public NodesTableModel(ComponentInfoModel componentsModel) {
			this.componentsModel = componentsModel;
			nodes = new ArrayList<Node>(componentsModel.getNodes());
		}

		@Override
		public int getRowCount() {
			return componentsModel.nodeCount();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
				case 0:
					return "Vrchol x";
				case 1:
					return "e(x)";
				case 2:
					return "Součet vzdáleností vrcholu x";
			}
			throw new IllegalArgumentException("unknown column " + column);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			final Node node = nodes.get(rowIndex);
			switch (columnIndex) {
				case 0:
					return node;
				case 1:
					return componentsModel.getEccentricity(node);
				case 2:
					return componentsModel.getNodeDistancesSum(node);
			}
			throw new IllegalArgumentException("unknown row " + rowIndex + " and column " + columnIndex);
		}
	}
}
