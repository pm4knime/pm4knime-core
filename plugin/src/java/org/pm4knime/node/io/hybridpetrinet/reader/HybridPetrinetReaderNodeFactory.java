package org.pm4knime.node.io.hybridpetrinet.reader;

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
import org.pm4knime.portobject.HybridPetriNetPortObject;


public class HybridPetrinetReaderNodeFactory extends ConfigurableNodeFactory<HybridPetrinetReaderNodeModel> implements WizardNodeFactoryExtension<HybridPetrinetReaderNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	private static final String VARIABLE_OUTPUT_PORT_GRP_NAME = "Variable Output Port";
    static final String CONNECTION_INPUT_PORT_GRP_NAME = "File System Connection";
    
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
	public NodeView<HybridPetrinetReaderNodeModel> createNodeView(int viewIndex, HybridPetrinetReaderNodeModel nodeModel) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		final PortsConfigurationBuilder builder = new PortsConfigurationBuilder();
        builder.addOptionalInputPortGroup(CONNECTION_INPUT_PORT_GRP_NAME, FileSystemPortObject.TYPE);
		builder.addFixedOutputPortGroup(VARIABLE_OUTPUT_PORT_GRP_NAME, new PortType[] { PortTypeRegistry.getInstance().getPortType(HybridPetriNetPortObject.class, false) });
		return Optional.of(builder);
	}

	@Override
	protected HybridPetrinetReaderNodeModel createNodeModel(NodeCreationConfiguration creationConfig) {
		// TODO Auto-generated method stub
		return new HybridPetrinetReaderNodeModel((creationConfig.getPortConfig().orElseThrow(IllegalStateException::new)));
	}

	@Override
	protected NodeDialogPane createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		// TODO Auto-generated method stub
		return new HybridPetrinetReaderNodeDialog(creationConfig.getPortConfig().orElseThrow(IllegalStateException::new));
	}

}
