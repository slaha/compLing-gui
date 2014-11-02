package cz.slahora.compling.gui.analysis.aggregation;

import cz.slahora.compling.gui.utils.HtmlLabelBuilder;

import javax.swing.table.AbstractTableModel;

class AggregationTableModel extends AbstractTableModel {
	private static final int ROW_COUNT = 4;

	private final AggregationModel model;

	public AggregationTableModel(AggregationModel model) {
		this.model = model;
	}

	@Override
	public int getRowCount() {
		return ROW_COUNT;
	}

	@Override
	public int getColumnCount() {
		return model.getAggregationShifts() + 1;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return String.class;
		}
		return Number.class;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return getRowName(rowIndex);
		} else {
			switch (rowIndex) {
				case 0:
				case 1:
					return columnIndex;
				case 2:
					return model.getAvgSimilarity(columnIndex);
				case 3:
					return model.getApproximatedSimilarity(columnIndex);
			}
		}

		return null;
	}

	private Object getRowName(int rowIndex) {
		switch (rowIndex) {
			case 0:
				return "i";
			case 1:
				return new HtmlLabelBuilder().text("L").sub("i").build().getText();
			case 2:
				return new HtmlLabelBuilder().text("S").sub("i").build().getText();
			case 3:
				return new HtmlLabelBuilder().text("Ŝ").sub("i").build().getText();
			default:
				throw new IllegalArgumentException("rowIndex >= ROW_COUNT → " + rowIndex + " >= " + ROW_COUNT);
		}
	}
}
