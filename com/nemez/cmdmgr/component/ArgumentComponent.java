package com.nemez.cmdmgr.component;

public abstract class ArgumentComponent implements ICommandComponent {

	public String argName;

	@Override
	public String argName() {
		return argName;
	}
}
