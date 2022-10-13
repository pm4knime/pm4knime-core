package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.XLogUtil;
import org.pm4knime.util.ReplayerUtil;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.ReplayerUtilTable;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClasses;


import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * this class serializes and deserializes the manifest class, in order to
 * provide PerformanceCheckerNodeView after reloading The methods here will be
 * used in PerformanceCheckerNodeModel. But to avoid the portobject load and
 * save process, we use this easy way for it
 * 
 * To use the method for log and net, change this class package!!
 * 
 * @author kefang-pads
 *
 */
public class ManifestWithSerializerTable {
	
	// the data is given for
	private final static String CFG_LOG_FILENAME = "Log file";
	private final static String CFG_ANET_FILENAME = "Accepting Petri net file";
	private final static String CFG_MODEL_FILE = "Model Content file";
	private final static String CFG_PRIMITIVE_MODEL = "Content Model for primitive values";
	
	private final static String CFG_PRIMITIVE_KEY_CASEPTR = "Case pointer";
	private final static String CFG_PRIMITIVE_KEY_CASEREALIABILITY = "Case reliability";
	private final static String CFG_PRIMITIVE_KEY_FITNESSSTATS = "Fitness Statistics";
	private final static String CFG_PRIMITIVE_KEY_INFO = "Info";
	private static final String CFG_PRIMITIVE_KEY_MMCOST = "Move Model Cost";
	private static final String CFG_PRIMITIVE_KEY_MPID = "Manifest2Pattern ID";
	private static final String CFG_IDX2FITNESSSTATS_KEYS = "Index2FitnessStats Keys";
	private static final String CFG_IDX2FITNESSSTATS_VALUES = "Index2FitnessStats Values";
	private static final String CFG_TRANS2IDX_KEYS = "Transition2Index Keys";
	private static final String CFG_TRANS2IDX_VALUES = "Transition2Index Values";
	private static final String CFG_MLCOST_KEYS = "Move Log Cost Keys";
	private static final String CFG_MLCOST_VALUES = "Move Log Cost Values";
	private static final String CFG_TRANSCLASS_KEYS = "Transclass Keys";
	private static final String CFG_TRANSCLASS_VALUES = "Transclass Values";
	private static final String CFG_EVENTCLASS_KEYS = "Eventclass Keys";
	private static final String CFG_EVENTCLASS_VALUES = "Eventclass Values";
	private static final String CFG_PATTERNS = "Patterns";
	private static final String CFG_PATTERNS_IDX = "Pattern Index";
	private static final String CFG_EVENT_CLASSIFIER = "Event Classifier";
	
	
	public static void saveTo(ManifestEvClassPatternTable manifest, File internDir, ExecutionMonitor exec) throws IOException, CanceledExecutionException {
		// log, use log serialize method for it
		File logFile = new File(internDir, CFG_LOG_FILENAME);
		TableEventLog log = manifest.getLog();		
		FileOutputStream logOut = new FileOutputStream(logFile);
		XSerializer serializer = new XesXmlSerializer();
//		try {
//			serializer.serialize(log, logOut);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// the net + and markings with them...[convert them into the PetrinetPO and use
		// it to serialize, change the models of ResetInhibitorNetImpl to Net
		// for creating AcceptingPetriNet 
		AcceptingPetriNet anet = new AcceptingPetriNetImpl((Petrinet) manifest.getNet(), 
				manifest.getInitMarking(), manifest.getFinalMarkings());
		File anetFile = new File(internDir, CFG_ANET_FILENAME);
		FileOutputStream netOut = new FileOutputStream(anetFile);
		PetriNetUtil.exportToStream(anet, netOut);
		
		
		// other primitive data types
		ModelContent modelContent = new ModelContent(CFG_PRIMITIVE_MODEL);
		// save the event classifier
		String classifier = manifest.getEvClassifier();
		// not know if it is attribute or normal classifier
		String clfName = classifier;
		modelContent.addString(CFG_EVENT_CLASSIFIER, clfName);
		
		// casePtr points to the index of manifest information, given caseID
		modelContent.addIntArray(CFG_PRIMITIVE_KEY_CASEPTR, manifest.getCasePointers());
		modelContent.addBooleanArray(CFG_PRIMITIVE_KEY_CASEREALIABILITY, manifest.getCaseReliability());
		modelContent.addDoubleArray(CFG_PRIMITIVE_KEY_FITNESSSTATS, manifest.getFitnessStats());
		modelContent.addIntArray(CFG_PRIMITIVE_KEY_INFO, manifest.getInfo());
		
		modelContent.addIntArray(CFG_PRIMITIVE_KEY_MMCOST, manifest.getMoveModelCost());
		modelContent.addIntArray(CFG_PRIMITIVE_KEY_MPID, manifest.getManifest2PatternID());
		int idx = 0;
		// manifest.getIndex2FitnessStats(), make sure that they are in order
		int[] ifKeys = new int[manifest.getIndex2FitnessStats().size()];
		int[] ifValues = new int[manifest.getIndex2FitnessStats().size()];
		for(int key: manifest.getIndex2FitnessStats().keys()) {
			ifKeys[idx] = key;
			ifValues[idx] = manifest.getIndex2FitnessStats().get(key);
			idx++;
		}
		modelContent.addIntArray(CFG_IDX2FITNESSSTATS_KEYS, ifKeys);
		modelContent.addIntArray(CFG_IDX2FITNESSSTATS_VALUES, ifValues);
		
		// with transitions and event classes
		// 1. transitionArray to store the value...
		int  tLength = manifest.getTransArr().length ;
		
		Transition[] trans = manifest.getTransArr();
		String[] transNames = new String[tLength];
		int[] tIdx = new int[tLength];
		// how to save it here?? we need the transition Ids
		idx=0;
		for(Transition t : trans) {
			transNames[idx] = t.getLabel();
			// get its idx for this
			tIdx[idx] = manifest.getTrans2idx().get(t);
			idx++;
		}
		modelContent.addStringArray(CFG_TRANS2IDX_KEYS, transNames);
		modelContent.addIntArray(CFG_TRANS2IDX_VALUES, tIdx);
		
		// save the event class and its log cost
		idx = 0;
		int eLength = manifest.getMoveLogCost().size();
		String[] eventNames = new String[eLength];
		int[] eIdx = new int[eLength];
		
		for(String ecls : manifest.getMoveLogCost().keySet()) {
			eventNames[idx] = ecls;
			eIdx[idx] = manifest.getMoveLogCost().get(ecls);
			idx++;
		}
		modelContent.addStringArray(CFG_MLCOST_KEYS, eventNames);
		modelContent.addIntArray(CFG_MLCOST_VALUES, eIdx);
		
		// first not consider it, if there is exception, then implement it
		TransClass2PatternMapTable tpMap = manifest.getTransClass2PatternMap();
		// transitions and event class they are the same 
		idx = 0;
		String[] tcNames = new String[tpMap.getTransClass2Enc().size()];
		short[] tcIdx = new short[tpMap.getTransClass2Enc().size()];
		// need to save the event classifier, the name and classifier name 
		for(TransClass tc : tpMap.getTransClass2Enc().keySet()) {
			tcNames[idx] = tc.getId();
			tcIdx[idx] = tpMap.getTransClassEncFor(tc);
			idx++;
		}
		modelContent.addStringArray(CFG_TRANSCLASS_KEYS, tcNames);
		modelContent.addShortArray(CFG_TRANSCLASS_VALUES, tcIdx);
		
		
		idx = 0;
		String[] ecNames = new String[tpMap.getEvClass2Enc().size()];
		short[] ecIdx = new short[tpMap.getEvClass2Enc().size()];
		// need to save the event classifier, the name and classifier name 
		for(String ec : tpMap.getEvClass2Enc().keySet()) {
			ecNames[idx] = ec;
			ecIdx[idx] = tpMap.getEvClassEncFor(ec);
			idx++;
		}
		modelContent.addStringArray(CFG_EVENTCLASS_KEYS, ecNames);
		modelContent.addShortArray(CFG_EVENTCLASS_VALUES, ecIdx);
		
		modelContent.addShortArray(CFG_PATTERNS, tpMap.getPatterns());
		modelContent.addShortArray(CFG_PATTERNS_IDX, tpMap.getPatternID2Idx());
		
		// set the trans class together?? Or we use the transitions right?
		File mcFile = new File(internDir, CFG_MODEL_FILE);
	    FileOutputStream mcOut = new FileOutputStream(mcFile);
	    modelContent.saveToXML(mcOut);
	}

	
	public static ManifestEvClassPatternTable loadFrom(File internDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException, InvalidSettingsException {
		
		File logFile = new File(internDir, CFG_LOG_FILENAME);
		FileInputStream logIn = new FileInputStream(logFile);
		TableEventLog log = null; //= XLogUtil.loadLog(logIn);
		
		File anetFile = new File(internDir, CFG_ANET_FILENAME);
		FileInputStream anetIn = new FileInputStream(anetFile);
		AcceptingPetriNet anet = PetriNetUtil.importFromStream(anetIn);
		
		
		File mcFile = new File(internDir, CFG_MODEL_FILE);
        FileInputStream mcIn = new FileInputStream(mcFile);
        ModelContentRO modelContent = ModelContent.loadFromXML(mcIn);
        
      
		// not know if it is attribute or normal classifier
		String clfName = modelContent.getString(CFG_EVENT_CLASSIFIER);
		// if it includes the precix, or not. Need to adjust it
        String classifier = clfName;
        
        // read from modelContent and get the values
        int[] caseptr = modelContent.getIntArray(CFG_PRIMITIVE_KEY_CASEPTR);
        boolean[] caseReliability = modelContent.getBooleanArray(CFG_PRIMITIVE_KEY_CASEREALIABILITY);
        
        double[] fitnessStats = modelContent.getDoubleArray(CFG_PRIMITIVE_KEY_FITNESSSTATS); 
        int[] info = modelContent.getIntArray(CFG_PRIMITIVE_KEY_INFO);
        
        int[] mmCost = modelContent.getIntArray(CFG_PRIMITIVE_KEY_MMCOST);
        int[] mpID = modelContent.getIntArray(CFG_PRIMITIVE_KEY_MPID);
       
        int idx = 0;
        //  manifest.setIndex2FitnessStats()
        int[] ifKeys = modelContent.getIntArray(CFG_IDX2FITNESSSTATS_KEYS);
        int[] ifValues = modelContent.getIntArray(CFG_IDX2FITNESSSTATS_VALUES);
		TIntIntMap index2FitnessStats = new TIntIntHashMap();
        for(; idx < ifKeys.length; idx++) {
        	index2FitnessStats.put(ifKeys[idx], ifValues[idx]);
        }
        
        // build transitions array and move model cost
        String[] transNames = modelContent.getStringArray(CFG_TRANS2IDX_KEYS);
		int[] tIdx = modelContent.getIntArray(CFG_TRANS2IDX_VALUES);
		Transition[] transArr = new Transition[transNames.length];
		TObjectIntMap<Transition> trans2idx = new TObjectIntHashMap<Transition>(transNames.length);
		for(idx = 0; idx <transArr.length; idx++ ) {
			String name = transNames[idx];
			// get the transition from anet 
			Transition t = PetriNetUtil.findTransition(name, anet.getNet().getTransitions());
			transArr[idx] = t;
			trans2idx.put(t, tIdx[idx]);
		}
		

		// build move log cost set
		// have the event classes from the log?? How to we get it?
		String[] eventNames = modelContent.getStringArray(CFG_MLCOST_KEYS);
		int[] eIdx = modelContent.getIntArray(CFG_MLCOST_VALUES);
		// to get the event classifier.. So at first, we need to save the event classifier here
		
		
//		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, classifier);
//		Collection<S> eventClasses = logInfo.getEventClasses().getClasses();
		String[] evClassEnc = log.getActivties();
		Map<String, Integer> mapEvClass2Cost = new HashMap();
		for(idx = 0; idx <eventNames.length; idx++ ) {
			String name = eventNames[idx];
			String ecls = name;
			evClassEnc[idx] = ecls;
			mapEvClass2Cost.put(ecls, eIdx[idx]);
		}
		
		
		// build transition class to enc
		String[] tcNames = modelContent.getStringArray(CFG_TRANSCLASS_KEYS);
		short[] tcIdx = modelContent.getShortArray(CFG_TRANSCLASS_VALUES);
		TransClasses tcClasses = new TransClasses(anet.getNet());
		TransClass[] transClassEnc = new TransClass[tcClasses.getTransClasses().size()];
		Map<TransClass, Short> transClass2Enc = new HashMap();
		for(idx = 0; idx< tcNames.length; idx++) {
			String name = tcNames[idx];
			// get the transition class from its name
			TransClass tc = PetriNetUtil.findTransClass(name, tcClasses);
			transClassEnc[idx] = tc;
			transClass2Enc.put(tc, tcIdx[idx]);
		}
		
		
		String[] ecNames = modelContent.getStringArray(CFG_EVENTCLASS_KEYS);
		short[] ecIdx = modelContent.getShortArray(CFG_EVENTCLASS_VALUES);
		Map<String, Short> evClass2Enc = new HashMap();
		for(idx = 0; idx< ecNames.length; idx++) {
			String name = ecNames[idx];
			// get the transition class from its name
			String ecls = name;
			evClass2Enc.put(ecls, ecIdx[idx]);
		}
		
		short[] patterns = modelContent.getShortArray(CFG_PATTERNS);
		short[] ptIdx = modelContent.getShortArray(CFG_PATTERNS_IDX);
		// make the constructor method visible and reassign them  
		TransClass2PatternMapTable transClass2PatternMap = new TransClass2PatternMapTable();
		transClass2PatternMap.setEvClassEnc(evClassEnc);
		transClass2PatternMap.setEvClass2Enc(evClass2Enc);
		transClass2PatternMap.setTransClasses(tcClasses);
		transClass2PatternMap.setTransClassEnc(transClassEnc);
		transClass2PatternMap.setTransClass2Enc(transClass2Enc);
		transClass2PatternMap.setPatterns(patterns);
		transClass2PatternMap.setPatternID2Idx(ptIdx);
		
		ManifestEvClassPatternTable manifest = new ManifestEvClassPatternTable(anet.getNet(), anet.getInitialMarking(), anet.getFinalMarkings().toArray(new Marking[0]),
				log, transArr, trans2idx, caseptr, caseReliability, info, transClass2PatternMap, index2FitnessStats, 
				mpID, fitnessStats);
		manifest.getTransClass2PatternMap().setEvClassifier(classifier);
		manifest.setMoveModelCost(mmCost);
		manifest.setMoveLogCost(mapEvClass2Cost);
		return manifest;
	}
	
	
}
