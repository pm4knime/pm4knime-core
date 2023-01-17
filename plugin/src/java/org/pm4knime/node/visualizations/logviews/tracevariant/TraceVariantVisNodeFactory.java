package org.pm4knime.node.visualizations.logviews.tracevariant;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;

public class TraceVariantVisNodeFactory extends NodeFactory<TraceVariantVisNodeModel>
implements WizardNodeFactoryExtension<TraceVariantVisNodeModel, TraceVariantVisViewRepresentation, TraceVariantVisViewValue> {

TraceVariantVisNodeModel n;

@Override
public TraceVariantVisNodeModel createNodeModel() {

	n = new TraceVariantVisNodeModel();
	return n;
}

@Override
protected int getNrNodeViews() {
	return 0;
}

@Override
public NodeView<TraceVariantVisNodeModel> createNodeView(int viewIndex, TraceVariantVisNodeModel nodeModel) {
	return null;
}

@Override
protected boolean hasDialog() {
	return true;
}

@Override
protected NodeDialogPane createNodeDialogPane() {
	return new TraceVariantVisNodeDialog(n);
}

}