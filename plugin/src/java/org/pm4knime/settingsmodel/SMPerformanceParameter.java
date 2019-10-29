package org.pm4knime.settingsmodel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * add more attributes:
 * -- timstamp attribute
 * -- comparison strategy for syn moves: strictly performance or
 *    move on models are assumed to be firing transitions as soon as they are enabled
 * -- the pattern problem 
 *    ++ create new pattern/omega pattern for event classes
 *    ++ map pattern for transitions
 *    
 *  why to use pattern?? Need to look for the codes later!!
 * Too many details there, change goal, to extend the project-maven
 * @author kefang-pads
 * @date 23 Oct. 2019
 *
 */
public class SMPerformanceParameter extends SMAlignmentReplayParameter{

	public final String CKF_KEY_TIMESTAMP = "Time Stamp";
	public final String CKF_KEY_WITH_SYN_MOVE = "Consider Performance In Syn Moves";
	public final String CKF_KEY_WITH_UNRELIABLE_RESULT = "Include Unreliable Replay Results";
	
	// this use to record the attribute name for time stamp
	private SettingsModelString m_timeStamp;
	private SettingsModelBoolean m_withSynMove ;
	private SettingsModelBoolean m_withUnreliableResult ;
	
	
	public SMPerformanceParameter(String configName) {
		super(configName);
		// TODO Auto-generated constructor stub
		m_timeStamp = new SettingsModelString(CKF_KEY_TIMESTAMP, "timestamp");
		m_withSynMove = new SettingsModelBoolean(CKF_KEY_WITH_SYN_MOVE, true);
		m_withUnreliableResult = new SettingsModelBoolean(CKF_KEY_WITH_UNRELIABLE_RESULT, true);
	}

	public SettingsModelString getMTimeStamp() {
		return m_timeStamp;
	}



	public void setMTimeStamp(SettingsModelString m_timeStamp) {
		this.m_timeStamp = m_timeStamp;
	}



	public SettingsModelBoolean isMWithSynMove() {
		return m_withSynMove;
	}


	public SettingsModelBoolean isMWithUnreliableResult() {
		return m_withUnreliableResult;
	}

	public void setMWithSynMove(SettingsModelBoolean m_withSynMove) {
		this.m_withSynMove = m_withSynMove;
	}

	
	@Override
	protected void loadSettingsPure(NodeSettingsRO settings) throws InvalidSettingsException {
		// store the existing value and also the additional parameters
		super.loadSettingsPure(settings);
		
		m_timeStamp.loadSettingsFrom(settings);
		m_withSynMove.loadSettingsFrom(settings);
		m_withUnreliableResult.loadSettingsFrom(settings);
	}
	
	@Override
	protected void saveSettingsPure(NodeSettingsWO settings) {
		// 
		super.saveSettingsPure(settings);
		
		m_timeStamp.saveSettingsTo(settings);
		m_withSynMove.saveSettingsTo(settings);
		m_withUnreliableResult.saveSettingsTo(settings);
	}
}
