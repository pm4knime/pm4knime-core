package org.pm4knime.node.io.petrinet.reader;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.filehandling.core.port.FileSystemPortObject;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.PetriNetPortObject;

/**
 * <code>NodeFactory</code> for the "PetrinetReader" Node. read Petri net from
 * pnml file
 *
 * @author KFDing
 */
public class PetrinetReaderNodeFactory extends ConfigurableNodeFactory<PetrinetReaderNodeModel> implements WizardNodeFactoryExtension<PetrinetReaderNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	
	private static final String VARIABLE_OUTPUT_PORT_GRP_NAME = "Variable Output Port";
    static final String CONNECTION_INPUT_PORT_GRP_NAME = "File System Connection";
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}


	@Override
	protected int getNrNodeViews() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public NodeView<PetrinetReaderNodeModel> createNodeView(int viewIndex, PetrinetReaderNodeModel nodeModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		final PortsConfigurationBuilder builder = new PortsConfigurationBuilder();
        builder.addOptionalInputPortGroup(CONNECTION_INPUT_PORT_GRP_NAME, FileSystemPortObject.TYPE);
		builder.addFixedOutputPortGroup(VARIABLE_OUTPUT_PORT_GRP_NAME, new PortType[] { PortTypeRegistry.getInstance().getPortType(PetriNetPortObject.class, false) });
		return Optional.of(builder);
	}

	@Override
	protected PetrinetReaderNodeModel createNodeModel(NodeCreationConfiguration creationConfig) {
		// TODO Auto-generated method stub
		return new PetrinetReaderNodeModel((creationConfig.getPortConfig().orElseThrow(IllegalStateException::new)));
	}

	@Override
	protected NodeDialogPane createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		// TODO Auto-generated method stub
		return new PetrinetReaderNodeDialog(creationConfig.getPortConfig().orElseThrow(IllegalStateException::new));
	}

}
