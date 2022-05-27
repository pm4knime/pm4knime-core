package org.pm4knime.portobject;

import java.io.IOException;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.port.AbstractPortObject.AbstractPortObjectSerializer;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.plugins.EfficientTreeVisualisationPlugin;
import org.processmining.plugins.inductiveminer2.plugins.DfgMsdVisualisationPlugin;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsd;

public class EfficientTreePortObject extends AbstractPortObject{
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(DfgMsdPortObject.class);
	private static final String ZIP_ENTRY_NAME = "DfgMsdPortObject";
	EfficientTree effTree;
	EfficientTreePortObjectSpec m_spec;
	public EfficientTreePortObject() {
	}

	public EfficientTreePortObject(EfficientTree effTree) {
		this.effTree = effTree;
	}

	public EfficientTree getEfficientTree() {
		return effTree;
	}

	public void setEfficientTree(EfficientTree effTree) {
		this.effTree = effTree;
	}

	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return "EfficientTree PortObject";
	}

	@Override
	public PortObjectSpec getSpec() {
		// TODO Auto-generated method stub
		if(m_spec!=null)
			return m_spec;
		return new EfficientTreePortObjectSpec();
	}

	public void setSpec(PortObjectSpec spec) {
		m_spec = (EfficientTreePortObjectSpec) spec;
	}
	@Override
	public JComponent[] getViews() {
		// TODO it has view which is
		// we need to change the steps..
		//JComponent viewPanel = DfgMsdVisualisationPlugin.fancy((DirectlyFollowsGraph) dfm);
		JComponent viewPanel = getDotPanel();
		viewPanel.setName("Efficient Tree Model");
		return new JComponent[] { viewPanel };
	}
	
	public DotPanel getDotPanel() {
		
		if(effTree != null) {
				Dot dot = EfficientTreeVisualisationPlugin.fancy(effTree);
				DotPanel navDot = new DotPanel(dot);
				
				navDot.setName("Generated process tree");
				return navDot;
				
			}
			
			
			return null;
			
		}

	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	
	}

	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	public static final class DfgMsdPortObjectSerializer extends AbstractPortObjectSerializer<DfgMsdPortObject> {
	}
}
