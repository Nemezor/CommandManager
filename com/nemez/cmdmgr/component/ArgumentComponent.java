package com.nemez.cmdmgr.component;

public abstract class ArgumentComponent implements ICommandComponent {

	public String argName;
	public int position;

	@Override
	public String argName() {
		return argName;
	}
}
