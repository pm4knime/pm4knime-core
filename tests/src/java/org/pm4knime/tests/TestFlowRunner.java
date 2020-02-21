package org.pm4knime.tests;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.rules.ErrorCollector;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.workflow.UnsupportedWorkflowVersionException;
import org.knime.core.util.LockFailedException;
import org.knime.testing.core.TestrunConfiguration;
import org.knime.testing.core.ng.TestflowRunnerApplication;
import org.knime.testing.core.ng.WorkflowTestContext;
import org.knime.testing.core.ng.WorkflowTestSuite;
/**
 * first try to integrate the testing codes. It failed.. So change to the KNIME Server.
 * @author kefang-pads
 *
 */
public class TestFlowRunner {
	private ErrorCollector collector = new ErrorCollector();
	private TestrunConfiguration m_runConfiguration;

	public TestFlowRunner(ErrorCollector collector, TestrunConfiguration runConfiguration) {
		super();
		this.collector = collector;
		this.m_runConfiguration = runConfiguration;
		if (m_runConfiguration.isLoadSaveLoad()) {
			throw new UnsupportedOperationException("LoadSaveLoad is not supported");
		}
	}

	public void runTestWorkflow(File workflowDir) throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		File testcaseRoot = workflowDir;
		String workflowName = workflowDir.getName();
		IProgressMonitor monitor = null;

		WorkflowTestContext m_context = new WorkflowTestContext(m_runConfiguration);
		TestflowRunnerApplication app = new TestflowRunnerApplication();
		WorkflowTestSuite suit = new WorkflowTestSuite(testcaseRoot, testcaseRoot, m_runConfiguration, monitor);
		suit.aboutToStart();
		
	}

}
