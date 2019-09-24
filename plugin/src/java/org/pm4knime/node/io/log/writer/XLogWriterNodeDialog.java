package org.pm4knime.node.io.log.writer;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "XLogWriter" Node.
 * It exports the event log into files, which seperates the codes from the reading part
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Kefang
 */
public class XLogWriterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the XLogWriter node.
     */
	private final SettingsModelString m_format = XLogWriterNodeModel.createFormatModel();
	private final SettingsModelString m_outfile = XLogWriterNodeModel.createFileNameModel();
	
    protected XLogWriterNodeDialog() {
    	
    	
    	addDialogComponent(new DialogComponentStringSelection(m_format, "Select Export Type", XLogWriterNodeModel.formatTypes));
    	
    	DialogComponentFileChooser fileChooser = new DialogComponentFileChooser(m_outfile, "log.xes.writer.history",
        		JFileChooser.SAVE_DIALOG, false, createFlowVariableModel(m_outfile));
    	fileChooser.setBorderTitle("Export Location");
    	// fileChooser.setDialogTypeSaveWithExtension(m_format.getStringValue());
        addDialogComponent(fileChooser); 
    }
    
    private String convertType2Extension(String format) {
    	switch(format) {
    	
    	case "XES":
    		return ".xes";
    	case "XES_GZ":
    		return ".xes.gz";
    	case "MXML":
    		return ".mxml";
    	case "MXML_GZ":
    		return ".mxml.gz";
    	}
    	
    	return null;
    }
}

