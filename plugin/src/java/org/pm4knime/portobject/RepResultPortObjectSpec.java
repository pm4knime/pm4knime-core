package org.pm4knime.portobject;

import java.io.IOException;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;
import org.knime.core.node.port.PortObjectSpec.PortObjectSpecSerializer;

public class RepResultPortObjectSpec implements PortObjectSpec {

	private static final String ZIP_ENTRY_NAME = "RepResultPortObjectSpec";
	
	public RepResultPortObjectSpec() {}
	
	@Override 
	public JComponent[] getViews() {
		// TODO do it later when implementing the whole result
		return null;
	}

	public static final class RepResultPortObjectSpecSerializer extends PortObjectSpecSerializer<RepResultPortObjectSpec> {

		@Override
		public void savePortObjectSpec(RepResultPortObjectSpec portObjectSpec, PortObjectSpecZipOutputStream out)
				throws IOException {
			// TODO Auto-generated method stub
			out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
			out.close();
		}

		@Override
		public RepResultPortObjectSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
			// TODO there is no need to store the Spec here..
			ZipEntry nextEntry = in.getNextEntry();
			if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_NAME)) {
				throw new IOException("Expected zip entry '" + ZIP_ENTRY_NAME + "' not present");
			}
			
			return new RepResultPortObjectSpec();
		}
		
	}
	
	
	
}
