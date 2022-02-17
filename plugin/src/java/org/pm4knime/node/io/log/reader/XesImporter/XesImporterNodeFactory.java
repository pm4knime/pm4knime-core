package org.pm4knime.node.io.log.reader.XesImporter;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.filehandling.core.port.FileSystemPortObject;
import org.pm4knime.portobject.XLogPortObject;

/**
 * This is an example implementation of the node factory of the "XesImporter"
 * node.
 *
 * @author tbd
 */
public class XesImporterNodeFactory extends ConfigurableNodeFactory<XesImporterNodeModel> {


	private static final String VARIABLE_OUTPUT_PORT_GRP_NAME = "Variable Output Port";
    static final String CONNECTION_INPUT_PORT_GRP_NAME = "File System Connection";

	/**
	 * {@inheritDoc}
	 */
	/**
	 * @Override public XesImporterNodeModel createNodeModel() { // Create and
	 *           return a new node model. return new XesImporterNodeModel(); }
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		// The number of views the node should have, in this cases there is none.
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<XesImporterNodeModel> createNodeView(final int viewIndex, final XesImporterNodeModel nodeModel) {
		// We return null as this example node does not provide a view. Also see
		// "getNrNodeViews()".
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		// Indication whether the node has a dialog or not.
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	/**
	 * @Override public NodeDialogPane createNodeDialogPane() { // This example node
	 *           has a dialog, hence we create and return it here. Also see //
	 *           "hasDialog()". return new XesImporterNodeDialog(); }
	 */
	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		final PortsConfigurationBuilder builder = new PortsConfigurationBuilder();
        builder.addOptionalInputPortGroup(CONNECTION_INPUT_PORT_GRP_NAME, FileSystemPortObject.TYPE);
		builder.addFixedOutputPortGroup(VARIABLE_OUTPUT_PORT_GRP_NAME, new PortType[] { PortTypeRegistry.getInstance().getPortType(XLogPortObject.class, false) });
		return Optional.of(builder);
	}

	@Override
	protected XesImporterNodeModel createNodeModel(NodeCreationConfiguration creationConfig) {
		// TODO Auto-generated method stub
		return new XesImporterNodeModel((creationConfig.getPortConfig().orElseThrow(IllegalStateException::new)));
	}

	@Override
	protected NodeDialogPane createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		// TODO Auto-generated method stub
		return new XesImporterNodeDialog(creationConfig.getPortConfig().orElseThrow(IllegalStateException::new));
	}

}
