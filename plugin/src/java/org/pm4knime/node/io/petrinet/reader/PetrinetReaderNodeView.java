package org.pm4knime.node.io.petrinet.reader;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "PetrinetReader" Node.
 * read Petri net from pnml file
 * -- one thing how to show the Petri net afer reading this graph
 *
 * @author KFDing
 */
public class PetrinetReaderNodeView extends NodeView<PetrinetReaderNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link PetrinetReaderNodeModel})
     */
    protected PetrinetReaderNodeView(final PetrinetReaderNodeModel nodeModel) {
        super(nodeModel);

        // TODO instantiate the components of the view here.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        PetrinetReaderNodeModel nodeModel = 
            (PetrinetReaderNodeModel)getNodeModel();
        assert nodeModel != null;
        
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
    
        // TODO things to do when closing the view
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {

        // TODO things to do when opening the view
    }

}

