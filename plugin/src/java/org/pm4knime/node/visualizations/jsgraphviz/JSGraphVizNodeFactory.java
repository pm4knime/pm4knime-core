package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.web.ValidationError;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.js.core.node.AbstractWizardNodeModel;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * This is an example implementation of the node factory of the
 * "JSGraphViz" node.
 *
 * @author 
 */
public class JSGraphVizNodeFactory 
        extends NodeFactory<JSGraphVizNodeModel> implements WizardNodeFactoryExtension<JSGraphVizNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

    /**
     * {@inheritDoc}
     */
    @Override
    public JSGraphVizNodeModel createNodeModel() {
		// Create and return a new node model.
        return new JSGraphVizNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
		// The number of views the node should have, in this cases there is none.
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<JSGraphVizNodeModel> createNodeView(final int viewIndex,
            final JSGraphVizNodeModel nodeModel) {
		// We return null as this example node does not provide a view. Also see "getNrNodeViews()".
		return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
		// Indication whether the node has a dialog or not.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
		// This example node has a dialog, hence we create and return it here. Also see "hasDialog()".
        return null;
    }

}

