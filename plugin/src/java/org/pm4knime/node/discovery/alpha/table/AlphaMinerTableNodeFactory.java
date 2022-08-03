package org.pm4knime.node.discovery.alpha.table;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class AlphaMinerTableNodeFactory extends NodeFactory<AlphaMinerTableNodeModel> {
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
