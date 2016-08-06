package com.nemez.cmdmgr.util;

public enum Type {

	BOTH, PLAYER, CONSOLE, NOBODY;
	
	public static Type parse(String string) {
		if (string.equals("both")) {
			return BOTH;
		}else if (string.equals("player")) {
			return PLAYER;
		}else if (string.equals("console")) {
			return CONSOLE;
		}else if (string.equals("nobody")) {
			return NOBODY;
		}else{
			return null;
		}
	}
	
	public static String get(Type t) {
		if (t == null) {
			return "null";
		}
		switch (t) {
		case BOTH:
			return "both";
		case PLAYER:
			return "player";
		case CONSOLE:
			return "console";
		case NOBODY:
			return "nobody";
		}
		return "null";
	}
}
