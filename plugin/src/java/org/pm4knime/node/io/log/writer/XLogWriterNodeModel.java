package org.pm4knime.node.io.log.writer;

import java.io.File;
import java.io.IOException;

import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;
import org.pm4knime.node.io.petrinet.writer.PetrinetWriterNodeDialog;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.processmining.plugins.log.exporting.ExportLogMxml;
import org.processmining.plugins.log.exporting.ExportLogMxmlGz;
import org.processmining.plugins.log.exporting.ExportLogXes;
import org.processmining.plugins.log.exporting.ExportLogXesGz;

/**
 * This is the model implementation of XLogWriter.
 * It exports the event log into files, which seperates the codes from the reading part
 *
 * @author Kefang
 */
public class XLogWriterNodeModel extends NodeModel {
    
	private final SettingsModelString m_format = XLogWriterNodeModel.createFormatModel();
	private final SettingsModelString m_outfile = XLogWriterNodeModel.createFileNameModel();
	
    public static final String[] formatTypes = {"XES","XES_GZ","MXML","MXML_GZ"};
    /**
     * Constructor for the node model.
     */
    protected XLogWriterNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] {XLogPortObject.TYPE}, new PortType[] {});
    }

    public static SettingsModelString createFormatModel() {
		// TODO Auto-generated method stub
		return new SettingsModelString("XLog Writer Format", formatTypes[0]);
	}

    public static SettingsModelString createFileNameModel() {
		// TODO Auto-generated method stub
		return new SettingsModelString("XLog Writer File Name", "");
	}

	/**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO: As a sink, it has no output 
    	XLogPortObject logData = (XLogPortObject) inData[0];
    	
    	if(logData.getLog()!=null) {
    		File file = new File(m_outfile.getStringValue());
    		writeToFile(file, logData.getLog(), m_format.getStringValue());
    	}
    	
        return new PortObject[] {};
    }

    protected void writeToFile(File file, XLog log, String format) throws IOException {
    	switch(format) {
    	
    	case "XES":
    		ExportLogXes.export(log, file);
    		break;
    	case "XES_GZ":
    		ExportLogXesGz.export(log, file);
    		break;
    	case "MXML":
    		ExportLogMxml.export(log, file);
    		break;
    	case "MXML_GZ":
    		ExportLogMxmlGz.export(log, file);
    		break;
    	}
    	
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

        // TODO: only when the portspec is the log, then we write it down
    	String warning = CheckUtils.checkDestinationFile(m_outfile.getStringValue(), true);
        if(warning != null) {
        	setWarningMessage(warning);
        }
        if(inSpecs[0].getClass().equals(XLogPortObjectSpec.class))
        	return new PortObjectSpec[] {};
        else
        	throw new InvalidSettingsException("Not a Petri net to export");
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	m_format.saveSettingsTo(settings);
    	m_outfile.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_format.loadSettingsFrom(settings);
    	m_outfile.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_format.validateSettings(settings);
    	m_outfile.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

}

