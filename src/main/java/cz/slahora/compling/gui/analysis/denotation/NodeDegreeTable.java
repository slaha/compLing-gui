package cz.slahora.compling.gui.analysis.denotation;

import org.apache.commons.lang.text.StrBuilder;
import org.graphstream.graph.Node;
import org.jdesktop.swingx.JXTable;

import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.List;

class NodeDegreeTable extends JXTable {

	private NodeDegreesModel nodeDegreesModel;

	public NodeDegreeTable() {
		setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
		setSortable(false);
	}

	public void setModel(NodeDegreesModel nodeDegreesModel) {
		this.nodeDegreesModel = nodeDegreesModel;
		setModel(new NodeDegreeTableModel());
	}

	private class NodeDegreeTableModel extends AbstractTableModel {

		private final int[] keys;

		public NodeDegreeTableModel() {
			keys = nodeDegreesModel.keys();
			Arrays.sort(keys);
		}

		@Override
		public int getRowCount() {
			return nodeDegreesModel.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
				case 0:
					return "Stupeň";
				case 1:
					return "Hřeby";
			}
			return null;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0:
					return keys[rowIndex];
				case 1:
					int key = keys[rowIndex];
					final List<Node> nodes = nodeDegreesModel.get(key);
					StrBuilder strBuilder = new StrBuilder();
					strBuilder.appendWithSeparators(nodes, ", ");
					return strBuilder.toString();
			}
			return null;
		}
	}
}
