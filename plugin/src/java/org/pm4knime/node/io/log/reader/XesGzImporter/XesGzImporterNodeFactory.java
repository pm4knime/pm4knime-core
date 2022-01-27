package org.pm4knime.node.io.log.reader.XesGzImporter;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;

/**
 * This is an example implementation of the node factory of the "XesGzImporter"
 * node.
 *
 * @author tbd
 */
public class XesGzImporterNodeFactory extends ConfigurableNodeFactory<XesGzImporterNodeModel> {
	private static final String VARIABLE_INPUT_PORT_GRP_NAME = "Variable Input Port";

	private static final String VARIABLE_OUTPUT_PORT_GRP_NAME = "Variable Output Port";

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
	public NodeView<XesGzImporterNodeModel> createNodeView(final int viewIndex,
			final XesGzImporterNodeModel nodeModel) {
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

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		final PortsConfigurationBuilder builder = new PortsConfigurationBuilder();
		builder.addFixedInputPortGroup(VARIABLE_INPUT_PORT_GRP_NAME, FlowVariablePortObject.TYPE_OPTIONAL);
		builder.addFixedOutputPortGroup(VARIABLE_OUTPUT_PORT_GRP_NAME, FlowVariablePortObject.TYPE);
		return Optional.of(builder);
	}

	@Override
	protected XesGzImporterNodeModel createNodeModel(NodeCreationConfiguration creationConfig) {
		// TODO Auto-generated method stub
		return new XesGzImporterNodeModel((creationConfig.getPortConfig().orElseThrow(IllegalStateException::new)));
	}

	@Override
	protected NodeDialogPane createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		// TODO Auto-generated method stub
		return new XesGzImporterNodeDialog(creationConfig.getPortConfig().orElseThrow(IllegalStateException::new));
	}

}
