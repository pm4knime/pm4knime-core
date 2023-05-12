package org.pm4knime.node.discovery.alpha.table;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;

public class AlphaMinerTableNodeFactory extends NodeFactory<AlphaMinerTableNodeModel> implements WizardNodeFactoryExtension<AlphaMinerTableNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {
	AlphaMinerTableNodeModel node;

	@Override
	public AlphaMinerTableNodeModel createNodeModel() {
		node = new AlphaMinerTableNodeModel();
        return node;
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<AlphaMinerTableNodeModel> createNodeView(int viewIndex, AlphaMinerTableNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new AlphaMinerTableNodeDialog(node);
	}

}
