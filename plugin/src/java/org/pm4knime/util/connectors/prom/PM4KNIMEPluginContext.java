package org.pm4knime.util.connectors.prom;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.processmining.framework.plugin.GlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.impl.AbstractPluginContext;

public class PM4KNIMEPluginContext extends AbstractPluginContext {
	
	private ExecutorService executor;
	private Progress progress;

	public PM4KNIMEPluginContext(GlobalContext context, String label) {
		super(context, label);
		executor = Executors.newCachedThreadPool();
	}

	public PM4KNIMEPluginContext(GlobalContext context, String label, Progress progress) {
		super(context, label);
		this.progress = progress;
		executor = Executors.newCachedThreadPool();
	}

	public PM4KNIMEPluginContext(PM4KNIMEPluginContext context, String label) {
		super(context, label);
		if (context == null) {
			executor = Executors.newCachedThreadPool();
		} else {
			executor = context.getExecutor();
		}
	}

	public void renewExecutor() {
		((ExecutorService) executor).shutdownNow();
		executor = Executors.newCachedThreadPool();
	}

	public void closeExecutor() {
		executor.shutdownNow();
	}

	@Override
	public ExecutorService getExecutor() {
		return executor;
	}

	@Override
	protected PluginContext createTypedChildContext(String label) {
		return new PM4KNIMEPluginContext(this, label);
	}

	public PluginContext createChildContext(String label, Progress progress) {
		return new PM4KNIMEPluginContext(this, label, progress);
	}

	@Override
	public void clear() {
		for (PluginContext c : getChildContexts())
			c.clear();
		super.clear();
	}

	@Override
	public Progress getProgress() {
		if (progress != null) {
			return progress;
		} else {
			return super.getProgress();
		}
}

}
