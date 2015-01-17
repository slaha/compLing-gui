package cz.slahora.compling.gui.analysis;

import org.apache.commons.lang3.StringUtils;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
*
* TODO
*
* <dl>
* <dt>Created by:</dt>
* <dd>slaha</dd>
* <dt>On:</dt>
* <dd> 24.8.14 9:45</dd>
* </dl>
*/
public class RulesTable {

	public enum RuleType {
		IGNORE, AS_ONE_CHAR, REPLACE
	}

	public static class RulesTableModel<T> extends AbstractTableModel {

		private final List<RuleHolder<T>> replaceRules;
		private static final String[] COLUMN_NAMES = {"Znaky k nahrazení", "Nahradit za"};

		public RulesTableModel(List<RuleHolder<T>> replaceRules) {
			this.replaceRules = replaceRules;

		}

		@Override
		public int getRowCount() {
			return replaceRules.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return COLUMN_NAMES[columnIndex];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			RuleHolder ruleHolder = replaceRules.get(rowIndex);
			if (columnIndex == 0) {
				String c = ruleHolder.whatToFind;
				if (StringUtils.isBlank(c)) {
					c = '\'' + c + '\'';
				}
				return c;
			}
			return ruleHolder.replaceWith;
		}
	}

	public static class RuleHolder<T> {
		final T rule;
		final String whatToFind;
		final String replaceWith;

		public RuleHolder(T rule, String whatToFind, String replaceWith) {
			this.rule = rule;
			this.whatToFind = whatToFind;
			this.replaceWith = replaceWith;
		}

		public T getRule() {
			return rule;
		}
	}


}
