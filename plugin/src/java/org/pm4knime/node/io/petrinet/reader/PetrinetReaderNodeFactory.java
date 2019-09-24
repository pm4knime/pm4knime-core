package org.pm4knime.node.io.petrinet.reader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "PetrinetReader" Node.
 * read Petri net from pnml file
 *
 * @author KFDing
 */
public class PetrinetReaderNodeFactory 
        extends NodeFactory<PetrinetReaderNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public PetrinetReaderNodeModel createNodeModel() {
        return new PetrinetReaderNodeModel();
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
        return new PetrinetReaderNodeDialog();
    }

	@Override
	protected int getNrNodeViews() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public NodeView<PetrinetReaderNodeModel> createNodeView(int viewIndex, PetrinetReaderNodeModel nodeModel) {
		// TODO Auto-generated method stub
		return null;
	}

}

