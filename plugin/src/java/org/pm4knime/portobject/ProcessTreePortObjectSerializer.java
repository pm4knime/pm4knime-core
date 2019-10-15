package org.pm4knime.portobject;

import java.io.IOException;
import java.util.zip.ZipEntry;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject.PortObjectSerializer;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;

public class ProcessTreePortObjectSerializer extends PortObjectSerializer<ProcessTreePortObject> {

	// here is difficult because we need to ahve out and in secifical already, we can't change it
	// which makes the transfer between the filea and other output difficult
	
	String FILE_NAME = "ProcessTreeObject.ptml";
	
	@Override
	public void savePortObject(ProcessTreePortObject portObject, PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO save port object into one out
		out.putNextEntry(new ZipEntry(FILE_NAME));
		out.write(portObject.toText().getBytes());
	}

	@Override
	public ProcessTreePortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO load process tree from file
		String entryName = in.getNextEntry().getName();
		
		if (!entryName.equals(FILE_NAME)) {
            throw new IOException("Found unexpected zip entry "
                    + entryName + "! Expected " + FILE_NAME);
        }
		ProcessTreePortObject portObj = null;
		try {// here we need to take care about the environment, we need to create a context, because we don't have one
            portObj = new ProcessTreePortObject();
            portObj.loadFromDefault((ProcessTreePortObjectSpec)spec, in);
        } catch (Exception e) {
            throw new IOException(e);
        }
        return portObj;
		
	}

}
