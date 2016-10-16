package com.nemez.cmdmgr.component;

public class BooleanComponent extends ArgumentComponent {
	
	@Override
	public Object get(String input) {
		if (input.toLowerCase().equals("true") || input.toLowerCase().equals("yes")) {
			return true;
		}
		return false;
	}

	@Override
	public boolean valid(String input) {
		if (input.toLowerCase().equals("true") || input.toLowerCase().equals("false") || input.toLowerCase().equals("yes") || input.toLowerCase().equals("no")) {
			return true;
		}
		return false;
	}
	
	@Override
	public String getComponentInfo() {
		return "<" + argName +  ":bool>";
	}
}
