package com.nemez.cmdmgr.component;

public class IntegerComponent extends ArgumentComponent {

	@Override
	public Object get(String input) {
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean valid(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public String getComponentInfo() {
		return "<" + argName + ":i32>";
	}
}
