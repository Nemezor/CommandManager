package com.nemez.cmdmgr.component;

public class LongComponent extends ArgumentComponent {

	@Override
	public Object get(String input) {
		try {
			return Long.parseLong(input);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean valid(String input) {
		try {
			Long.parseLong(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public String getComponentInfo() {
		return "<" + argName + ":i64>";
	}
}