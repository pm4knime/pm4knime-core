package org.pm4knime.node.io.processtree.writer;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.filehandling.core.port.FileSystemPortObject;
import org.pm4knime.portobject.ProcessTreePortObject;

/**
 * <code>NodeFactory</code> for the "ProcessTreeWriter" Node.
 * Export process tree into file
 *
 * @author DKF
 */
public class ProcessTreeWriterNodeFactory 
        extends ConfigurableNodeFactory<ProcessTreeWriterNodeModel> {


	    private ProcessTreeWriterNodeModel model;
	    public static final String CONNECTION_INPUT_PORT_GRP_NAME = "File System Connection";
	    static final String INPUT_PORT_GRP_NAME = "Process Tree";

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
    public NodeView<ProcessTreeWriterNodeModel> createNodeView(final int viewIndex,
            final ProcessTreeWriterNodeModel nodeModel) {
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
        builder.addFixedInputPortGroup(INPUT_PORT_GRP_NAME, ProcessTreePortObject.TYPE);
        return Optional.of(builder);
	}

	@Override
	protected ProcessTreeWriterNodeModel createNodeModel(NodeCreationConfiguration creationConfig) {
		this.model = new ProcessTreeWriterNodeModel(creationConfig);
		return this.model;
	}
	

	@Override
	protected NodeDialogPane createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return new ProcessTreeWriterNodeDialog(creationConfig, this.model);
	}

}

