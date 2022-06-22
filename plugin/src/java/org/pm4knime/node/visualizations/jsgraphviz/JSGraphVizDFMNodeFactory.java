package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.pm4knime.node.jsvis.DataExplorerNodeRepresentation;
import org.pm4knime.node.jsvis.DataExplorerNodeValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * This is an example implementation of the node factory of the
 * "JSGraphViz" node.
 *
 * @author 
 */
public class JSGraphVizDFMNodeFactory 
        extends NodeFactory<JSGraphVizDFMNodeModel> implements WizardNodeFactoryExtension<JSGraphVizDFMNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	
	@Override
	public JSGraphVizDFMNodeModel createNodeModel() {
		return new JSGraphVizDFMNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<JSGraphVizDFMNodeModel> createNodeView(int viewIndex, JSGraphVizDFMNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return false;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return null;
	}

}

