package org.pm4knime.node.io.processtree.reader;

import javax.swing.JFileChooser;

import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.filehandling.core.data.location.variable.FSLocationVariableType;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.DialogComponentReaderFileChooser;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;


public class ProcessTreeReaderNodeDialog extends DefaultNodeSettingsPane {

	protected ProcessTreeReaderNodeDialog(PortsConfiguration portsConfiguration) {
	
		SettingsModelReaderFileChooser m_source = ProcessTreeReaderNodeModel.createSourceModel(portsConfiguration);
		this.setHorizontalPlacement(true);

		DialogComponentReaderFileChooser fileCompAdvanced = new DialogComponentReaderFileChooser(m_source,
				"copy-source",
				createFlowVariableModel(m_source.getKeysForFSLocation(), FSLocationVariableType.INSTANCE));
		
		addDialogComponent(fileCompAdvanced);


	}
}