package com.nemez.cmdmgr.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.nemez.cmdmgr.CommandManager;
import com.nemez.cmdmgr.component.ArgumentComponent;
import com.nemez.cmdmgr.component.ICommandComponent;
import com.nemez.cmdmgr.component.OptionalComponent;
import com.nemez.cmdmgr.component.StringComponent;

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
		if (index < 0) {
			return false;
		}
		if (index >= components.size()) {
			if (components.get(components.size() - 1) instanceof StringComponent) {
				StringComponent strComp = (StringComponent) components.get(components.size() - 1);
				if (strComp.infinite) {
					return strComp.valid(arg);
				}else{
					return false;
				}
			}else{
				return false;
			}
		}
		return components.get(index).valid(arg);
	}

	public Object get(int index, String arg) {
		if (index < 0) {
			return null;
		}
		if (index >= components.size()) {
			if (components.get(components.size() - 1) instanceof StringComponent) {
				StringComponent strComp = (StringComponent) components.get(components.size() - 1);
				if (strComp.infinite) {
					return strComp.get(arg);
				}else{
					return null;
				}
			}else{
				return null;
			}
		}
		return components.get(index).get(arg);
	}
	
	public boolean isArgument(int index) {
		if (index < 0) {
			return false;
		}
		if (index >= components.size()) {
			if (components.get(components.size() - 1) instanceof StringComponent) {
				StringComponent strComp = (StringComponent) components.get(components.size() - 1);
				if (strComp.infinite) {
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
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
	
	public int getLength(int argSize) {
		if (components.size() == 0) {
			return 0;
		}
		if (argSize >= components.size()) {
			if (components.get(components.size() - 1) instanceof StringComponent) {
				StringComponent strComp = (StringComponent) components.get(components.size() - 1);
				if (strComp.infinite) {
					return argSize;
				}
			}
		}
		return components.size();
	}
	
	public int getNumOfArgs() {
		int counter = 0;
		for (ICommandComponent c : components) {
			if (c instanceof ArgumentComponent) {
				counter++;
			}
		}
		return counter;
	}
	
	public int getLink(int i) {
		if (i < 0) {
			return i;
		}
		if (i >= paramLinks.size()) {
			if (components.get(components.size() - 1) instanceof StringComponent) {
				StringComponent strComp = (StringComponent) components.get(components.size() - 1);
				if (strComp.infinite) {
					return paramLinks.get(paramLinks.size() - 1);
				}else{
					return i;
				}
			}else{
				return i;
			}
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
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', CommandManager.helpInvalidPageFormatting + "An internal error occured, please contact the server administrator and/or report a bug."));
			plugin.getLogger().log(Level.WARNING, "Runtime Error: invalid method");
			e.printStackTrace();
			return true;
		}
		return false;
	}
}
