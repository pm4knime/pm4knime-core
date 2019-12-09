package org.pm4knime.node.io.log.reader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class XLogReaderNodeFactory extends NodeFactory<XLogReaderNodeModel> {

	@Override
	public XLogReaderNodeModel createNodeModel() {
		return new XLogReaderNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<XLogReaderNodeModel> createNodeView(int viewIndex, XLogReaderNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new XLogReaderNodeDialog();
	}

}
