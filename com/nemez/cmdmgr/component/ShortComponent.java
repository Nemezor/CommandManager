package com.nemez.cmdmgr.component;

public class ShortComponent extends ArgumentComponent {

	@Override
	public Object get(String input) {
		try {
			return Short.parseShort(input);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean valid(String input) {
		try {
			Short.parseShort(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public String getComponentInfo() {
		return "<" + argName + ":i16>";
	}
}