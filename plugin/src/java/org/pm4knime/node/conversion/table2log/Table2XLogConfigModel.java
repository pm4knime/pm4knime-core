package org.pm4knime.node.conversion.table2log;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.pm4knime.util.XLogSpecUtil;
import org.processmining.log.csvimport.config.CSVConversionConfig;
import org.processmining.log.csvimport.config.CSVConversionConfig.CSVEmptyCellHandlingMode;
import org.processmining.log.csvimport.config.CSVConversionConfig.CSVErrorHandlingMode;

/*
 * when is is used as a customized configuration node model, not extend SettingsModel. There exists
 * problem. 1. no key for instances.  2. if no key there, we can't refer to the instances with the same value. 
 * Not really, what I want to know is!! If in NodeDialog and NodeModel, they create different instances. 
 *  If the instances are different, how do they make sure with the same key, they could access the same values there? 
 *  If they are the same, then how does it work behind this? By using HashMap??
 *  
 *  @18 Nov 2019. Should rewrite the codes to be SettingsModel based. 
 *  Modification: make the factory as one option. Another changes is that, this is converted from DataTable,
 *  not directly from CSV file!!! So we need to change the name here, to check if to save it as CSV file, or table file??
 *  
 *  @Modification: 13 Dec 2019, to allow the use of lifecycle:transition
 *  
 *   Add one option to choose, if it is with lifecycle:transition. 
 *   -- with: column to assign the value, and column for the timestamp with its format
 *   -- without: just choose the time stamp and its format
 *   
 */
public class Table2XLogConfigModel {

	public static final String CFG_KEY_XFACTORY = "Factory Name";
	public static final String CFG_KEY_ERROR_HANDLE_MODE = "Error Handling Mode";
	public static final String CFG_KEY_EMPTY_HANDLE_MODE = "Empty Cell Handling Mode";
	public static final String CFG_KEY_ADD_START_EA = "Add Start Event Attribute";

	// we add the column configuration into this config model
	// here we have two model to choose for caseID and eventID items
	public static final String CFG_KEY_CASEID = "Case ID";
	public static final String CFG_KEY_EVENTID = "Event ID";
	public static final String CFG_KEY_EVENTCLASS = "Event class";
	
	public static final String CFG_KEY_LIFECYCLE = "Life Cycle";
	public static final String CFG_KEY_TIMESTAMP = "Time stamp";
	public static final String CFG_KEY_TS_FORMAT = "Time stamp format";
	public static final String CFG_NO_OPTION = "NO AVAILABLE";

	private String configName;
	
	private SettingsModelString m_caseID = new SettingsModelString(CFG_KEY_CASEID, "CaseID");
	private SettingsModelString m_eventClass = new SettingsModelString(CFG_KEY_EVENTCLASS, "Event Class");
	
//	private SettingsModelString m_eventID = new SettingsModelString(CFG_KEY_EVENTID, "EventID");

	private SettingsModelString m_lifecycle = new SettingsModelString(CFG_KEY_LIFECYCLE, "");
	// with situation, choose the column to represent the lifecycle column value

	private SettingsModelString m_timeStamp = new SettingsModelString(CFG_KEY_TIMESTAMP, "");
	// allow user self define one
	private SettingsModelString m_tsFormat = new SettingsModelString(CFG_KEY_TS_FORMAT + " Date Format", "");

	private SettingsModelFilterString m_traceAttrSet = new SettingsModelFilterString(XLogSpecUtil.CFG_KEY_TRACE_ATTRSET,
			new String[] {}, new String[] {}, false);
	private SettingsModelFilterString m_eventAttrSet = new SettingsModelFilterString(XLogSpecUtil.CFG_KEY_EVENT_ATTRSET,
			new String[] {}, new String[] {}, false);

	// for the factory, we can return the values later, but now we need to save the
	// values here
	private XFactory factory = XFactoryRegistry.instance().currentDefault();
	// Various "expert" configuration options
	private CSVErrorHandlingMode errorHandlingMode = CSVErrorHandlingMode.OMIT_TRACE_ON_ERROR;
	private CSVEmptyCellHandlingMode emptyCellHandlingMode = CSVEmptyCellHandlingMode.SPARSE;
	
	public Table2XLogConfigModel(String configName) {
		
	}

	public void setErrorHandlingMode(CSVErrorHandlingMode errorHandlingMode) {
		// TODO Auto-generated method stub
		this.errorHandlingMode = errorHandlingMode;
	}

	public CSVErrorHandlingMode getErrorHandlingMode() {
		return errorHandlingMode;
	}

	public void setEmptyCellHandlingMode(CSVEmptyCellHandlingMode emptyCellHandlingMode) {
		// TODO Auto-generated method stub
		this.emptyCellHandlingMode = emptyCellHandlingMode;
	}

