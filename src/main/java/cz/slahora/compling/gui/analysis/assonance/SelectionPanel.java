package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.CompLing;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ScrollablePanel;
import cz.slahora.compling.gui.panels.WrapLayout;
import cz.slahora.compling.gui.utils.IconUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

class SelectionPanel implements ActionListener {

	private static final String ACTION = "action";
	private static final String GROUP = "group";

	private ScrollablePanel left;
	private JPanel right;
	private static final GridBagConstraints RIGHT_PANEL_CONSTRAINTS = new GridBagConstraints();
	private static final GridBagConstraints LEFT_PANEL_BUTTON_CONSTRAINTS = new GridBagConstraints();
	static {
		RIGHT_PANEL_CONSTRAINTS.gridx = 0;
		RIGHT_PANEL_CONSTRAINTS.insets = new Insets(5, 0, 5, 0);
		RIGHT_PANEL_CONSTRAINTS.fill = GridBagConstraints.BOTH;
		RIGHT_PANEL_CONSTRAINTS.weightx = 1;
		RIGHT_PANEL_CONSTRAINTS.weighty = 1;
		RIGHT_PANEL_CONSTRAINTS.anchor = GridBagConstraints.PAGE_START;

		LEFT_PANEL_BUTTON_CONSTRAINTS.fill = GridBagConstraints.HORIZONTAL;
		LEFT_PANEL_BUTTON_CONSTRAINTS.weightx = 1;
		LEFT_PANEL_BUTTON_CONSTRAINTS.gridx =  0;
		LEFT_PANEL_BUTTON_CONSTRAINTS.anchor = GridBagConstraints.PAGE_START;
	}


	private final Component parent;
	private final Map<WorkingText, CompLing> texts;
	private final Selections selections;
	private JDialog dialog;
	private String lastSelectedGroup;


	SelectionPanel(Component parent, Map<WorkingText, CompLing> texts, Selections selections) {
		this.parent = parent;
		this.texts = texts;
		this.selections = selections;
	}

	int showSelectionsPanel( ) {

		left = new ScrollablePanel(new GridBagLayout());
		left.setScrollableWidth( ScrollablePanel.ScrollableSizeHint.FIT );
		left.setScrollableBlockIncrement(ScrollablePanel.VERTICAL, ScrollablePanel.IncrementType.PERCENT, 100);

		right = new JPanel(new GridBagLayout());

		JButton add = new JButton(IconUtils.getIcon(IconUtils.Icon.ADD));
		add.putClientProperty(ACTION, "add_new_group");
		add.addActionListener(this);

		java.util.List<Map.Entry<WorkingText, CompLing>> sortedTexts = new ArrayList<Map.Entry<WorkingText, CompLing>>(texts.entrySet());
		Collections.sort(sortedTexts, new Comparator<Map.Entry<WorkingText, CompLing>>() {
			@Override
			public int compare(Map.Entry<WorkingText, CompLing> o1, Map.Entry<WorkingText, CompLing> o2) {
				return o1.getKey().getName().compareTo(o2.getKey().getName());
			}
		});
		for (final Map.Entry<WorkingText, CompLing> e : sortedTexts) {
			final WorkingText workingText = e.getKey();
			JButton button = new JButton(workingText.getName());
			button.putClientProperty(ACTION, "add_to_group");
			button.putClientProperty("text", workingText);
			button.addActionListener(this);
			left.add(button, LEFT_PANEL_BUTTON_CONSTRAINTS);
		}


		JPanel leftContainer = new JPanel(new BorderLayout());
		final JScrollPane leftScrollPane = new JScrollPane(left);
		leftContainer.add(add, BorderLayout.NORTH);
		leftContainer.add(leftScrollPane, BorderLayout.CENTER);
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftContainer, new JScrollPane(right));
		splitPane.setPreferredSize(new Dimension(500, 500));
		splitPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		JPanel allPane = new JPanel(new GridBagLayout());

		GridBagConstraints cons1 = new GridBagConstraints();
		cons1.fill = GridBagConstraints.BOTH;
		cons1.weightx = cons1.weighty = 1;
		cons1.gridx = cons1.gridy = 0;
		allPane.add(splitPane, cons1);

		JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton ok = new JButton("OK");
		ok.putClientProperty(ACTION, "ok");
		ok.addActionListener(this);
		buttonsPane.add(ok);
		JButton cancel = new JButton("Cancel");
		cancel.putClientProperty(ACTION, "cancel");
		cancel.addActionListener(this);
		buttonsPane.add(cancel);
		GridBagConstraints cons2 = new GridBagConstraints();
		cons2.gridx = 0;
		cons2.gridy = 1;
		cons2.fill = GridBagConstraints.HORIZONTAL;
		cons1.weightx = 1;
		allPane.add(buttonsPane, cons2);

		dialog = new JDialog((Frame)null, true);
		dialog.setContentPane(allPane);
		dialog.setResizable(true);
		dialog.pack();
		dialog.setVisible(true);

		dialog.dispose();
		return allPane.getClientProperty("result") != null ? JOptionPane.OK_OPTION : JOptionPane.CANCEL_OPTION;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JComponent source = (JComponent) e.getSource();
		final String action = (String) source.getClientProperty(ACTION);

		if ("add_new_group".equals(action)) {

			final String name = JOptionPane.showInputDialog(parent, "Zadejte jméno nového výběru", "Jméno výběru", JOptionPane.QUESTION_MESSAGE);
			if (StringUtils.isNotBlank(name)) {
				selections.addNewName(name);
				lastSelectedGroup = name;

				JPanel panel = new JPanel(new WrapLayout(WrapLayout.LEFT));
				panel.setBorder(BorderFactory.createTitledBorder(name));
				right.putClientProperty(name, panel);
				right.add(panel, RIGHT_PANEL_CONSTRAINTS);
				right.revalidate();

				lastSelectedGroup = name;
			}

		} else if ("add_to_group".equals(action)) {

			WorkingText workingText = (WorkingText) source.getClientProperty("text");
			final String group = (String) JOptionPane.showInputDialog(parent, "Zvolte výběr pro " + workingText.getName(),  "Výběr pro " + workingText.getName(), JOptionPane.QUESTION_MESSAGE, null, selections.getAllNames(), lastSelectedGroup);
			if (group != null) {
				selections.addTo(group, workingText, workingText.getCompLing());
				left.remove(source);
				left.revalidate();
				left.repaint();

				JPanel targetPanel = (JPanel) right.getClientProperty(group);
				targetPanel.add(source);
				targetPanel.revalidate();
				targetPanel.repaint();

				source.putClientProperty(ACTION, "remove_from_group");
				source.putClientProperty(GROUP, group);

				lastSelectedGroup = group;
			}
		} else if ("remove_from_group".equals(action)) {

			WorkingText workingText = (WorkingText) source.getClientProperty("text");
			String group = (String) source.getClientProperty(GROUP);
			if (group == null) {
				return;
			}
			if (selections.removeFrom(group, workingText)) {
				left.add(source, LEFT_PANEL_BUTTON_CONSTRAINTS);
				left.revalidate();
				left.repaint();

				JPanel groupPanel = (JPanel) right.getClientProperty(group);
				groupPanel.remove(source);
				groupPanel.revalidate();
				groupPanel.repaint();

				source.putClientProperty(ACTION, "add_to_group");
				source.putClientProperty(GROUP, null);
			}

		} else if ("ok".equals(action)) {
			((JComponent)dialog.getContentPane()).putClientProperty("result", 1);
			dialog.setVisible(false);
		} else if ("cancel".equals(action)) {
			dialog.setVisible(false);
		}

	}
}
