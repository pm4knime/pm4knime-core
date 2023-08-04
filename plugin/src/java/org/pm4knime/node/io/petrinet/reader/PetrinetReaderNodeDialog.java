package org.pm4knime.node.io.petrinet.reader;

import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.filehandling.core.data.location.variable.FSLocationVariableType;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.DialogComponentReaderFileChooser;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;


public class PetrinetReaderNodeDialog extends DefaultNodeSettingsPane {

	protected PetrinetReaderNodeDialog(PortsConfiguration portsConfiguration) {
	
		SettingsModelReaderFileChooser m_source = PetrinetReaderNodeModel.createSourceModel(portsConfiguration);
		this.setHorizontalPlacement(true);

		DialogComponentReaderFileChooser fileCompAdvanced = new DialogComponentReaderFileChooser(m_source,
				"copy-source",
				createFlowVariableModel(m_source.getKeysForFSLocation(), FSLocationVariableType.INSTANCE));
		
		addDialogComponent(fileCompAdvanced);

	}
}
