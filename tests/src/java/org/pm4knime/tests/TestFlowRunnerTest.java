package org.pm4knime.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.workflow.UnsupportedWorkflowVersionException;
import org.knime.core.util.LockFailedException;
import org.knime.testing.core.TestrunConfiguration;
import org.knime.testing.core.ng.TestflowRunnerApplication;

/** 
 * this class is referred to the class under link 
 * https://github.com/3D-e-Chem/knime-testflow/blob/master/tests/src/java/nl/esciencecenter/e3dchem/knime/testing/TestFlowRunnerTest.java
 * @author kefang-pads
 *
 */
public class TestFlowRunnerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void test_unsupported_loadsaveload() {
		ErrorCollector collector = new ErrorCollector();
		TestrunConfiguration runConfiguration = new TestrunConfiguration();
		runConfiguration.setLoadSaveLoad(true);

		thrown.expect(UnsupportedOperationException.class);
		thrown.expectMessage("LoadSaveLoad is not supported");
		new TestFlowRunner(collector, runConfiguration);
	}

	private class FakeErrorCollector extends ErrorCollector {
		public List<String> messages = new ArrayList<String>();

		@Override
		public void addError(Throwable error) {
			messages.add(error.getMessage());
		}
	}

	@Test
	public void test_workflow_defaultchecks_noerrors() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		File workflow = new File("src/knime/gold");
		List<String> expected = new ArrayList<String>();

		assertWorkflowErrors(workflow, expected);
	}

	@Test
	public void test_workflow_allchecksenabled_noerrors() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		TestrunConfiguration runConfiguration = new TestrunConfiguration();
		runConfiguration.setCheckForLoadWarnings(true);
		runConfiguration.setCheckLogMessages(true);
		runConfiguration.setCheckMemoryLeaks(true);
		runConfiguration.setAllowedMemoryIncrease(10485760);
		runConfiguration.setCheckNodeMessages(true);
		runConfiguration.setCloseWorkflowAfterTest(true);
		runConfiguration.setReportDeprecatedNodes(true);
		runConfiguration.setTestDialogs(true);
		runConfiguration.setTestViews(true);
		runConfiguration.setLoadSaveLoad(false);
		File workflow = new File("src/knime/gold");
		List<String> expected = new ArrayList<String>();

		assertWorkflowErrors(runConfiguration, workflow, expected);
	}

	@Test
	public void test_workflow_2messages() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		File workflow = new File("src/knime/messages");
		List<String> expected = new ArrayList<String>();
		expected.add(
				"Node 'Table Creator 0:2' has unexpected warning message: expected 'This is not the warning message you are looking for.', got 'Node created an empty data table.'");
		expected.add("Expected WARN log message 'This is not the warning message you are looking for.' not found");

		assertWorkflowErrors(workflow, expected);
	}

	@Test
	public void test_workflow_ignore2messages() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		TestrunConfiguration runConfiguration = new TestrunConfiguration();
		runConfiguration.setCheckLogMessages(false);
		runConfiguration.setCheckNodeMessages(false);
		runConfiguration.setLoadSaveLoad(false);
		File workflow = new File("src/knime/messages");
		List<String> expected = new ArrayList<String>();

		assertWorkflowErrors(runConfiguration, workflow, expected);
	}

	private void assertWorkflowErrors(TestrunConfiguration runConfiguration, File workflow, List<String> expected)
			throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		FakeErrorCollector collector = new FakeErrorCollector();
		TestFlowRunner runner = new TestFlowRunner(collector, runConfiguration);

		runner.runTestWorkflow(workflow);

		assertEquals(expected, collector.messages);
	}

	private void assertWorkflowErrors(File workflow, List<String> expected)
			throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		TestrunConfiguration runConfiguration = new TestrunConfiguration();
		runConfiguration.setLoadSaveLoad(false);
		assertWorkflowErrors(runConfiguration, workflow, expected);
	}
}
