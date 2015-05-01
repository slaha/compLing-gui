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
	private final GuiDenotationHrebsModel hrebsModel;

	public GuiDenotationModel(WorkingText text) {
		this.poemModel = new GuiDenotationPoemModel(text);
		this.hrebsModel = new GuiDenotationHrebsModel(poemModel.getDenotation());
	}

	public GuiDenotationPoemModel getPoemModel() {
		return poemModel;
	}

	public GuiDenotationHrebsModel getHrebsModel() {
		return hrebsModel;
	}

	@Override
	public CsvSaver<GuiDenotationModel> getCsvSaver() {
		return new CsvSaver<GuiDenotationModel>() {
			@Override
			public CsvData saveToCsv(GuiDenotationModel object, Object... params) {
				CsvData poemCsvData = object.poemModel.getCsvSaver().saveToCsv(object.poemModel, params);
				CsvData hrebsCsvData = object.hrebsModel.getCsvSaver().saveToCsv(object.hrebsModel, params);

				return new CsvData(poemCsvData, hrebsCsvData);
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

				CsvData sectionHrebs = new CsvData(csv.getSection(1));
				objectToLoad.getHrebsModel().getCsvLoader().loadFromCsv(sectionHrebs, objectToLoad.getHrebsModel(), objectToLoad.getHrebsModel(), objectToLoad.getPoemModel());
			}
		};
	}
}
