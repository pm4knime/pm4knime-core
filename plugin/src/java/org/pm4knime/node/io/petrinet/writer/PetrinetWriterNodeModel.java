package org.pm4knime.node.io.petrinet.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.util.PetriNetUtil;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of PetrinetWriter.
 * Write Petri net into file to implement the serialization.
 * The input is Petri net, output is the nothing I guess
 * we need to configure the file name for output. That's all. 
 *
 * @author DKF
 */
public class PetrinetWriterNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(PetrinetWriterNodeModel.class);
        
    private SettingsModelString m_outFileName = PetrinetWriterNodeDialog.createFileMode();
    
    String subfix = "marking.txt";
	
	/**
     * Constructor for the node model.
     */
    protected PetrinetWriterNodeModel() {
    
        // TODO one incoming port and one outgoing port is assumed
        super(new PortType[] {PetriNetPortObject.TYPE}, new PortType[] {});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,  final ExecutionContext exec) throws Exception {

        // TODO do something here
        logger.info("Begin to write Petri net into file");

        
        PetriNetPortObject pnObj = (PetriNetPortObject) inData[0];
        
        if(pnObj.getANet() != null) {
        	CheckUtils.checkDestinationFile(m_outFileName.getStringValue(),true);
            
            URL url = FileUtil.toURL(m_outFileName.getStringValue());
            Path localPath = FileUtil.resolveToPath(url);
            
        	File f =  createFile(localPath, url);
        	
			// we should also write the marking into disk
        	FileOutputStream out = new FileOutputStream(f);
        	PetriNetUtil.exportToStream(pnObj.getANet(), out);
    		out.close();
        }
        
        logger.info("End to write Petri net into pnml file");
        return new PortObject[] {};
    }

    private static File createFile(final Path localPath, final URL url) throws IOException {
        if (localPath != null) {
            return localPath.toFile();
        } else {
            return new File(url.getPath());
        }
}
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        
        String warning = CheckUtils.checkDestinationFile(m_outFileName.getStringValue(), true);
        if(warning != null) {
        	setWarningMessage(warning);
        }
        if(inSpecs[0].getClass().equals(PetriNetPortObjectSpec.class))
        	return new PortObjectSpec[] {};
        else
        	throw new InvalidSettingsException("Not a Petri net to export");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_outFileName.saveSettingsTo(settings);
       
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_outFileName.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_outFileName.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        
       

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
       
    }

}

