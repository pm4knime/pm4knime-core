package org.pm4knime.portobject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;
import org.pm4knime.settingsmodel.SMAlignmentReplayParameter;

/**
 * how to set the spec to its Portobject there?
 * @author kefang-pads
 *
 */
public class RepResultPortObjectSpec implements PortObjectSpec {

	private static final String ZIP_ENTRY_NAME = "RepResultPortObjectSpec";
	
	// add m_parameter here 
	SMAlignmentReplayParameter m_parameter;
	
	public RepResultPortObjectSpec() {}
	public RepResultPortObjectSpec(SMAlignmentReplayParameter parameter) {
		m_parameter = parameter;
	}
	
	@Override 
	public JComponent[] getViews() {
		// TODO do it later when implementing the whole result
		// for spec, currently, no view, but later should generate a table for it to show the parameter
		return null;
	}

	public SMAlignmentReplayParameter getMParameter() {
		// TODO Auto-generated method stub
		return m_parameter;
	}
	public void setMParameter(SMAlignmentReplayParameter parameter) {
		// TODO Auto-generated method stub
		m_parameter = parameter;
	}

	
	
	public static final class RepResultPortObjectSpecSerializer extends PortObjectSpecSerializer<RepResultPortObjectSpec> {

		@Override
		public void savePortObjectSpec(RepResultPortObjectSpec portObjectSpec, PortObjectSpecZipOutputStream out)
				throws IOException {
			// TODO Auto-generated method stub
			out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
			// how to save m_parameter with out, but not the settings?? 
			
			SMAlignmentReplayParameter m_parameter = portObjectSpec.getMParameter();
			NodeSettings tmp = new NodeSettings(ZIP_ENTRY_NAME + ":"+ m_parameter.getConfigName());
			m_parameter.saveSettingsTo(tmp);
			
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			objOut.writeObject(tmp);
			
		}

		@Override
		public RepResultPortObjectSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
			// TODO there is no need to store the Spec here..
			ZipEntry nextEntry = in.getNextEntry();
			if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_NAME)) {
				throw new IOException("Expected zip entry '" + ZIP_ENTRY_NAME + "' not present");
			}
			
			ObjectInputStream objIn = new ObjectInputStream(in);
			try {
				NodeSettings tmp = (NodeSettings) objIn.readObject();
				String configName = tmp.getKey().split(":")[1];
				// how to get the config name and then use it to load the values??
				SMAlignmentReplayParameter m_parameter = new SMAlignmentReplayParameter(configName);
				m_parameter.loadSettingsFrom(tmp);
				return new RepResultPortObjectSpec(m_parameter);
			} catch (ClassNotFoundException | InvalidSettingsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return new RepResultPortObjectSpec();
		}
		
	}

	
	
}
