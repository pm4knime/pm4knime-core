package org.pm4knime.node.io.log.writer.xes;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.EnumSet;

import javax.swing.JCheckBox;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;
import org.knime.filehandling.core.connections.FSFiles;
import org.knime.filehandling.core.defaultnodesettings.EnumConfig;
import org.knime.filehandling.core.defaultnodesettings.filechooser.writer.FileOverwritePolicy;
import org.knime.filehandling.core.defaultnodesettings.filechooser.writer.SettingsModelWriterFileChooser;
import org.knime.filehandling.core.defaultnodesettings.filechooser.writer.WritePathAccessor;
import org.knime.filehandling.core.defaultnodesettings.filtermode.SettingsModelFilterMode.FilterMode;
import org.knime.filehandling.core.defaultnodesettings.status.NodeModelStatusConsumer;
import org.knime.filehandling.core.defaultnodesettings.status.StatusMessage.MessageType;
import org.knime.filehandling.core.util.SettingsUtils;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.defaultnode.DefaultNodeModel;


public class XESWriterNodeModel extends DefaultNodeModel {
	private final NodeLogger logger = NodeLogger.getLogger(XESWriterNodeModel.class);
	final String COMREESS_KEY = "compress_key";
	final String PATH_SETTINGS_TAB = "path setting tab";
	final SettingsModelWriterFileChooser m_fileChooserModel;
	protected final String[] FILE_SUFFIXES = new String[]{".xes", ".xes.gz"};
	final NodeModelStatusConsumer m_statusConsumer;
	JCheckBox m_compressWithGzipChecker;
	private Path outputPath;

    protected XESWriterNodeModel(final NodeCreationConfiguration creationConfig) {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] {XLogPortObject.TYPE}, null);
    	PortsConfiguration portsConfig = creationConfig.getPortConfig().orElseThrow(IllegalStateException::new);
		m_fileChooserModel = new SettingsModelWriterFileChooser("file_chooser_settings", portsConfig,
        		"File System Connection", EnumConfig.create(FilterMode.FILE),
                EnumConfig.create(FileOverwritePolicy.FAIL, FileOverwritePolicy.OVERWRITE),
                FILE_SUFFIXES);
		m_compressWithGzipChecker = new JCheckBox("Compress output file (gz)");
		 m_statusConsumer = new NodeModelStatusConsumer(EnumSet.of(MessageType.ERROR, MessageType.WARNING));
    }


    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	XLogPortObject logData = (XLogPortObject) inData[0];
    	
    	if(logData.getLog()!=null) {
    		if(logData.getLog().size()<1) {
    			logger.warn("The current event log has no trace.");
    		}
    		checkCanceled(exec);    		
    		writeToFile(createOutputStream(outputPath), logData.getLog());
    	}
    	
    	logger.info("Begin: Write XES");
        return new PortObject[] {};
    }
    
    private OutputStream createOutputStream(final Path outputPath) throws IOException, InvalidSettingsException {

    	
    	OutputStream outStream;
        try {
            outStream = FSFiles.newOutputStream(outputPath,
            		m_fileChooserModel.getFileOverwritePolicy().getOpenOptions());
        } catch (final FileAlreadyExistsException e) {
            throw new InvalidSettingsException(
                "Output file '" + e.getFile() + "' exists and must not be overwritten due to user settings.", e);
        }
        
        return outStream;
    }

    protected void writeToFile(OutputStream outputStream, XLog log) throws IOException {
    	if(m_compressWithGzipChecker.isSelected()) {       	
    		XSerializer logSerializer = new XesXmlGZIPSerializer();
    		logSerializer.serialize(log, outputStream);
    		outputStream.close();
    	} else {
    		XSerializer logSerializer = new XesXmlSerializer();
    		logSerializer.serialize(log, outputStream);
    		outputStream.close();
    	}   	 	
    }
    private void createParentDirIfRequired(final Path outputPath) throws InvalidSettingsException {
        final Path parentPath = outputPath.getParent();
        try {
			if (parentPath != null && !FSFiles.exists(parentPath)) {
			    if (m_fileChooserModel.isCreateMissingFolders()) {
			        FSFiles.createDirectories(parentPath);
			    } else {
			        throw new InvalidSettingsException(String.format(
			            "The directory '%s' does not exist and must not be created due to user settings.", parentPath));
			    }
			}
		} catch (AccessDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

        String warning = CheckUtils.checkDestinationFile(m_fileChooserModel.getFileSystemName(), true);
        if(warning != null) {
        	setWarningMessage(warning);
        }
        if(inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) {
        	checkFileExtension();
        	return new PortObjectSpec[] {};
        } else {
        	throw new InvalidSettingsException("Invalid Input! Event Log expected!");
        }      	
        
    }


	public void checkFileExtension() throws InvalidSettingsException {
		final WritePathAccessor accessor = m_fileChooserModel.createWritePathAccessor();	
		m_statusConsumer.setWarningsIfRequired(this::setWarningMessage);
        outputPath = accessor.getOutputPath(m_statusConsumer);
        createParentDirIfRequired(outputPath);
		boolean compress = m_compressWithGzipChecker.isSelected(); 
    	if (compress) {
        	if (!outputPath.toString().endsWith(".xes.gz")) {
        		throw new InvalidSettingsException(
    	                "Invalid file extension! .xes.gz is expected!");
        	}
        } else {
        	if (!outputPath.toString().endsWith(".xes")) {
    			throw new InvalidSettingsException(
    	                "Invalid file extension! .xes is expected!");
        	}
        }  	
		
	}
	
	
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		settings.addBoolean(COMREESS_KEY, m_compressWithGzipChecker.isSelected());
        m_fileChooserModel.saveSettingsTo(SettingsUtils.getOrAdd(settings, PATH_SETTINGS_TAB));   
	}


	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		m_compressWithGzipChecker.setSelected(settings.getBoolean(COMREESS_KEY, false));
		try {
			m_fileChooserModel.loadSettingsFrom(settings.getNodeSettings(PATH_SETTINGS_TAB));
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    
}

