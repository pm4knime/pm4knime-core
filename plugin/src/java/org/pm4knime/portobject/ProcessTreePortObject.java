package org.pm4knime.portobject;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.plugins.GraphvizProcessTree;
import org.processmining.plugins.inductiveVisualMiner.plugins.GraphvizProcessTree.NotYetImplementedException;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.ProcessTreeImpl;
import org.processmining.processtree.ptml.Ptml;
import org.processmining.processtree.ptml.importing.PtmlImportTree;

public class ProcessTreePortObject extends AbstractDotPanelPortObject {
	// if we put save and load codes at this place, then we save codes for reader and writer,
	// because we can use them directly.. so we put the save and load here
	// but we need to specify the input and output operator
	
	// private ProcessTreePortObjectSpec m_spec ;
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(ProcessTreePortObject.class);
	public static final PortType TYPE_OPTIONAL =
			PortTypeRegistry.getInstance().getPortType(ProcessTreePortObject.class, true);
	
	private static final String ZIP_ENTRY_NAME = "ProcessTreePortObject";
	String FILE_NAME = "ProcessTreeObject.ptml";
	
	ProcessTree tree;
	ProcessTreePortObjectSpec m_spec;
	
	public ProcessTreePortObject(ProcessTree t) {
		this.tree = t;
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
		return null;
	}
	
	public boolean equals(Object o) {
		return tree.equals(o);
	}

	@Override
	public PortObjectSpec getSpec() {
		if(m_spec!=null)
			return m_spec;
		return new ProcessTreePortObjectSpec();
	}
	
	
	public void setSpec(PortObjectSpec spec) {
		m_spec = (ProcessTreePortObjectSpec) spec;
	}

	@Override
	public JComponent[] getViews() {
		// TODO this is used to show the process tree
//		if(tree != null) {
//			
//			JPanel viewPanel;
//			try {
//				viewPanel = new DotPanel(GraphvizProcessTree.convert(tree));
//				viewPanel.setName("Generated process tree");
//				return new JComponent[] { viewPanel };
//			} catch (NotYetImplementedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
		
		return new JComponent[] {};
	}
	
	public DotPanel getDotPanel() {
		
	    if(tree != null) {
			
			DotPanel navDot;
			try {
				navDot = new DotPanel(GraphvizProcessTree.convert(tree));
				navDot.setName("Generated process tree");
				return navDot;
			} catch (NotYetImplementedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		return null;
		
	}
	
	public String toText() {
		Ptml ptml = new Ptml().marshall(tree);
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + ptml.exportElement(ptml);
		return text;
	}

	public void save(String fileName) throws IOException {
	    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
		bw.write(toText());
		bw.close();
	}
	
	
	public void save_from_stream(OutputStream out) throws IOException {
	    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		bw.write(toText());
		bw.close();
	}
	
	
	
	public void loadFromDefault(ProcessTreePortObjectSpec spec, InputStream in) throws Exception {
		// TODO here we need to load object from input strem, or we can just give one filename is is ok
		// the problem is here that we need to use the in part and load from it.. so, let check 
		// if we can do it
		PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
		
		PtmlImportTree importer = new PtmlImportTree();
		Ptml ptml = importer.importPtmlFromStream(context, in, spec.getFileName(), -1);
		tree = new ProcessTreeImpl(ptml.getId(), ptml.getName());
		ptml.unmarshall(tree);
		setSpec(spec);
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

	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		out.putNextEntry(new ZipEntry(FILE_NAME));
		out.write(toText().getBytes());
	}

	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		String entryName = in.getNextEntry().getName();
		
		if (!entryName.equals(FILE_NAME)) {
            throw new IOException("Found unexpected zip entry "
                    + entryName + "! Expected " + FILE_NAME);
        }
		try {
			loadFromDefault((ProcessTreePortObjectSpec)spec, in);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static class ProcessTreePortObjectSerializer extends AbstractPortObject.AbstractPortObjectSerializer<ProcessTreePortObject> {
//		String FILE_NAME = "ProcessTreeObject.ptml";
//		
//		@Override
//		public void savePortObject(ProcessTreePortObject portObject, PortObjectZipOutputStream out, ExecutionMonitor exec)
//				throws IOException, CanceledExecutionException {
//			// TODO save port object into one out
//			out.putNextEntry(new ZipEntry(FILE_NAME));
//			out.write(portObject.toText().getBytes());
//		}
//
//		@Override
//		public ProcessTreePortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
//				throws IOException, CanceledExecutionException {
//			// TODO load process tree from file
//			String entryName = in.getNextEntry().getName();
//			
//			if (!entryName.equals(FILE_NAME)) {
//	            throw new IOException("Found unexpected zip entry "
//	                    + entryName + "! Expected " + FILE_NAME);
//	        }
//			ProcessTreePortObject portObj = null;
//			try {// here we need to take care about the environment, we need to create a context, because we don't have one
//	            portObj = new ProcessTreePortObject();
//	            portObj.loadFromDefault((ProcessTreePortObjectSpec)spec, in);
//	        } catch (Exception e) {
//	            throw new IOException(e);
//	        }
//	        return portObj;
//			
//		}

    }
	
}
