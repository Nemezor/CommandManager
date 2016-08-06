package com.nemez.cmdmgr.component;

public class ConstantComponent implements ICommandComponent {

	private String component;
	
	public ConstantComponent(String comp) {
		component = comp;
	}
	
	@Override
	public Object get(String input) {
		if (input.equals(component)) {
			return input;
		}
		return null;
	}

	@Override
	public boolean valid(String input) {
		return input.equals(component);
	}

	@Override
	public String argName() {
		return null;
	}
	
	@Override
	public String getComponentInfo() {
		return component;
	}
}
