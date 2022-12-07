package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.pm4knime.portobject.CausalGraphPortObject;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.port.PortType;


public class JSVisCGNodeFactory 
        extends NodeFactory<JSGraphVizAbstractModel> implements WizardNodeFactoryExtension<JSGraphVizAbstractModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

    /**
     * {@inheritDoc}
     */
    @Override
    public JSGraphVizAbstractModel createNodeModel() {

    	PortType[] IN_TYPES = {CausalGraphPortObject.TYPE};
        return new JSGraphVizAbstractModel(IN_TYPES, "Causal Graph JS View");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<JSGraphVizAbstractModel> createNodeView(final int viewIndex,
            final JSGraphVizAbstractModel nodeModel) {
		return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return null;
    }

}

