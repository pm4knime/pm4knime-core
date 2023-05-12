package org.pm4knime.portobject;

import java.io.IOException;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

public class ProcessTreePortObjectSpec implements PortObjectSpec {

	// only one method to implement, but why?? for this spec?? 
	
	// private static final String CFG_FILE_NAME = "ProcessTreePortObjectSpec";
	private static final String ZIP_ENTRY_NAME = "ProcessTreePortObjectSpec";
	private String fileName = "ProcessTreeSpec.txt";
	
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
		return new JComponent[]{};
	}

	public String getFileName() {
		// TODO Auto-generated method stub
		return fileName;
	}
	public void setFileName(String name) {
		fileName = name;
	}
	
	
	
	public static final class ProcessTreePortObjectSpecSerializer extends PortObjectSpecSerializer<ProcessTreePortObjectSpec> {

		@Override
		public void savePortObjectSpec(ProcessTreePortObjectSpec portObjectSpec, PortObjectSpecZipOutputStream out)
				throws IOException {
			// TODO we just write the spec object into some file, we can't really store the context
			// for each one, we need to create a new
//			out.putNextEntry(new ZipEntry(fileName));
//			out.write(portObjectSpec.getFileName().getBytes());
			out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
			out.close();
			
		}

		@Override
		public ProcessTreePortObjectSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
			// load spec from some file.. but first we need to differ the context
//			String entryName = in.getNextEntry().getName();
//			
//			if (!entryName.equals(fileName)) {
//	            throw new IOException("Found unexpected zip entry "
//	                    + entryName + "! Expected " + fileName);
//	        }
//			
//			
//			byte[] buffer = new byte[512];
//			
//			ProcessTreePortObjectSpec spec = null;
//			try {
//				spec = new ProcessTreePortObjectSpec();
//				
//				in.read(buffer);
//				// now we need to transfer bytes into string
//				String tmpName = new String(buffer);
//				spec.setFileName(tmpName);
//			}catch (Exception e) {
//	            throw new IOException(e);
//	        }
//			return spec;
			ZipEntry nextEntry = in.getNextEntry();
			if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_NAME)) {
				throw new IOException("Expected zip entry '" + ZIP_ENTRY_NAME + "' not present");
			}
			
			return new ProcessTreePortObjectSpec();
		}



	}


}
