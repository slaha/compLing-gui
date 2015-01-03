package cz.slahora.compling.gui;

/**
 *
 * Interface for common app methods
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 22.3.14 11:36</dd>
 * </dl>
 */
public interface AppContext {

	void exit(int code);

	void settingsChanged();
}
