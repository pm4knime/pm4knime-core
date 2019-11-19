package org.pm4knime.settingsmodel;
/**
 * need to reconstrcut this class later to make it compatible to others
 */
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

public class SMXLogReaderParameter extends SettingsModel {

	public static String CFG_KEY_FILE_PATH = "cfg_xlog_reader_file_path";
	public static String CFG_DEFAULT_VALUE_FILE_PATH = System.getProperty("user.home");

	public static String CFG_KEY_IMPORTER = "cfg_xlog_reader_importer";
	public static SMXLogImporter.Importer CFG_DEFAULT_VALUE_IMPORTER = SMXLogImporter.Importer.NAIVE;

	private final SettingsModelString filePath = new SettingsModelString(CFG_KEY_FILE_PATH,
			CFG_DEFAULT_VALUE_FILE_PATH);
	private final SMXLogImporter importer = new SMXLogImporter(CFG_KEY_IMPORTER,
			CFG_DEFAULT_VALUE_IMPORTER);

	@SuppressWarnings("unchecked")
	@Override
	protected SMXLogReaderParameter createClone() {
		SMXLogReaderParameter clone = new SMXLogReaderParameter();
		clone.getFilePathSettingsModel().setStringValue(getFilePathSettingsModel().getStringValue());
		clone.getImporterSettingsModel().setImporter(getImporterSettingsModel().getImporter());
		return clone;
	}

	public SettingsModelString getFilePathSettingsModel() {
		return filePath;
	}

	public SMXLogImporter getImporterSettingsModel() {
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
