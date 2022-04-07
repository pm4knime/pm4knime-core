package org.pm4knime.node.discovery.dfgminer.dfgTableMiner.helper;

import javax.swing.JComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;

public class DFMPortObjectSpec2 extends AbstractSimplePortObjectSpec {

	@Override
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void save(ModelContentWO model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(ModelContentRO model) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}
	
	public static final class DFMPortObjectSpec2Serializer extends AbstractSimplePortObjectSpecSerializer<DFMPortObjectSpec2> {
    }

}
