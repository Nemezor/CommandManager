package com.nemez.cmdmgr.component;

public class DoubleComponent extends ArgumentComponent {

	@Override
	public Object get(String input) {
		try {
			return Double.parseDouble(input);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean valid(String input) {
		try {
			Double.parseDouble(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public String getComponentInfo() {
		return "<" + argName + ":fp64>";
	}
}
