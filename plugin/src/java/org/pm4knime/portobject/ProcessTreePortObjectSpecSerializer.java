package org.pm4knime.portobject;

import java.io.IOException;
import java.util.zip.ZipEntry;

import org.knime.core.node.port.PortObjectSpec.PortObjectSpecSerializer;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

public class ProcessTreePortObjectSpecSerializer extends PortObjectSpecSerializer<ProcessTreePortObjectSpec> {

	String fileName = "ProcessTreeSpec.txt";
	@Override
	public void savePortObjectSpec(ProcessTreePortObjectSpec portObjectSpec, PortObjectSpecZipOutputStream out)
			throws IOException {
		// TODO we just write the spec object into some file, we can't really store the context
		// for each one, we need to create a new
		out.putNextEntry(new ZipEntry(fileName));
		out.write(portObjectSpec.getFileName().getBytes());
		
	}

	@Override
	public ProcessTreePortObjectSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
		// load spec from some file.. but first we need to differ the context
		String entryName = in.getNextEntry().getName();
		
		if (!entryName.equals(fileName)) {
            throw new IOException("Found unexpected zip entry "
                    + entryName + "! Expected " + fileName);
        }
		
		
		byte[] buffer = new byte[512];
		
		ProcessTreePortObjectSpec spec = null;
		try {
			spec = new ProcessTreePortObjectSpec();
			
			in.read(buffer);
			// now we need to transfer bytes into string
			String tmpName = new String(buffer);
			spec.setFileName(tmpName);
		}catch (Exception e) {
            throw new IOException(e);
        }
		return spec;
	}



}
