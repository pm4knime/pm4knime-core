package org.pm4knime.node.io.processtree.writer;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "ProcessTreeWriter" Node.
 * Export process tree into file
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author DKF
 */
public class ProcessTreeWriterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the ProcessTreeWriter node. It has the similar structure like the reading part
     */
    protected ProcessTreeWriterNodeDialog() {
    	SettingsModelString fileModel = createFileNameModel();
    	
    	DialogComponentFileChooser fileChooser = new DialogComponentFileChooser(fileModel, "ptml.writer.history",
        		JFileChooser.SAVE_DIALOG, false, createFlowVariableModel(fileModel), ".ptml");
    	fileChooser.setBorderTitle("Export Location");
    	fileChooser.setDialogTypeSaveWithExtension(".ptml");
    	
        addDialogComponent(fileChooser);  
        
    }

	public static SettingsModelString createFileNameModel() {
		// TODO Auto-generated method stub
		return new SettingsModelString("Process Tree Writer", "");
	}
    
}