	public void setFactory(XFactory factory) {
		// TODO Auto-generated method stub
		this.factory = factory;
	}

	public XFactory getFactory() {
		return factory;
	}

	static Collection<String> createPredefinedFormats() {
		// unique values
		Set<String> formats = new LinkedHashSet<String>();
		formats.add("yyyy-MM-dd'T'HH:mm[:ss[.SSS]]");
		formats.add("yyyy-MM-dd;HH:mm:ss[.SSS][.SS][.S]");
		formats.add("dd.MM.yyyy;HH:mm:ss.S");
		formats.add("yyyy-MM-dd HH:mm:ss.S");
		formats.add("dd.MM.yyyy HH:mm:ss.S");
		formats.add("yyyy-MM-dd'T'HH:mm:ss.SSS");
		// formats.add("yyyy-MM-dd'T'HH:mm[:ss[.SSS]]VV['['zzzz']']");
		// formats.add("yyyy-MM-dd;HH:mm:ssVV");
		// formats.add("yyyy-MM-dd'T'HH:mm:ss.SSSVV");
		// formats.add("yyyy-MM-dd'T'HH:mm:ss.SSSVV'['zzzz']'");
		formats.add("yyyy-MM-dd");
		formats.add("yyyy/dd/MM");
		formats.add("dd.MM.yyyy");
		formats.add("HH:mm[:ss[.SSS]]");
		formats.add("HH:mm:ss");

		return formats;
	}

	public void saveSettings(final NodeSettingsWO settings) {
		m_caseID.saveSettingsTo(settings);
//		m_eventID.saveSettingsTo(settings);
		m_eventClass.saveSettingsTo(settings);
		
		m_lifecycle.saveSettingsTo(settings);
		m_timeStamp.saveSettingsTo(settings);
		m_tsFormat.saveSettingsTo(settings);

		m_traceAttrSet.saveSettingsTo(settings);
		m_eventAttrSet.saveSettingsTo(settings);

		settings.addString(CFG_KEY_XFACTORY, factory.getName());
		settings.addString(CFG_KEY_ERROR_HANDLE_MODE, errorHandlingMode.name());
		settings.addString(CFG_KEY_EMPTY_HANDLE_MODE, emptyCellHandlingMode.name());

	}

	public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_caseID.loadSettingsFrom(settings);
//		m_eventID.loadSettingsFrom(settings);
		m_eventClass.loadSettingsFrom(settings);
		
		m_lifecycle.loadSettingsFrom(settings);

		m_timeStamp.loadSettingsFrom(settings);
		m_tsFormat.loadSettingsFrom(settings);

		m_traceAttrSet.loadSettingsFrom(settings);
		m_eventAttrSet.loadSettingsFrom(settings);

		String fName = settings.getString(CFG_KEY_XFACTORY);

		Set<XFactory> fSet = XFactoryRegistry.instance().getAvailable();
		for (XFactory f : fSet) {
			if (f.getName().equals(fName)) {
				factory = f;
				break;
			}
		}

		String errorName = settings.getString(CFG_KEY_ERROR_HANDLE_MODE);
		errorHandlingMode = CSVConversionConfig.CSVErrorHandlingMode.valueOf(errorName);

		String emptyName = settings.getString(CFG_KEY_EMPTY_HANDLE_MODE);
		emptyCellHandlingMode = CSVConversionConfig.CSVEmptyCellHandlingMode.valueOf(emptyName);

	}

	public SettingsModelString getMCaseID() {
		return m_caseID;
	}

	public void setMCaseID(SettingsModelString m_caseID) {
		this.m_caseID = m_caseID;
	}


	public SettingsModelString getMTimeStamp() {
		return m_timeStamp;
	}

	public void setMStartTime(SettingsModelString timeStamp) {
		this.m_timeStamp = timeStamp;
	}

	public SettingsModelString getMTSFormat() {
		return m_tsFormat;
	}

	public void setMSFormat(SettingsModelString m_tsFormat) {
		this.m_tsFormat = m_tsFormat;
	}

	public SettingsModelFilterString getMTraceAttrSet() {
		return m_traceAttrSet;
	}

	public void setMTraceAttrSet(SettingsModelFilterString m_traceAttrSet) {
		this.m_traceAttrSet = m_traceAttrSet;
	}

	public SettingsModelFilterString getMEventAttrSet() {
		return m_eventAttrSet;
	}

	public void setMEventAttrSet(SettingsModelFilterString m_eventAttrSet) {
		this.m_eventAttrSet = m_eventAttrSet;
	}

	public SettingsModelString getMLifecycle() {
		// TODO Auto-generated method stub
		return m_lifecycle;
	}

	public SettingsModelString getMEventClass() {
		// TODO Auto-generated method stub
		return m_eventClass;
	}

	public void clear() {
		// TODO set the included list to be cleared to adapt to the new connection
		
		
	}
}
