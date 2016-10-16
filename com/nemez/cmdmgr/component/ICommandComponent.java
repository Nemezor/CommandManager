package com.nemez.cmdmgr.component;

public interface ICommandComponent {

	public Object get(String input);
	public boolean valid(String input);
	public String argName();
	public String getComponentInfo();
}
