package cz.slahora.compling.gui.utils;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 *
 * Builder for generating {@code GridBagConstraints}
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 25.3.14 20:49</dd>
 * </dl>
 */
public class GridBagConstraintBuilder {

	private final GridBagConstraints constraint;

	public GridBagConstraintBuilder() {
		constraint = new GridBagConstraints();
	}

	private GridBagConstraintBuilder(GridBagConstraintBuilder _constraint) {
		constraint = (GridBagConstraints) _constraint.constraint.clone();
	}


	public GridBagConstraintBuilder fill(int fillingMode) {
		constraint.fill = fillingMode;
		return this;
	}

	public GridBagConstraintBuilder anchor(int lineStart) {
		constraint.anchor = lineStart;
		return this;
	}

	public GridBagConstraintBuilder gridXY(int x, int y) {
		return gridX(x).gridY(y);
	}
	public GridBagConstraintBuilder gridX(int i) {
		constraint.gridx = i;
		return this;
	}

	public GridBagConstraintBuilder gridY(int i) {
		constraint.gridy = i;
		return this;
	}

	public GridBagConstraintBuilder weightX(int i) {
		constraint.weightx = i;
		return this;
	}

	public GridBagConstraintBuilder weightY(int i) {
		constraint.weighty = i;
		return this;
	}

	public GridBagConstraintBuilder insets(Insets insets) {
		constraint.insets = insets;
		return this;
	}

	public GridBagConstraints build() {
		return constraint;
	}

	public GridBagConstraintBuilder copy() {
		return new GridBagConstraintBuilder(this);
	}

	public GridBagConstraintBuilder gridWidth(int i) {
		constraint.gridwidth = i;
		return this;
	}
}
