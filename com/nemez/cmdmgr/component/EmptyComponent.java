package com.nemez.cmdmgr.component;

public class EmptyComponent extends ArgumentComponent {

	@Override
	public Object get(String input) {
		return "";
	}

	@Override
	public boolean valid(String input) {
		return true;
	}
	
	@Override
	public String getComponentInfo() {
		return "<empty>";
	}
}
