package org.pm4knime.node.io.hybridpetrinet.reader;

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
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.pm4knime.portobject.HybridPetriNetPortObjectSpec;
import org.pm4knime.util.HybridPetriNetUtil;
import org.pm4knime.util.defaultnode.DefaultNodeModel;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.extendedhybridminer.models.hybridpetrinet.ExtendedHybridPetrinet;
// or extended hybridminer
import org.processmining.extendedhybridminer.models.pnml.utils;
import org.processmining.framework.plugin.PluginContext;

/**
 * This is the model implementation of PetrinetReader.
 * read Petri net from pnml file
 *
 * @author KFDing
 */
public class HybridPetrinetReaderNodeModel extends DefaultNodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(HybridPetrinetReaderNodeModel.class);
    
    public static final String CFG_FILE_NAME = "HybridPetriNet FileName";

    // now we should assign one read types to the model
    public static final String GFG_PETRINET_TYPE = "HybridPetrinet Type";
    // don't know the use of this parameter
	public static final String CFG_HISTORY_ID = "History ID";
	
	public static final String[] defaultTypes = new String[] {".pnml"};

    
	private final SettingsModelString m_fileName = new SettingsModelString(HybridPetrinetReaderNodeModel.CFG_FILE_NAME, "");
	private final SettingsModelString m_type = new SettingsModelString(GFG_PETRINET_TYPE, "");
	
	HybridPetriNetPortObjectSpec m_spec = new HybridPetriNetPortObjectSpec();
	
    public HybridPetrinetReaderNodeModel() {
    
        // TODO as one of those tests
        super(null, new PortType[] {HybridPetriNetPortObject.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	HybridPetriNetPortObject m_netPort = null;
        if(m_type.getStringValue().equals(defaultTypes[0])) {
            logger.info("Read Naive Petri net !");
            
            checkCanceled(exec);
            // read the file and create fileInputStream
            
//            HybridPetriNetUtil.importHybridPetrinetFromStream(new FileInputStream(m_spec.getFileName()), net);
            
            ExtendedHybridPetrinet net = new ExtendedHybridPetrinet("Hybrid Petrinet");
            UIContext context = new UIContext();
            UIPluginContext pContext = context.getMainPluginContext();
        	utils.importHybridPetrinetFromStream(pContext, new FileInputStream(m_spec.getFileName()), net);
            checkCanceled(exec);
        	m_netPort = new HybridPetriNetPortObject(net);
        }
		
		logger.info("end of reading of Hybrid Petri net");
        return new PortObject[] {m_netPort};
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
    

}

