package org.pm4knime.node.io.petrinet.reader;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.web.ValidationError;
import org.knime.js.core.node.AbstractSVGWizardNodeModel;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.AbstractDotPanelPortObject;
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.defaultnode.DefaultNodeModel;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.extendedhybridminer.models.hybridpetrinet.ExtendedHybridPetrinet;
import org.processmining.extendedhybridminer.models.pnml.utils;
import org.processmining.plugins.graphviz.dot.Dot;


/**
 * This is the model implementation of PetrinetReader.
 * read Petri net from pnml file
 *
 * @author KFDing
 */
public class PetrinetReaderNodeModel extends AbstractSVGWizardNodeModel<JSGraphVizViewRepresentation, JSGraphVizViewValue> implements PortObjectHolder {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(PetrinetReaderNodeModel.class);
    
    public static final String CFG_FILE_NAME = "PetriNet FileName";

    // now we should assign one read types to the model
    public static final String GFG_PETRINET_TYPE = "Petrinet Type";
    // don't know the use of this parameter
	public static final String CFG_HISTORY_ID = "History ID";
	
	public static final String[] defaultTypes = new String[] {".pnml"};

    
	private final SettingsModelString m_fileName = new SettingsModelString(PetrinetReaderNodeModel.CFG_FILE_NAME, "");
	private final SettingsModelString m_type = new SettingsModelString(GFG_PETRINET_TYPE, "");
	
	PetriNetPortObjectSpec m_spec = new PetriNetPortObjectSpec();
	protected PetriNetPortObject m_netPort;
	
    public PetrinetReaderNodeModel() {
    
        // TODO as one of those tests
        super(null, new PortType[] {PetriNetPortObject.TYPE}, "Petri Net JS View");
    }

    @Override
    protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
        final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        return new PortObject[]{m_netPort};
    }
	
	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		
		 if(m_type.getStringValue().equals(defaultTypes[0])) {
             logger.info("Read Naive Petri net !");
             
             exec.checkCanceled();
             // read the file and create fileInputStream
             AcceptingPetriNet anet = PetriNetUtil.importFromStream(new FileInputStream(m_spec.getFileName()));
             exec.checkCanceled();
         	m_netPort = new PetriNetPortObject(anet);
         }
 		
 		logger.info("end of reading of Petri net");
 		final String dotstr;
		JSGraphVizViewRepresentation representation = getViewRepresentation();

		synchronized (getLock()) {
			AbstractDotPanelPortObject port_obj = (AbstractDotPanelPortObject) m_netPort;
			Dot dot =  port_obj.getDotPanel().getDot();
			dotstr = dot.toString();
		}
		representation.setDotstr(dotstr);
     } 
 


    
    /**
     * we need to define our own Spec for the Petri net and not DataTableSpec
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        
        // here to check if the file exists,if not, it gives a warning information
    	String fileS = m_fileName.getStringValue();
    	String warning = CheckUtils.checkSourceFile(fileS);
    	if(warning != null ) {
    		setWarningMessage(warning);
    	}
    	// we need to get the values from inSpecs to m_fileName?? Or we have it directly?? 
    	URL url = getURLFromSettings(fileS);
    	if(url == null) {
    		throw new IllegalArgumentException("url can't be null");
    	}
    	String url2String ;
    	if("file".equals(url.getProtocol())) {
    		try {
    			url2String =  new File(url.toURI()).getAbsolutePath();
    		}catch(Exception e){
    			url2String = url.toString();
    			String msg = "File \"" + url + "\" is not a valid PMML file:\n" + e.getMessage();
    			setWarningMessage(msg);
    		}
    	}else {
    		url2String = url.toString();
    	}
    	m_spec.setFileName(url2String);
    	
        return new PortObjectSpec[]{m_spec};
    }

    /** Convert argument string to a URL.
     * @param fileS The file string (a url or a file path)
     * @return The url (if it's a path then file access is checked)
     * @throws InvalidSettingsException If no valid url given.
     */
   private static URL getURLFromSettings(final String fileS)
       throws InvalidSettingsException {
       if (fileS == null || fileS.length() == 0) {
           throw new InvalidSettingsException("No file/url specified");
       }

       try {
           return new URL(fileS);
       } catch (MalformedURLException e) {
           File tmp = new File(fileS);
           if (tmp.isFile() && tmp.canRead()) {
               try  {
                   return tmp.getAbsoluteFile().toURI().toURL();
               } catch (MalformedURLException e1) {
                   throw new InvalidSettingsException(e1);
               }
           }
           throw new InvalidSettingsException("File/URL \"" + fileS
                      + "\" cannot be parsed as a URL or represents a non exising file location");
       }

   }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        // TODO save user settings to the config object.
      m_fileName.saveSettingsTo(settings);
      m_type.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO load (valid) settings from the config object.
        // It can be safely assumed that the settings are valided by the 
        // method below.
      m_fileName.loadSettingsFrom(settings);
      m_type.loadSettingsFrom(settings);
      
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	// here check if the source location has a valid syntax, like it is not empty or in the required extension format
    	// to make sure the setting can be loaded into the workflow
    	m_fileName.validateSettings(settings);
    	m_type.validateSettings(settings);
    }
    
    @Override
	protected void performReset() {
	}

	@Override
	protected void useCurrentValueAsDefault() {
	}

	
	@Override
    protected boolean generateImage() {
        return false;
    }
	
	
	@Override
	public JSGraphVizViewRepresentation createEmptyViewRepresentation() {
		return new JSGraphVizViewRepresentation();
	}

	@Override
	public JSGraphVizViewValue createEmptyViewValue() {
		return new JSGraphVizViewValue();
	}
	
	@Override
	public boolean isHideInWizard() {
		return false;
	}

	@Override
	public void setHideInWizard(boolean hide) {
	}

	@Override
	public ValidationError validateViewValue(JSGraphVizViewValue viewContent) {
		return null;
	}

	@Override
	public void saveCurrentValue(NodeSettingsWO content) {
	}
	
	
	@Override
	public String getJavascriptObjectID() {
		return "org.pm4knime.node.visualizations.jsgraphviz.component";
	}


	@Override
	public PortObject[] getInternalPortObjects() {
		// TODO Auto-generated method stub
		return new PortObject[] {};
	}


	@Override
	public void setInternalPortObjects(PortObject[] portObjects) {
		
	}

}

