package com.nemez.cmdmgr.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.nemez.cmdmgr.CommandManager;
import com.nemez.cmdmgr.component.ArgumentComponent;
import com.nemez.cmdmgr.component.ICommandComponent;
import com.nemez.cmdmgr.component.OptionalComponent;

public class ExecutableDefinition {

	private ArrayList<ICommandComponent> components;
	private String permission;
	private Method target;
	private Object methodContainer;
	private Type type;
	private ArrayList<Integer> paramLinks;
	
	public ExecutableDefinition(ArrayList<ICommandComponent> cmd, ArrayList<Integer> paramLinks, String perm, Method method, Object methodContainer, Type type) {
		this.components = cmd;
		this.permission = perm;
		this.target = method;
		this.methodContainer = methodContainer;
		this.type = type;
		this.paramLinks = paramLinks;
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
	
	public boolean isOptional(int index) {
		if (index < 0 || index >= components.size()) {
			return false;
		}
		return components.get(index) instanceof OptionalComponent;
	}
	
	public boolean isHelp() {
		return target == null && components.get(0).valid("help") && components.get(1).getComponentInfo().equals("<page:i32>");
	}
	
	public String getPermission() {
		return permission;
	}
	
	public Type getExecType() {
		return type;
	}
	
	public int getLength() {
		return components.size();
	}
	
	public int getLink(int i) {
		if (i < 0 || i > paramLinks.size()) {
			return i;
		}
		return paramLinks.get(i);
	}
	
	public boolean invoke(Object[] args, CommandSender sender, JavaPlugin plugin) {
		if (target == null) {
			return false;
		}
		args[0] = sender;
		try {
			if (target.getReturnType() == void.class) {
				target.invoke(methodContainer, args);
				return true;
			}else if (target.getReturnType() == boolean.class) {
				return (boolean) target.invoke(methodContainer, args);
			}
		} catch (Exception e) {
			sender.sendMessage(CommandManager.helpInvalidPageFormatting + "An internal error occured, please contact the server administrator and/or report a bug.");
			plugin.getLogger().log(Level.WARNING, "Runtime Error: invalid method");
			e.printStackTrace();
			return true;
		}
		return false;
	}
}
