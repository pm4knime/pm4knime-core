package org.pm4knime.settingsmodel;

import java.util.EnumSet;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;

public class SettingsModelXLogImporter extends SettingsModel {

	public enum Importer {
		NAIVE, MAP_DB
	}

	private Importer importer;

	private final String configName;

	public SettingsModelXLogImporter(final String configName, final Importer defaultValue) {
		if ((configName == null) || "".equals(configName)) {
			throw new IllegalArgumentException("The configName must be a " + "non-empty string");
		}
		importer = defaultValue;
		this.configName = configName;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected SettingsModelXLogImporter createClone() {
		return new SettingsModelXLogImporter(configName, importer);
	}

	@Override
	protected String getModelTypeID() {
		return "SMID_XLOG_IMPORTER";
	}

	@Override
	protected String getConfigName() {
		return configName;
	}

	public void setImporter(final Importer importer) {
		boolean notify = !importer.equals(getImporter());
		this.importer = importer;
		if (notify) {
			notifyChangeListeners();
		}

	}

	public Importer getImporter() {
		return importer;
	}

	@Override
	protected void loadSettingsForDialog(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
		try {
			setImporterStr(settings.getString(configName, importer.toString()));
		} catch (IllegalArgumentException e) {
			// NOP settigsmodel remains the same
		} finally {
			notifyChangeListeners();
		}
	}

	public void setImporterStr(final String desc) {
		Importer selected = null;
		for (Importer importOption : EnumSet.allOf(Importer.class)) {
			if (importOption.toString().equals(desc)) {
				selected = importOption;
				break;
			}
		}
		setImporter(selected);
	}

	@Override
	protected void saveSettingsForDialog(NodeSettingsWO settings) throws InvalidSettingsException {
		saveSettingsForModel(settings);
	}

	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO see if there is a need to have some form of validation of a settings
		// read object
	}

	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		setImporterStr(settings.getString(configName));
		notifyChangeListeners();
	}

	@Override
	protected void saveSettingsForModel(NodeSettingsWO settings) {
		settings.addString(configName, getImporter().toString());

	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " ('" + configName + "')";
	}

}
