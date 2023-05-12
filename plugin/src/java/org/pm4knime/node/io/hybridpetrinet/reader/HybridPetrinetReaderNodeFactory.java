package org.pm4knime.node.io.hybridpetrinet.reader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;

/**
 * <code>NodeFactory</code> for the "PetrinetReader" Node. read Petri net from
 * pnml file
 *
 * @author KFDing
 */
public class HybridPetrinetReaderNodeFactory extends NodeFactory<HybridPetrinetReaderNodeModel> implements WizardNodeFactoryExtension<HybridPetrinetReaderNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HybridPetrinetReaderNodeModel createNodeModel() {
		return new HybridPetrinetReaderNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new HybridPetrinetReaderNodeDialog();
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

}
