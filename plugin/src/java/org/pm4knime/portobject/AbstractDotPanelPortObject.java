package org.pm4knime.portobject;

import org.knime.core.node.port.AbstractPortObject;
import org.processmining.plugins.graphviz.visualisation.DotPanel;


public abstract class AbstractDotPanelPortObject extends AbstractPortObject {
	
	public abstract DotPanel getDotPanel();

}