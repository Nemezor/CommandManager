package com.nemez.cmdmgr.component;

public class OptionalComponent extends ArgumentComponent {
	
	@Override
	public Object get(String input) {
		return input.equals(argName);
	}

	@Override
	public boolean valid(String input) {
		return input.equals(argName);
	}

	@Override
	public String getComponentInfo() {
		return "<" + argName + ":flag>";
	}
}
