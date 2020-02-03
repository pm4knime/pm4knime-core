package org.pm4knime.portobject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;
import org.knime.core.node.tableview.TableView;
/**
 * we don't have things to write in this PortObjectSpec. For the XLog of Inform from the LogInfo,
 * if we check and make that they should be the same, then when we manipulate log, we have exception of the 
 * configure and execution error, so stay here, do nothing..
 * For log manipulation parts, we can make it as the null and then assign it later. 
 * For one log, we need to assign the spec, which is extracted from the event log. 
 * 
 * Currently, only the attributes we care, which includes the global, trace, and event attributes
 * @Modification: 10 Dec 2019 complete the classifier!!
 *   Classifier is stored as a map, with the Prefix and its name, to the class it belongs to 
 *         aMap.put(prefix + clf.name(), clf.getClass().getSimpleName());
 * @author kefang-pads
 *
 */
public class XLogPortObjectSpec implements PortObjectSpec {
	private static final String ZIP_ENTRY_NAME = "XLogPortObjectSpec";
	// if we save the attributes as string in spec, we change the storage to be map
	Map<String, Class> gTraceAttrMap, gEventAttrMap, clfMap;
	public XLogPortObjectSpec() {
		
	}
	
	public XLogPortObjectSpec(Map<String, Class> gTraceAttrMap, Map<String, Class> gEventAttrMap, 
			Map<String, Class> clfMap ) {
		this.gTraceAttrMap = gTraceAttrMap;
		this.gEventAttrMap = gEventAttrMap;
		this.clfMap = clfMap;
	}
	
	public void setSpec(XLogPortObjectSpec spec) {
		gTraceAttrMap = spec.getGTraceAttrMap();
		gEventAttrMap = spec.getGEventAttrMap();
		clfMap = spec.getClassifiersMap();
	}

	
	public Map<String, Class> getClassifiersMap() {
		// TODO Auto-generated method stub
		return clfMap;
	}

	public Map<String, Class> getGEventAttrMap() {
		// TODO Auto-generated method stub
		return gEventAttrMap;
	}

	public Map<String, Class> getGTraceAttrMap() {
		// TODO Auto-generated method stub
		return gTraceAttrMap;
	}

	@Override
	public JComponent[] getViews() {
		// this we could give the spec as one table view and show the key and values for it
		TableView m_specView = new TableView();
		m_specView.createViewMenu();
		m_specView.getContentModel().setSortingAllowed(false);
		m_specView.setShowIconInColumnHeader(false);
	    m_specView.setShowColorInfo(false);
	    
	    m_specView.setName(ZIP_ENTRY_NAME);
	    // how to set the data into m_spec
	    String[] names = new String[]{"Attribute Key", "Attribute Type"};
        DataType[] types = new DataType[]{StringCell.TYPE, StringCell.TYPE};
        DataContainer result = new DataContainer(new DataTableSpec(names, types));
        // create the trace and event attributes here and add it there
        // we need to add trace and event prefix to distinguish it
        int i=0;
        for(String key : gTraceAttrMap.keySet()) {
        	DataRow row = new DefaultRow("Row " + (i++), key, gTraceAttrMap.get(key).getSimpleName());
        	result.addRowToTable(row);
        }
        
        
        for(String key : clfMap.keySet()) {
        	DataRow row = new DefaultRow("Row " + (i++), key, clfMap.get(key).getSimpleName());
        	result.addRowToTable(row);
        }
        
        result.close();
	    m_specView.setDataTable(result.getTable());
	    
	    m_specView.getHeaderTable().sizeWidthToFit();
		return new JComponent[] {m_specView};
	}

	public static class XLogPortObjectSpecSerializer
			extends PortObjectSpecSerializer<XLogPortObjectSpec> {

		@Override
		public void savePortObjectSpec(XLogPortObjectSpec portObjectSpec, PortObjectSpecZipOutputStream out)
				throws IOException {
			// serialize the attributes and classifier
			out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
			// it just defines the attributes from it, we can save define it again. 
			// it just compares the key for the attributes here. So just to save the rebuild the values here
			
			try (ObjectOutputStream objOut = new ObjectOutputStream(out)){
				objOut.writeObject(portObjectSpec.getGTraceAttrMap());
				
				objOut.writeObject(portObjectSpec.getGEventAttrMap());
				
				objOut.writeObject(portObjectSpec.getClassifiersMap());
			}
			
		}

		@Override
		public XLogPortObjectSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
			// load the attributes here
			ZipEntry nextEntry = in.getNextEntry();
			if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_NAME)) {
				throw new IOException("Expected zip entry '" + ZIP_ENTRY_NAME + "' not present");
			}
			Map<String, Class> tMap = null, eMap = null, cMap = null;
			try(ObjectInputStream objIn = new ObjectInputStream(in)){
				try {
					tMap= (Map<String, Class>) objIn.readObject();
					eMap = (Map<String, Class>) objIn.readObject();
					cMap = (Map<String, Class>) objIn.readObject();
					
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return new XLogPortObjectSpec(tMap, eMap, cMap);
		}
	}

}
