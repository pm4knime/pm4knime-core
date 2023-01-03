package org.pm4knime.node.io.hybridpetrinet.reader;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "HybridPetrinetReader" Node. read HybridPetri net from
 * pnml file
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author KFDing
 */
public class HybridPetrinetReaderNodeDialog extends DefaultNodeSettingsPane {

	private final SettingsModelString m_fileName;
	private final SettingsModelString m_type;

	protected HybridPetrinetReaderNodeDialog() {
		m_fileName = new SettingsModelString(HybridPetrinetReaderNodeModel.CFG_FILE_NAME, "");

		DialogComponentFileChooser fileChooser = new DialogComponentFileChooser(m_fileName,
				HybridPetrinetReaderNodeModel.CFG_HISTORY_ID, JFileChooser.OPEN_DIALOG, false,
				createFlowVariableModel(m_fileName), ".pnml");
		fileChooser.setBorderTitle("Input Location");
		addDialogComponent(fileChooser);
		String[] defaultValue = HybridPetrinetReaderNodeModel.defaultTypes;
		m_type = new SettingsModelString(HybridPetrinetReaderNodeModel.GFG_PETRINET_TYPE, defaultValue[0]);
		addDialogComponent(new DialogComponentStringSelection(m_type, "Select Import Petri net Type", defaultValue));

	}
}


/*
 * /**
 * <code>NodeDialog</code> for the "HybridPetrinetReader" Node. read HybridPetri net from
 * pnml file
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author KFDing
 
public class HybridPetrinetReaderNodeDialog extends DefaultNodeSettingsPane {

	private final SettingsModelString m_fileName;
	private final SettingsModelString m_type;

	protected HybridPetrinetReaderNodeDialog() {
		m_fileName = new SettingsModelString(PetrinetReaderNodeModel.CFG_FILE_NAME, "");

		DialogComponentFileChooser fileChooser = new DialogComponentFileChooser(m_fileName,
				PetrinetReaderNodeModel.CFG_HISTORY_ID, JFileChooser.OPEN_DIALOG, false,
				createFlowVariableModel(m_fileName), ".pnml");
		fileChooser.setBorderTitle("Input Location");
		addDialogComponent(fileChooser);
		String[] defaultValue = PetrinetReaderNodeModel.defaultTypes;
		m_type = new SettingsModelString(PetrinetReaderNodeModel.GFG_PETRINET_TYPE, defaultValue[0]);
		addDialogComponent(new DialogComponentStringSelection(m_type, "Select Import Petri net Type", defaultValue));

	}
}

 * 
 * 
 * */
 