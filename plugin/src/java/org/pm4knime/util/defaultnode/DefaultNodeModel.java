package org.pm4knime.util.defaultnode;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortType;
import org.processmining.framework.plugin.PluginContext;

/**
 * this class is used to implement the cancellation from ExecutionContext to Prom Cancellation
 * @author kefang-pads
 *
 */
public class DefaultNodeModel extends NodeModel {
	
	protected DefaultNodeModel(PortType[] inPortTypes, PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
	}

	public void checkCanceled(final ExecutionContext exec) throws CanceledExecutionException {
		checkCanceled(null, exec);
	}
	
	public void checkCanceled(PluginContext pluginContext, final ExecutionContext exec) throws CanceledExecutionException {
		try {
			exec.checkCanceled();
		}catch (final CanceledExecutionException ce) {
			if(pluginContext != null)
				pluginContext.getProgress().cancel();
			throw ce;
		}
	}
	
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub
		
	}

}
