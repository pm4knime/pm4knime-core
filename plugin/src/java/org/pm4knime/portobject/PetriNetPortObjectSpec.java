package org.pm4knime.portobject;


import java.io.IOException;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;
/**
 * when there is no need to serialize the Spec, we don't give it 
 * @author dkf
 *
 */
public class PetriNetPortObjectSpec implements PortObjectSpec   {
	
	private static final String ZIP_ENTRY_NAME = "PetriNetPortObjectSpec";
	
	public PetriNetPortObjectSpec() {}

	@Override
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
		return new JComponent[]{};
	}

	public static final class PetriNetPortObjectSpecSerializer extends PortObjectSpecSerializer<PetriNetPortObjectSpec> {

		@Override
		public void savePortObjectSpec(PetriNetPortObjectSpec portObjectSpec, PortObjectSpecZipOutputStream out)
				throws IOException {
			// TODO Auto-generated method stub
			out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
			out.close();
		}

		@Override
		public PetriNetPortObjectSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
			// TODO there is no need to store the Spec here..
			ZipEntry nextEntry = in.getNextEntry();
			if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_NAME)) {
				throw new IOException("Expected zip entry '" + ZIP_ENTRY_NAME + "' not present");
			}
			
			return new PetriNetPortObjectSpec();
		}
		
	}
	
	
}
