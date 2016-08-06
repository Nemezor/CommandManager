package com.nemez.cmdmgr.util;

public class HelpPageCommand {

	public String permission;
	public String usage;
	public String description;
	public String method;
	public Type type;
	
	public HelpPageCommand(String perm, String usage, String description, String method, Type type) {
		this.permission = perm;
		this.usage = usage;
		this.description = description;
		this.method = method;
		this.type = type;
	}
}
