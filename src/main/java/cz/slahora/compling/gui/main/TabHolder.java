package cz.slahora.compling.gui.main;

import java.util.List;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 24.3.14 8:00</dd>
 * </dl>
 */
public interface TabHolder {

	void onNewTab(List<String> id);

	void onTabClose(String id);

	void onTabChange(String id);
}
