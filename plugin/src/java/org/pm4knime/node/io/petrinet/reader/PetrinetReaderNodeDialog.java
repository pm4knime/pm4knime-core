package org.pm4knime.node.io.petrinet.reader;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "PetrinetReader" Node.
 * read Petri net from pnml file
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author KFDing
 */
public class PetrinetReaderNodeDialog extends DefaultNodeSettingsPane {

	private final SettingsModelString m_fileName ;
	private final SettingsModelString m_type;
	
    protected PetrinetReaderNodeDialog() {
        m_fileName =  new SettingsModelString(PetrinetReaderNodeModel.CFG_FILE_NAME, "");
        
		DialogComponentFileChooser fileChooser = new DialogComponentFileChooser(m_fileName, PetrinetReaderNodeModel.CFG_HISTORY_ID,
        		JFileChooser.OPEN_DIALOG, false, createFlowVariableModel(m_fileName), ".pnml");
        fileChooser.setBorderTitle("Input Location");
        addDialogComponent(fileChooser);  
        String[] defaultValue =  PetrinetReaderNodeModel.defaultValue;
        m_type = new SettingsModelString(PetrinetReaderNodeModel.GFG_PETRINET_TYPE, defaultValue[0]);
        addDialogComponent(new DialogComponentStringSelection(m_type, "Select Import Petri net Type", defaultValue));
        
    }
}

