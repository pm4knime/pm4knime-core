package org.pm4knime.node.io.petrinet.reader;

import java.awt.Component;

import javax.swing.JFileChooser;

import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.FilesHistoryPanel;
import org.knime.filehandling.core.data.location.variable.FSLocationVariableType;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.DialogComponentReaderFileChooser;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;


/**
 * <code>NodeDialog</code> for the "PetrinetReader" Node. read Petri net from
 * pnml file
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author KFDing
 */
public class PetrinetReaderNodeDialog extends DefaultNodeSettingsPane {

	protected PetrinetReaderNodeDialog(PortsConfiguration portsConfiguration) {
	
		SettingsModelReaderFileChooser m_source = PetrinetReaderNodeModel.createSourceModel(portsConfiguration);
		this.setHorizontalPlacement(true);

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
