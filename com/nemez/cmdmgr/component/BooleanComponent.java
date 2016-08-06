package com.nemez.cmdmgr.component;

public class BooleanComponent extends ArgumentComponent {
	
	@Override
	public Object get(String input) {
		try {
			return Boolean.parseBoolean(input);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean valid(String input) {
		try {
			Boolean.parseBoolean(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public String getComponentInfo() {
		return "<" + argName +  ":bool>";
	}
}
