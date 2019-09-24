package org.pm4knime.portobject;

import java.io.IOException;

import javax.swing.JComponent;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;
/**
 * we don't have things to write in this PortObjectSpec. For the XLog of Inform from the LogInfo,
 * if we check and make that they should be the same, then when we manipulate log, we have exception of the 
 * configure and execution error, so stay here, do nothing..
 * @author kefang-pads
 *
 */
public class XLogPortObjectSpec implements PortObjectSpec {

	public XLogPortObjectSpec() {}
	
	public void setSpec(XLogPortObjectSpec spec) {
		
	}
	
	
	@Override
	public JComponent[] getViews() {
		return new JComponent[] {};
	}

	public static class XLogPortObjectSpecSerializer
			extends PortObjectSpecSerializer<XLogPortObjectSpec> {

		@Override
		public void savePortObjectSpec(XLogPortObjectSpec portObjectSpec, PortObjectSpecZipOutputStream out)
				throws IOException {
			// nothing to store here
		}

		@Override
		public XLogPortObjectSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
			// TODO nothing to store and save here, only return new one for this
			return new XLogPortObjectSpec();
		}
	}

}
