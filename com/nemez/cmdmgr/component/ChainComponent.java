package com.nemez.cmdmgr.component;

import java.util.ArrayList;

import com.nemez.cmdmgr.util.Type;

public class ChainComponent implements ICommandComponent {

	private ArrayList<ICommandComponent> components;
	public String permission;
	public String help;
	public String execute;
	public Type type;
	
	public ChainComponent() {
		components = new ArrayList<ICommandComponent>();
	}
	
	public void append(ICommandComponent comp) {
		components.add(comp);
	}

	@Override
	public Object get(String input) {
		return components;
	}

	@Override
	public boolean valid(String input) {
		return true;
	}

	@Override
	public String argName() {
		return null;
	}
	
	@Override
	public String getComponentInfo() {
		return "chain[" + components.size() + "]";
	}
	
	public int capacity() {
		return components.size();
	}
	
	public ArrayList<ICommandComponent> getComponents() {
		return components;
	}
}
