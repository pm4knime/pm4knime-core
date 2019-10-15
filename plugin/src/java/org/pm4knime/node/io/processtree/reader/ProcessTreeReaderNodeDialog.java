package org.pm4knime.node.io.processtree.reader;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "ProcessTreeReader" Node.
 * this node is used to read process tree from file ptml * n
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author DKF
 */
public class ProcessTreeReaderNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the ProcessTreeReader node.
     */
    protected ProcessTreeReaderNodeDialog() {
    	SettingsModelString fileModel = createFileNameModel();
    	
    	DialogComponentFileChooser fileChooser = new DialogComponentFileChooser(fileModel, "ptml.reader.history",
        		JFileChooser.OPEN_DIALOG, false, createFlowVariableModel(fileModel), ".ptml");
    	fileChooser.setBorderTitle("Input Location");
        addDialogComponent(fileChooser);  
        
    }

	public static SettingsModelString createFileNameModel() {
		// TODO Auto-generated method stub
		return new SettingsModelString("Process Tree Reader", "");
	}
	
}

