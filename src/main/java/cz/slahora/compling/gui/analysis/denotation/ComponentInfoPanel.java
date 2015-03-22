package cz.slahora.compling.gui.analysis.denotation;

import com.jidesoft.swing.MultilineLabel;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;

public class ComponentInfoPanel extends JPanel {

	public ComponentInfoPanel(ComponentInfoModel componentsModel) {
		setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		setBackground(Color.white);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		final String name = componentsModel.getComponentName();
		final JLabel headline = new HtmlLabelBuilder().hx(2, "Komponenta " + name).build();
		add(headline, new GridBagConstraintBuilder().gridXY(0, 0).weightX(1).fill(GridBagConstraints.HORIZONTAL).build());

		String message = "Komponenta " + name + " obsahuje " + getVertexCountDesc(componentsModel.nodeCount()) + '.';
		final MultilineLabel label = new MultilineLabel();
		add(label);

		if (componentsModel.nodeCount() > 1) {

			message += " Diametrem komponenty d(" + name + ") = " + componentsModel.getComponentDiameter() + ". Centrem komponenty je hřeb " +
				componentsModel.getComponentCenter() + '.';

			final int componentDistancesSum = componentsModel.getComponentDistancesSum();
			add(new JLabel("Součet všech vzdáleností z(" + name + ")=" + componentDistancesSum));
			add(new JLabel("Centrální index (průměrná vzdálenost) d=" + componentsModel.computeCentralIndex(componentDistancesSum)));
			add(new JLabel("Relativní míra centrality Z(" + name + ")=" + componentsModel.computeRelativeCentrality(componentDistancesSum)));
		}
		label.setText(message);
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

	public Component add(JComponent comp) {
		comp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		return super.add(comp);
	}
}
