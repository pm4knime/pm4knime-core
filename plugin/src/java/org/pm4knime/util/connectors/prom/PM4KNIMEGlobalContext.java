package org.pm4knime.util.connectors.prom;

import java.lang.annotation.Annotation;

import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.connections.impl.ConnectionManagerImpl;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.impl.AbstractGlobalContext;

public class PM4KNIMEGlobalContext extends AbstractGlobalContext {

	private static PM4KNIMEGlobalContext instance = null;

	private static PackageDescriptor pd = null;

	public static PM4KNIMEGlobalContext instance() {
		if (instance == null) {
			// PluginManagerImpl.initialize(PM4KNIMEPluginContext.class);
			PluginManager pluginManager = new PM4KNIMEPluginManager(PM4KNIMEPluginContext.class);
			instance = new PM4KNIMEGlobalContext(pluginManager);
		}
		return instance;
	}

	private final ConnectionManager connMgr;

	private final PM4KNIMEPluginContext context = new PM4KNIMEPluginContext(this, "pm4knime root plugin context");

	private final PluginManager pluginManager;

	private PM4KNIMEGlobalContext(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
		this.connMgr = new ConnectionManagerImpl(pluginManager);
	}

	public PackageDescriptor getPackageDescriptor() {
		if (pd == null) {
			pd = new PM4KNIMEPackageDescriptor();
		}
		return pd;
	}

	@Override
	public ConnectionManager getConnectionManager() {
		return connMgr;
	}

	private ProMFuture<?>[] createProMFutures(Plugin pluginAnn) {
		ProMFuture<?>[] futures = new ProMFuture<?>[pluginAnn.returnTypes().length];
		for (int i = 0; i < pluginAnn.returnTypes().length; i++) {
			futures[i] = new ProMFuture<Object>(pluginAnn.returnTypes()[i], pluginAnn.returnLabels()[i]) {
				@Override
				protected Object doInBackground() throws Exception {
					// NOP
					return null;
				}
			};
		}
		return futures;
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> T findAnnotation(Annotation[] annotations, Class<T> clazz) {
		T result = null;
		for (Annotation a : annotations) {
			if (a.annotationType().equals(clazz)) {
				result = (T) a;
				break;
			}
		}
		if (result == null) {
			// not registered yet, try to do that now...
		}
		return result;
	}

	/**
	 * This method prepares a PluginContext object, which is a child object of the
	 * PluginContext provided by the "PluginContextManager". Basically this method
	 * mimics some of the internal workings of the ProM framework, e.g. setting the
	 * future result objects.
	 * <p>
	 * This method requires that the supplied class contains a plugin variant with
	 * the {@link PluginContext} annotation.
	 * 
	 * @param classContainingProMPlugin the class that contains the ProM plugin code
	 * @return
	 */
	public PluginContext getFutureResultAwarePluginContext(Class<?> classContainingProMPlugin) {
		return getFutureResultAwarePluginContext(classContainingProMPlugin, null);
	}

	public PluginContext getFutureResultAwarePluginContext(Class<?> classContainingProMPlugin, Progress progress) {
		final PluginContext result;
		if (progress != null) {
			result = getPM4KNIMEMainPluginContext()
					.createChildContext("pm4knime_child_context_" + System.currentTimeMillis(), progress);
		} else {
			result = instance.getMainPluginContext()
					.createChildContext("pm4knime_child_context_" + System.currentTimeMillis());
		}
		Plugin pluginAnn = findAnnotation(classContainingProMPlugin.getAnnotations(), Plugin.class);
		// here I have changed to name from class after deleting the PM4KNIMEPluginManager
		System.out.println("class name is " + classContainingProMPlugin.getName());
		PluginDescriptor pd =  ((PM4KNIMEPluginManager) PM4KNIMEGlobalContext.instance().getPluginManager())
				.getPlugin(classContainingProMPlugin);
		PM4KNIMEPluginExecutionResult per = new PM4KNIMEPluginExecutionResult(pluginAnn.returnTypes(),
				pluginAnn.returnLabels(), pd);
		ProMFuture<?>[] futures = createProMFutures(pluginAnn);
		per.setRapidProMFuture(futures);
		result.setFuture(per);
		return result;
	}

	private PM4KNIMEPluginContext getPM4KNIMEMainPluginContext() {
		return (PM4KNIMEPluginContext) instance.getMainPluginContext();
	}

	@Override
	protected PluginContext getMainPluginContext() {
		return context;
	}

	public PluginContext getProgressAwarePluginContext(Progress progress) {
		return getPM4KNIMEMainPluginContext().createChildContext("rprom_child_context_" + System.currentTimeMillis(),
				progress);
	}

	public PluginContext getPluginContext() {
		return getMainPluginContext().createChildContext("rprom_child_context_" + System.currentTimeMillis());
	}

	@Override
	public Class<? extends PluginContext> getPluginContextType() {
		return PM4KNIMEPluginContext.class;
	}

	@Override
	public PluginManager getPluginManager() {
		return pluginManager;
	}

	public PM4KNIMEPluginContext getPM4KNIMEPluginContext() {
		return (PM4KNIMEPluginContext) getMainPluginContext();
	}
	// create new methods to allow context accept the future result without giving plugin names on it
	public PM4KNIMEPluginContext getPM4KNIMEPluginContextWithFutureResult(int len) {
		ProMFuture<?>[] futures = new ProMFuture<?>[len];
		for(int i=0;i<len;i++) {
			futures[i] = new ProMFuture<Object>(Object.class, i+"") {
				@Override
				protected Object doInBackground() throws Exception {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}
		
		return context;
	}

}
