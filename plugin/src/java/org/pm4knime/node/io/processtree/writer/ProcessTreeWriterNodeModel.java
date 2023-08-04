package org.pm4knime.node.io.processtree.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.EnumSet;

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
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;
import org.pm4knime.util.defaultnode.DefaultNodeModel;


public class ProcessTreeWriterNodeModel extends DefaultNodeModel {
    
	private static final NodeLogger logger = NodeLogger
            .getLogger(ProcessTreeWriterNodeModel.class);
        
	final String PATH_SETTINGS_TAB = "path setting tab";
	final SettingsModelWriterFileChooser m_fileChooserModel;
	protected final String[] FILE_SUFFIXES = new String[]{".ptml"};
	final NodeModelStatusConsumer m_statusConsumer;
	private Path outputPath;
		
    protected ProcessTreeWriterNodeModel(NodeCreationConfiguration creationConfig) {
    
        super(new PortType[] {ProcessTreePortObject.TYPE}, new PortType[] {});
        PortsConfiguration portsConfig = creationConfig.getPortConfig().orElseThrow(IllegalStateException::new);
		m_fileChooserModel = new SettingsModelWriterFileChooser("file_chooser_settings", portsConfig,
        		"File System Connection", EnumConfig.create(FilterMode.FILE),
                EnumConfig.create(FileOverwritePolicy.FAIL, FileOverwritePolicy.OVERWRITE),
                FILE_SUFFIXES);
		m_statusConsumer = new NodeModelStatusConsumer(EnumSet.of(MessageType.ERROR, MessageType.WARNING));
    }

    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        logger.info("Begin: write Process Tree into ptml file");
        
    	OutputStream outStream;
        try {
            outStream = FSFiles.newOutputStream(outputPath,
            		m_fileChooserModel.getFileOverwritePolicy().getOpenOptions());
        } catch (final FileAlreadyExistsException e) {
            throw new InvalidSettingsException(
                "Output file '" + e.getFile() + "' exists and must not be overwritten due to user settings.", e);
        }

    	ProcessTreePortObject m_ptPort = (ProcessTreePortObject) inData[0];
        
        if(m_ptPort.getTree() != null) {
        	m_ptPort.save_from_stream(outStream);
        }
        
        checkCanceled(exec);
        logger.info("End: write Process Tree into ptml file");
        
        return new PortObject[]{};
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        
    	String warning = CheckUtils.checkDestinationFile(m_fileChooserModel.getFileSystemName(), true);
        if(warning != null) {
        	setWarningMessage(warning);
        }
        if(inSpecs[0].getClass().equals(ProcessTreePortObjectSpec.class)) {
        	checkFileExtension();
        	return new PortObjectSpec[] {};
        } else {
        	throw new InvalidSettingsException("Invalid Input! Process Tree expected!");
        }     
    	
    }

    public void checkFileExtension() throws InvalidSettingsException {
		final WritePathAccessor accessor = m_fileChooserModel.createWritePathAccessor();	
		m_statusConsumer.setWarningsIfRequired(this::setWarningMessage);
        outputPath = accessor.getOutputPath(m_statusConsumer);
        createParentDirIfRequired(outputPath);
        if (!outputPath.toString().endsWith(".ptml")) {
    		throw new InvalidSettingsException(
	                "Invalid file extension! .ptml is expected!");
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
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_fileChooserModel.saveSettingsTo(SettingsUtils.getOrAdd(settings, PATH_SETTINGS_TAB));  
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	try {
			m_fileChooserModel.loadSettingsFrom(settings.getNodeSettings(PATH_SETTINGS_TAB));
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
}

