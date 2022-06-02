package org.pm4knime.portobject;

import javax.swing.JComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;

public class DfgMsdPortObjectSpec extends AbstractSimplePortObjectSpec  {


	
	public static final class DfgMsdPortObjectSpecSerializer extends AbstractSimplePortObjectSpecSerializer<DfgMsdPortObjectSpec> {
    }

	public DfgMsdPortObjectSpec() {
		
	}
	
	@Override
	protected void save(ModelContentWO model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(ModelContentRO model) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
		return null;
	}


}
