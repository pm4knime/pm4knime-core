package org.pm4knime.node.discovery.alpha;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class AlphaMinerNodeFactory extends NodeFactory<AlphaMinerNodeModel> {

	@Override
	public AlphaMinerNodeModel createNodeModel() {
		return new AlphaMinerNodeModel();
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
		return new AlphaMinerNodeDialog();
	}

}
