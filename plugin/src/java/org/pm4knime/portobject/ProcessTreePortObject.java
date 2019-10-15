package org.pm4knime.portobject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.incorporatenegativeinformation.dialogs.ui.process.GraphvizProcessTree;
import org.processmining.incorporatenegativeinformation.dialogs.ui.process.GraphvizProcessTree.NotYetImplementedException;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.ProcessTreeImpl;
import org.processmining.processtree.ptml.Ptml;
import org.processmining.processtree.ptml.importing.PtmlImportTree;
import org.processmining.processtree.visualization.tree.TreeVisualization;

public class ProcessTreePortObject implements PortObject{
	// if we put save and load codes at this place, then we save codes for reader and writer,
	// because we can use them directly.. so we put the save and load here
	// but we need to specify the input and output operator
	
	// private ProcessTreePortObjectSpec m_spec ;
	private ProcessTree tree;
	
	public ProcessTreePortObject(ProcessTree t) {
		tree = t;
	}
	
	public ProcessTreePortObject() {
	}
	
	public void setTree(ProcessTree tree) {
		this.tree = tree;
	}
	
	public ProcessTree getTree() {
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
		return new ProcessTreePortObjectSpec();
	}

	@Override
	public JComponent[] getViews() {
		// TODO this is used to show the process tree
		if(tree != null) {
			
			JPanel viewPanel;
			try {
				viewPanel = new DotPanel(GraphvizProcessTree.convert(tree));
				viewPanel.setName("Generated process tree");
				return new JComponent[] { viewPanel };
			} catch (NotYetImplementedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return new JComponent[] {};
	}
	
	public String toText() {
		Ptml ptml = new Ptml().marshall(tree);
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + ptml.exportElement(ptml);
		return text;
	}

	public void save(String fileName) throws IOException {
	    // String fileName = spec.getFileName();
		// directly export to specific file
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
		bw.write(toText());
		bw.close();
	}
	
	
	public void loadFromDefault(ProcessTreePortObjectSpec spec, PortObjectZipInputStream in) throws Exception {
		// TODO here we need to load object from input strem, or we can just give one filename is is ok
		// the problem is here that we need to use the in part and load from it.. so, let check 
		// if we can do it
		PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
		
		PtmlImportTree importer = new PtmlImportTree();
		Ptml ptml = importer.importPtmlFromStream(context, in, spec.getFileName(), -1);
		tree = new ProcessTreeImpl(ptml.getId(), ptml.getName());
		ptml.unmarshall(tree);
		
	}
	
	public void loadFrom(String fileName) throws Exception{
		// here we need to make sure it is the right file format
		PluginContext context = PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(PtmlImportTree.class);
		
		PtmlImportTree importer = new PtmlImportTree();
		
		tree = (ProcessTree) importer.importFile(context, fileName);
	}


	public static void main(String[] args) {
		String fileName = "D:\\ProcessMining\\Programs\\MSProject\\dataset\\property-experiment\\model_pt_02_with_2_xor.ptml";
		
		ProcessTree tree;
		PluginContext context = null;
		context = PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(PtmlImportTree.class);
		
		PtmlImportTree importer = new PtmlImportTree();
		
		try {
			tree = (ProcessTree) importer.importFile(context, fileName);
			System.out.println("tree information " + tree.getName() +  ", size of nodes:  " +tree.getNodes().size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
