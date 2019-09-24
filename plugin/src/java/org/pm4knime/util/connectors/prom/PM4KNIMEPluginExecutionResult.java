package org.pm4knime.util.connectors.prom;

import org.processmining.framework.plugin.IncorrectReturnTypeException;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.events.NameChangeListener;
import org.processmining.framework.plugin.impl.PluginExecutionResultImpl;
import org.processmining.framework.util.Cast;

public class PM4KNIMEPluginExecutionResult extends PluginExecutionResultImpl {

	public PM4KNIMEPluginExecutionResult(Class<?>[] returnTypes, String[] returnNames, PluginDescriptor plugin) {
		super(returnTypes, returnNames, plugin);
	}

	public void setRapidProMFuture(Object[] objects) throws IncorrectReturnTypeException {
		Object[] objectArr = getResults();
		for (int i = 0; i < objectArr.length; i++) {
			objectArr[i] = objects[i];
			if (!getType(i).equals(void.class)) {
				if (objectArr[i] == null) {
					continue;
				}
				Class<?> type = objectArr[i].getClass();
				if (objectArr[i] instanceof ProMFuture<?>) {
					type = Cast.<ProMFuture<?>>cast(objects[i]).getReturnType();
				}
				if (!getType(i).isAssignableFrom(type)) {
					throw new IncorrectReturnTypeException(getPlugin().getName(), i, getType(i),
							objectArr[i].getClass());
				}
				if (objects[i] instanceof ProMFuture<?>) {
					final int index = i;
					((ProMFuture<?>) objects[i]).getNameChangeListeners().add(new NameChangeListener() {
						public void nameChanged(String newName) {
							getResultNames()[index] = newName;
						}
					});
				}
			}
		}
	}

}
