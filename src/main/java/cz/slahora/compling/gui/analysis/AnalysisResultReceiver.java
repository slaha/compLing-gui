package cz.slahora.compling.gui.analysis;

import cz.slahora.compling.gui.model.WorkingText;

import java.util.Map;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 25.3.14 21:15</dd>
 * </dl>
 */
public interface AnalysisResultReceiver {
	boolean canReceive(Class<?> aClass);

	void send(Map<WorkingText, ?> results) throws TypeNotSupportedException;

	public static class TypeNotSupportedException extends RuntimeException {


		public TypeNotSupportedException(Class<?> aClass) {
			super("Class " + aClass +" is not supported by this AnalysisResultReceiver.");
		}
	}
}
