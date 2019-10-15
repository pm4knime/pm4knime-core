package org.pm4knime.portobject;

import javax.swing.JComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;
import org.knime.core.node.port.PortObjectSpec;
import org.processmining.framework.plugin.PluginContext;

public class ProcessTreePortObjectSpec implements PortObjectSpec {

	// only one method to implement, but why?? for this spec?? 
	
	// private static final String CFG_FILE_NAME = "ProcessTreePortObjectSpec";
	private String fileName = "ProcessTreeObject.ptml";
	
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFileName() {
		// TODO Auto-generated method stub
		return fileName;
	}
	public void setFileName(String name) {
		fileName = name;
	}

}
