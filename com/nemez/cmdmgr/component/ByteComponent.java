package com.nemez.cmdmgr.component;

public class ByteComponent extends ArgumentComponent {
	
	@Override
	public Object get(String input) {
		try {
			return Byte.parseByte(input);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean valid(String input) {
		try {
			Byte.parseByte(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public String getComponentInfo() {
		return "<" + argName +  ":i8>";
	}
}
