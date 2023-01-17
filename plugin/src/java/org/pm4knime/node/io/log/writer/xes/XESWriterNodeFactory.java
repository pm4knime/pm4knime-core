package org.pm4knime.node.io.log.writer.xes;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.filehandling.core.port.FileSystemPortObject;
import org.pm4knime.portobject.XLogPortObject;


public class XESWriterNodeFactory 
        extends ConfigurableNodeFactory<XESWriterNodeModel> {


	public static final String CONNECTION_INPUT_PORT_GRP_NAME = "File System Connection";
    static final String Log_INPUT_PORT_GRP_NAME = "Event Log";

	private XESWriterNodeModel model;
    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<XESWriterNodeModel> createNodeView(final int viewIndex,
            final XESWriterNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }
 
	@Override
    protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
        final PortsConfigurationBuilder builder = new PortsConfigurationBuilder();
        builder.addOptionalInputPortGroup(CONNECTION_INPUT_PORT_GRP_NAME, FileSystemPortObject.TYPE);
        builder.addFixedInputPortGroup(Log_INPUT_PORT_GRP_NAME, XLogPortObject.TYPE);
        return Optional.of(builder);
    }

	@Override
	protected XESWriterNodeModel createNodeModel(NodeCreationConfiguration creationConfig) {
		// TODO Auto-generated method stub
		this.model = new XESWriterNodeModel(creationConfig);
		return this.model;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		// TODO Auto-generated method stub
		return new XESWriterNodeDialog(creationConfig, this.model);
	}

}

