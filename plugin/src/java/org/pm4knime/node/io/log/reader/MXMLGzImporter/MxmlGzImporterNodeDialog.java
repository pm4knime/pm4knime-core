package org.pm4knime.node.io.log.reader.MXMLGzImporter;

import java.awt.Component;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.FilesHistoryPanel;
import org.pm4knime.node.io.log.reader.XesGzImporter.XesGzImporterNodeModel;

/**
 * <code>NodeDialog</code> for the "MxmlGzImporter" node.
 * 
 * @author tbd
 */
public class MxmlGzImporterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the MxmlGzImporter node.
     */
    protected MxmlGzImporterNodeDialog() {
     	final String format = "MXML.GZ";
    	SettingsModelString m_method = MxmlGzImporterNodeModel.createMethodModel();
    	SettingsModelString m_fileName = MxmlGzImporterNodeModel.createFileNameModel();
    	this.setHorizontalPlacement(true);
    	addDialogComponent(new DialogComponentStringSelection(m_method,"Read Method",
    			XesGzImporterNodeModel.getCFG_METHODS()));
		this.setHorizontalPlacement(false);

		DialogComponentFileChooser fileComp = new DialogComponentFileChooser(m_fileName, 
												"File Name", format);
		Component[] compArray = fileComp.getComponentPanel().getComponents();
		Component tmp = null;
		for(Component c : compArray) {
			if(c instanceof FilesHistoryPanel) {
				tmp = c;
				break;
			}
		}
		final FilesHistoryPanel filePanel = (FilesHistoryPanel) tmp;
		addDialogComponent(fileComp);

    }
}

