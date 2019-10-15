package org.pm4knime.settingsmodel;

import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * add more attributes:
 * -- timstamp attribute
 * -- comparison strategy for syn moves
 * 
 * Too many details there, change goal, to extend the project-maven
 * @author kefang-pads
 *
 */
public class SMPerformanceParameter extends SMAlignmentReplayParameter{

	final String CKF_KEY_TIMESTAMP = "Time Stamp";
	private SettingsModelString m_timeStamp;
	
	public SMPerformanceParameter(String configName) {
		super(configName);
		// TODO Auto-generated constructor stub
		
	}

}
