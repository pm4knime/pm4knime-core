package org.pm4knime.node.io.log.writer.mxml;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.filehandling.core.port.FileSystemPortObject;
import org.pm4knime.portobject.XLogPortObject;


public class MXMLWriterNodeFactory 
        extends ConfigurableNodeFactory<MXMLWriterNodeModel> {
	
	
	public static final String CONNECTION_INPUT_PORT_GRP_NAME = "File System Connection";
    static final String Log_INPUT_PORT_GRP_NAME = "Event Log";

	private MXMLWriterNodeModel model;

	@Override
	protected MXMLWriterNodeModel createNodeModel(NodeCreationConfiguration creationConfig) {
		// TODO Auto-generated method stub
		this.model = new MXMLWriterNodeModel(creationConfig);
		return this.model;
	}

    
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    
    @Override
    public NodeView<MXMLWriterNodeModel> createNodeView(final int viewIndex,
            final MXMLWriterNodeModel nodeModel) {
        return null;
    }

    
    @Override
    public boolean hasDialog() {
        return true;
    }

    
    @Override
	protected NodeDialogPane createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		// TODO Auto-generated method stub
		return new MXMLWriterNodeDialog(creationConfig, this.model);
	}
    
    @Override
    protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
        final PortsConfigurationBuilder builder = new PortsConfigurationBuilder();
        builder.addOptionalInputPortGroup(CONNECTION_INPUT_PORT_GRP_NAME, FileSystemPortObject.TYPE);
        builder.addFixedInputPortGroup(Log_INPUT_PORT_GRP_NAME, XLogPortObject.TYPE);
        return Optional.of(builder);
    }

}

