package com.nemez.cmdmgr.component;

public class StringComponent extends ArgumentComponent {

	@Override
	public Object get(String input) {
		return input;
	}

	@Override
	public boolean valid(String input) {
		return true;
	}
	
	@Override
	public String getComponentInfo() {
		return "<" + argName + ":str>";
	}
}
