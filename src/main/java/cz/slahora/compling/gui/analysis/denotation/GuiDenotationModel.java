package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
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
public class GuiDenotationModel implements Csv<GuiDenotationModel> {

	private final GuiDenotationPoemModel poemModel;
	private final GuiDenotationSpikesModel spikesModel;

	public GuiDenotationModel(WorkingText text) {
		this.poemModel = new GuiDenotationPoemModel(text);
		this.spikesModel = new GuiDenotationSpikesModel(poemModel.getDenotation());
	}

	public GuiDenotationPoemModel getPoemModel() {
		return poemModel;
	}

	public GuiDenotationSpikesModel getSpikesModel() {
		return spikesModel;
	}

	@Override
	public CsvSaver<GuiDenotationModel> getCsvSaver() {
		return new CsvSaver<GuiDenotationModel>() {
			@Override
			public CsvData saveToCsv(GuiDenotationModel object, Object... params) {
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

	public IDenotation getDenotation() {
		return poemModel.getDenotation();
	}

	@Override
	public CsvLoader<GuiDenotationModel> getCsvLoader() {
		return new CsvLoader<GuiDenotationModel>() {
			@Override
			public void loadFromCsv(CsvData csv, GuiDenotationModel objectToLoad, Object... params) throws CsvParserException {
				CsvData sectionPoem = new CsvData(csv.getSection(0));
				objectToLoad.getPoemModel().getCsvLoader().loadFromCsv(sectionPoem, objectToLoad.getPoemModel(), params);

				CsvData sectionSpikes = new CsvData(csv.getSection(1));
				objectToLoad.getSpikesModel().getCsvLoader().loadFromCsv(sectionSpikes, objectToLoad.getSpikesModel(), objectToLoad.getSpikesModel(), objectToLoad.getPoemModel());
			}
		};
	}
}
