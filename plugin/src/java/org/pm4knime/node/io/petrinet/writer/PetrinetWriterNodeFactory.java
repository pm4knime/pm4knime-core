package org.pm4knime.node.io.petrinet.writer;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.filehandling.core.port.FileSystemPortObject;
import org.pm4knime.portobject.PetriNetPortObject;

/**
 * <code>NodeFactory</code> for the "PetrinetWriter" Node.
 * Write Petri net into file to implement the serialization.
 *
 * @author 
 */
public class PetrinetWriterNodeFactory 
        extends ConfigurableNodeFactory<PetrinetWriterNodeModel> {


    private PetrinetWriterNodeModel model;
    public static final String CONNECTION_INPUT_PORT_GRP_NAME = "File System Connection";
    static final String PN_INPUT_PORT_GRP_NAME = "Petri Net";

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
    public NodeView<PetrinetWriterNodeModel> createNodeView(final int viewIndex,
            final PetrinetWriterNodeModel nodeModel) {
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
        builder.addFixedInputPortGroup(PN_INPUT_PORT_GRP_NAME, PetriNetPortObject.TYPE);
        return Optional.of(builder);
	}

	@Override
	protected PetrinetWriterNodeModel createNodeModel(NodeCreationConfiguration creationConfig) {
		this.model = new PetrinetWriterNodeModel(creationConfig);
		return this.model;
	}
	

	@Override
	protected NodeDialogPane createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return new PetrinetWriterNodeDialog(creationConfig, this.model);
	}


}

