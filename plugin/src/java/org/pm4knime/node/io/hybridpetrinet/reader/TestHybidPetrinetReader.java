package org.pm4knime.node.io.hybridpetrinet.reader;

import java.io.File;

import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.pnml.importing.PnmlImportNet;

public class TestHybidPetrinetReader {

	public static void main(String[] args) {
		final SettingsModelString m_fileName = new SettingsModelString(HybridPetrinetReaderNodeModel.CFG_FILE_NAME, "");
		// PetriNetPortObject m_netPort = new PetriNetPortObject();
		
		//m_fileName.setStringValue("/home/dkf/ProcessMining/datasets/Chapter_1/running-example.pnml");
		m_fileName.setStringValue("/home/dkf/ProcessMining/datasets/plg/simple_experiment/model_04_gdep_07.pnml");
		File file = new File(m_fileName.getStringValue());
        // here we also need to set the PluginContext as the global value, like the flowVariable
        PluginContext context = PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(PnmlImportNet.class);
        
        PnmlImportNet importer = new PnmlImportNet();
        Object[] result = null;
		try {
			// sth about the result goes wrong, we need to check it here
			result = (Object[]) importer.importFile(context, m_fileName.getStringValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		m_netPort.setNet((Petrinet) result[0]);
		m_netPort.setInitMarking((Marking) result[1]);
		m_netPort.setFinalMarking(null);
		*/
	}
}
