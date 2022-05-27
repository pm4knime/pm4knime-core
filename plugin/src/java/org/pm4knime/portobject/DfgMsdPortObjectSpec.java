package org.pm4knime.portobject;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;
import org.knime.core.node.port.AbstractSimplePortObjectSpec.AbstractSimplePortObjectSpecSerializer;
import org.pm4knime.node.discovery.dfgminer.dfgTableMiner.helper.DFMPortObjectSpec2;

public class DfgMsdPortObjectSpec extends AbstractSimplePortObjectSpec  {

	@Override
	protected void save(ModelContentWO model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(ModelContentRO model) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}
	
	public static final class DfgMsdPortObjectSpecSerializer extends AbstractSimplePortObjectSpecSerializer<DfgMsdPortObjectSpec> {
    }

}
