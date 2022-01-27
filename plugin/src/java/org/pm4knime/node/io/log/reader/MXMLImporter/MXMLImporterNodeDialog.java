package org.pm4knime.node.io.log.reader.MXMLImporter;

import java.awt.Component;

import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.FilesHistoryPanel;
import org.knime.filehandling.core.data.location.variable.FSLocationVariableType;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.DialogComponentReaderFileChooser;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;
import org.pm4knime.node.io.log.reader.XesImporter.XesImporterNodeModel;

/**
 * <code>NodeDialog</code> for the "MXMLImporter" node.
 * 
 * @author tbd
 */
public class MXMLImporterNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the MXMLImporter node.
	 */
	protected MXMLImporterNodeDialog(PortsConfiguration portsConfiguration) {

		SettingsModelReaderFileChooser m_source = MXMLImporterNodeModel.createSourceModel(portsConfiguration);
		SettingsModelString m_method = MXMLImporterNodeModel.createMethodModel();
		this.setHorizontalPlacement(true);
		addDialogComponent(
				new DialogComponentStringSelection(m_method, "Read Method", XesImporterNodeModel.getCFG_METHODS()));
		this.setHorizontalPlacement(false);
		DialogComponentReaderFileChooser fileCompAdvanced = new DialogComponentReaderFileChooser(m_source,
				"copy-source",
				createFlowVariableModel(m_source.getKeysForFSLocation(), FSLocationVariableType.INSTANCE));
		Component[] compArray = fileCompAdvanced.getComponentPanel().getComponents();
		Component tmp = null;

		for (Component c : compArray) {
			if (c instanceof FilesHistoryPanel) {
				tmp = c;
				break;
			}
		}
		final FilesHistoryPanel filePanel = (FilesHistoryPanel) tmp;
		addDialogComponent(fileCompAdvanced);

	}

}
