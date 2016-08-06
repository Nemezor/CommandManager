package com.nemez.cmdmgr.util;

import java.util.ArrayList;

import com.nemez.cmdmgr.component.ChainComponent;

public class BranchStack {

	private ArrayList<ChainComponent> components;
	
	public BranchStack() {
		components = new ArrayList<ChainComponent>();
	}
	
	public void push(ChainComponent comp) {
		components.add(comp);
	}
	
	public ChainComponent pop() {
		if (components.size() > 0) {
			ChainComponent toPop = components.remove(components.size() - 1);
			return toPop;
		}
		return null;
	}
	
	public ChainComponent get() {
		if (components.size() > 0) {
			return components.get(components.size() - 1);
		}
		return null;
	}
}
