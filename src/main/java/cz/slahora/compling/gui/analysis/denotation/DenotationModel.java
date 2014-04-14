package cz.slahora.compling.gui.analysis.denotation;

import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;

import java.text.ParseException;

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
public class DenotationModel implements Csv<DenotationModel> {

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

	@Override
	public CsvSaver<DenotationModel> getCsvSaver() {
		return new CsvSaver<DenotationModel>() {
			@Override
			public CsvData saveToCsv(DenotationModel object, Object... params) {
				CsvData poemCsvData = object.poemModel.getCsvSaver().saveToCsv(object.poemModel, params);
				CsvData spikesCsvData = object.spikesModel.getCsvSaver().saveToCsv(object.spikesModel, params);

				return new CsvData(poemCsvData, spikesCsvData);
			}
		};
	}

	@Override
	public boolean supportsCsvImport() {
		return true;
	}

	@Override
	public CsvLoader<DenotationModel> getCsvLoader() {
		return new CsvLoader<DenotationModel>() {
			@Override
			public void loadFromCsv(CsvData csv, DenotationModel objectToLoad, Object... params) throws ParseException {
				CsvData sectionPoem = new CsvData(csv.getSection(0));
				objectToLoad.getPoemModel().getCsvLoader().loadFromCsv(sectionPoem, objectToLoad.getPoemModel(), params);

				CsvData sectionSpikes = new CsvData(csv.getSection(1));
				objectToLoad.getSpikesModel().getCsvLoader().loadFromCsv(sectionSpikes, objectToLoad.getSpikesModel(), objectToLoad.getSpikesModel(), objectToLoad.getPoemModel());
			}
		};
	}
}
