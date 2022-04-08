package org.pm4knime.node.discovery.inductiveminer.Table;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveminer2.plugins.ProcessTreeVisualisation;

public class EfficientTreePortObject implements PortObject {
	// if we put save and load codes at this place, then we save codes for reader and writer,
	// because we can use them directly.. so we put the save and load here
	// but we need to specify the input and output operator
	
	// private ProcessTreePortObjectSpec m_spec ;
	private EfficientTree tree;
	ProcessTreePortObjectSpec m_spec;
	public EfficientTreePortObject(EfficientTree t) {
		tree = t;
	}
	
	public EfficientTreePortObject() {
	}
	
	public void setTree(EfficientTree tree) {
		this.tree = tree;
	}
	
	public EfficientTree getTree() {
		return tree;
	}
	
	@Override
	public String getSummary() {
		// TODO I guess this is used to describe the object
		return "This is a process tree.";
	}

	@Override
	public PortObjectSpec getSpec() {
		// TODO Auto-generated method stub
		if(m_spec!=null)
			return m_spec;
		
		return new ProcessTreePortObjectSpec();
	}
	
	
	public void setSpec(PortObjectSpec spec) {
		// TODO Auto-generated method stub
		m_spec = (ProcessTreePortObjectSpec) spec;
	}

	@Override
	public JComponent[] getViews() {
		// TODO this is used to show the process tree
		if(tree != null) {
			
			JPanel viewPanel;
			viewPanel = new DotPanel(ProcessTreeVisualisation.fancy(tree));
			viewPanel.setName("Generated process tree");
			return new JComponent[] { viewPanel };
			
		}
		
		return new JComponent[] {};
	}
	
	public DotPanel getDotPanel() {
		
	if(tree != null) {
			
			DotPanel navDot;
			navDot = new DotPanel(ProcessTreeVisualisation.fancy(tree));
			navDot.setName("Generated process tree");
			return navDot;
			
		}
		
		
		return null;
		
	}
	
	public String toText() {
		/*Ptml ptml = new Ptml().marshall(tree);
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + ptml.exportElement(ptml);*/
		return "test";
	}

	public void save(String fileName) throws IOException {
	    // String fileName = spec.getFileName();
		// directly export to specific file
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
		bw.write(toText());
		bw.close();
	}
	
	
	public void loadFromDefault(EffTreePortObjectSpec spec, PortObjectZipInputStream in) throws Exception {
		// TODO here we need to load object from input strem, or we can just give one filename is is ok
		// the problem is here that we need to use the in part and load from it.. so, let check 
		// if we can do it
		/*PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
		
		PtmlImportTree importer = new PtmlImportTree();
		Ptml ptml = importer.importPtmlFromStream(context, in, spec.getFileName(), -1);
		tree = new ProcessTreeImpl(ptml.getId(), ptml.getName());
		ptml.unmarshall(tree);
		setSpec(spec);*/
	}
	
	public void loadFrom(String fileName) throws Exception{
		// here we need to make sure it is the right file format
		/*PluginContext context = PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(PtmlImportTree.class);
		
		PtmlImportTree importer = new PtmlImportTree();
		
		tree = (ProcessTree) importer.importFile(context, fileName);*/
	}

	
}
