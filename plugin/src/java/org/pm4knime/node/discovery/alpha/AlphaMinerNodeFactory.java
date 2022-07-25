package org.pm4knime.node.discovery.alpha;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.pm4knime.node.discovery.inductiveminer.Table.InductiveMinerTableNodeModel;

public class AlphaMinerNodeFactory extends NodeFactory<AlphaMinerNodeModel> {
	AlphaMinerNodeModel node;

	@Override
	public AlphaMinerNodeModel createNodeModel() {
		node = new AlphaMinerNodeModel();
        return node;
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<AlphaMinerNodeModel> createNodeView(int viewIndex, AlphaMinerNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new AlphaMinerNodeDialog(node);
	}

}
