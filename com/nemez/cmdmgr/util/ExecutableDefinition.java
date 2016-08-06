package com.nemez.cmdmgr.util;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import com.nemez.cmdmgr.component.ArgumentComponent;
import com.nemez.cmdmgr.component.ICommandComponent;

public class ExecutableDefinition {

	private ArrayList<ICommandComponent> components;
	private String permission;
	private Method target;
	private Object methodContainer;
	
	public ExecutableDefinition(ArrayList<ICommandComponent> cmd, String perm, Method method, Object methodContainer) {
		this.components = cmd;
		this.permission = perm;
		this.target = method;
		this.methodContainer = methodContainer;
	}
	
	public boolean valid(int index, String arg) {
		if (index < 0 || index >= components.size()) {
			return false;
		}
		return components.get(index).valid(arg);
	}

	public Object get(int index, String arg) {
		if (index < 0 || index >= components.size()) {
			return null;
		}
		return components.get(index).get(arg);
	}
	
	public boolean isArgument(int index) {
		if (index < 0 || index >= components.size()) {
			return false;
		}
		return components.get(index) instanceof ArgumentComponent;
	}
	
	public boolean isHelp() {
		return target == null && components.get(0).valid("help") && components.get(1).getComponentInfo().equals("<page:i32>");
	}
	
	public String getPermission() {
		return permission;
	}
	
	public int getLength() {
		return components.size();
	}
	
	public boolean invoke(ArrayList<Object> args, CommandSender sender) {
		if (target == null) {
			return false;
		}
		Object[] arguments = new Object[args.size() + 1];
		for (int i = 1; i < arguments.length; i++) {
			arguments[i] = args.get(i - 1);
		}
		arguments[0] = sender;
		try {
			if (target.getReturnType() == void.class) {
				target.invoke(methodContainer, arguments);
				return true;
			}else if (target.getReturnType() == boolean.class) {
				return (boolean) target.invoke(methodContainer, arguments);
			}
		} catch (Exception e) {
			System.err.println("M8 you're missing a function...");
			e.printStackTrace();
			return true;
		}
		return false;
	}
}
