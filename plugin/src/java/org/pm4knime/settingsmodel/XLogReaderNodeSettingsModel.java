package org.pm4knime.settingsmodel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

public class XLogReaderNodeSettingsModel extends SettingsModel {

	public static String CFG_KEY_FILE_PATH = "cfg_xlog_reader_file_path";
	public static String CFG_DEFAULT_VALUE_FILE_PATH = System.getProperty("user.home");

	public static String CFG_KEY_IMPORTER = "cfg_xlog_reader_importer";
	public static SettingsModelXLogImporter.Importer CFG_DEFAULT_VALUE_IMPORTER = SettingsModelXLogImporter.Importer.NAIVE;

	private final SettingsModelString filePath = new SettingsModelString(CFG_KEY_FILE_PATH,
			CFG_DEFAULT_VALUE_FILE_PATH);
	private final SettingsModelXLogImporter importer = new SettingsModelXLogImporter(CFG_KEY_IMPORTER,
			CFG_DEFAULT_VALUE_IMPORTER);

	@SuppressWarnings("unchecked")
	@Override
	protected XLogReaderNodeSettingsModel createClone() {
		XLogReaderNodeSettingsModel clone = new XLogReaderNodeSettingsModel();
		clone.getFilePathSettingsModel().setStringValue(getFilePathSettingsModel().getStringValue());
		clone.getImporterSettingsModel().setImporter(getImporterSettingsModel().getImporter());
		return clone;
	}

	public SettingsModelString getFilePathSettingsModel() {
		return filePath;
	}

	public SettingsModelXLogImporter getImporterSettingsModel() {
		return importer;
	}

	@Override
	protected String getModelTypeID() {
		return "SMID_XLOG_READER";
	}

	@Override
	protected String getConfigName() {
		return getModelTypeID() + "_" + CFG_KEY_FILE_PATH + "_" + CFG_KEY_IMPORTER;
	}

	@Override
	protected void loadSettingsForDialog(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
		try {
			loadSettingsForModel(settings);
		} catch (InvalidSettingsException e) {
			//
		} finally {
			notifyChangeListeners();
		}

	}

	@Override
	protected void saveSettingsForDialog(NodeSettingsWO settings) throws InvalidSettingsException {
		saveSettingsForModel(settings);

	}

	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
	}

	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		filePath.setStringValue(settings.getString(CFG_KEY_FILE_PATH, CFG_DEFAULT_VALUE_FILE_PATH));
		importer.setImporterStr(settings.getString(CFG_KEY_IMPORTER, CFG_DEFAULT_VALUE_IMPORTER.toString()));
		notifyChangeListeners();
		
	}

	@Override
	protected void saveSettingsForModel(NodeSettingsWO settings) {
		settings.addString(CFG_KEY_FILE_PATH, filePath.getStringValue());
		settings.addString(CFG_KEY_IMPORTER, importer.getImporter().toString());
		notifyChangeListeners();

	}

	@Override
	public String toString() {
		return "NOT YET IMPLEMENTED";
	}

}
