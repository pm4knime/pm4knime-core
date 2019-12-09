package org.pm4knime.node.io.log.reader;

import java.awt.Component;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.FilesHistoryPanel;
/**
 * Here to read a file XES file, two options are in need, one is the reading method,
 * another one is the reading type. We can get the extension automatically, but when it is shown here, I'd like to
 * have it as one feature. 
 * @author kefang-pads
 *
 */
public class XLogReaderNodeDialog extends DefaultNodeSettingsPane {
	SettingsModelString m_format, m_method, m_fileName; //
	
	public XLogReaderNodeDialog() {
		m_format  = XLogReaderNodeModel.createFormatModel();
		m_method = XLogReaderNodeModel.createMethodModel();
		m_fileName = XLogReaderNodeModel.createFileNameModel();
		this.setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentStringSelection(m_format,"Read Type", XLogReaderNodeModel.CFG_TYPES));
		addDialogComponent(new DialogComponentStringSelection(m_method,"Read Method", XLogReaderNodeModel.CFG_METHODS));
		this.setHorizontalPlacement(false);
		
		DialogComponentFileChooser fileComp = new DialogComponentFileChooser(m_fileName, "File Name", m_format.getStringValue());
		
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
		
		// add action listener for the component
		m_format.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO the value chosen affect the available extension shown in the m_fileName there..
				filePanel.setSuffixes(m_format.getStringValue());
			}
			
		});
	}

}