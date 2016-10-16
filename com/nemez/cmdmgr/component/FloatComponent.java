package com.nemez.cmdmgr.component;

public class FloatComponent extends ArgumentComponent {

	@Override
	public Object get(String input) {
		try {
			return Float.parseFloat(input);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean valid(String input) {
		try {
			Float.parseFloat(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public String getComponentInfo() {
		return "<" + argName + ":fp32>";
	}
}
