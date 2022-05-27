package org.pm4knime.portobject;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;
import org.knime.core.node.port.AbstractSimplePortObjectSpec.AbstractSimplePortObjectSpecSerializer;

public class EfficientTreePortObjectSpec extends AbstractSimplePortObjectSpec {
	@Override
	protected void save(ModelContentWO model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(ModelContentRO model) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}
	
	public static final class EfficientTreeObjectSpecSerializer extends AbstractSimplePortObjectSpecSerializer<EfficientTreePortObjectSpec> {
    }

}
