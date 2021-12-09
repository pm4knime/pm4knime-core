package org.pm4knime.node.io.log.reader.XesImporter;

import java.awt.Component;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.FilesHistoryPanel;

/**
 * This is an example implementation of the node dialog of the "XesImporter"
 * node.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}. In general, one can create an
 * arbitrary complex dialog using Java Swing.
 * 
 * @author tbd
 */
public class XesImporterNodeDialog extends DefaultNodeSettingsPane {

	// private Optional<SettingsModelString> m_method = Optional.ofNullable(null);
	// private Optional<SettingsModelString> m_fileName = Optional.ofNullable(null);
	/**
	 * New dialog pane for configuring the node. The dialog created here will show
	 * up when double clicking on a node in KNIME Analytics Platform.
	 */
	protected XesImporterNodeDialog() {
		final String format = "XES";
		SettingsModelString m_method = XesImporterNodeModel.createMethodModel();
		SettingsModelString m_fileName = XesImporterNodeModel.createFileNameModel();
		this.setHorizontalPlacement(true);
		addDialogComponent(
				new DialogComponentStringSelection(m_method, "Read Method", XesImporterNodeModel.getCFG_METHODS()));
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
