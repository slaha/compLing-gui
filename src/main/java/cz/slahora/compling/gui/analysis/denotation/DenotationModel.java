package cz.slahora.compling.gui.analysis.denotation;

import cz.slahora.compling.gui.model.WorkingText;

/**
 *
 * TODO
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd>6.4.14 13:39</dd>
 * </dl>
 */
public class DenotationModel  {

	private final DenotationPoemModel poemModel;
	private final DenotationSpikesModel spikesModel;

	public DenotationModel(WorkingText text) {
		this.poemModel = new DenotationPoemModel(text);
		this.spikesModel = new DenotationSpikesModel();
	}

	public DenotationPoemModel getPoemModel() {
		return poemModel;
	}

	public DenotationSpikesModel getSpikesModel() {
		return spikesModel;
	}
}
