package org.pm4knime.node.io.log.reader.XesGzImporter;

import java.awt.Component;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.FilesHistoryPanel;

/**
 * <code>NodeDialog</code> for the "XesGezImporter" node.
 * 
 * @author tbd
 */
public class XesGzImporterNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the XesGezImporter node.
	 */
	protected XesGzImporterNodeDialog() {

		final String format = "XES.GZ";
		SettingsModelString m_method = XesGzImporterNodeModel.createMethodModel();
		SettingsModelString m_fileName = XesGzImporterNodeModel.createFileNameModel();
		this.setHorizontalPlacement(true);
		addDialogComponent(
				new DialogComponentStringSelection(m_method, "Read Method", XesGzImporterNodeModel.getCFG_METHODS()));
		this.setHorizontalPlacement(false);

		DialogComponentFileChooser fileComp = new DialogComponentFileChooser(m_fileName, "File Name", format);
		Component[] compArray = fileComp.getComponentPanel().getComponents();
		Component tmp = null;
		for (Component c : compArray) {
			if (c instanceof FilesHistoryPanel) {
				tmp = c;
				break;
			}
		}
		final FilesHistoryPanel filePanel = (FilesHistoryPanel) tmp;
		addDialogComponent(fileComp);

	}
}
