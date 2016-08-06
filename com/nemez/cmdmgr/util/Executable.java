package com.nemez.cmdmgr.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.CommandManager;
import com.nemez.cmdmgr.component.BooleanComponent;
import com.nemez.cmdmgr.component.ByteComponent;
import com.nemez.cmdmgr.component.ConstantComponent;
import com.nemez.cmdmgr.component.DoubleComponent;
import com.nemez.cmdmgr.component.FloatComponent;
import com.nemez.cmdmgr.component.ICommandComponent;
import com.nemez.cmdmgr.component.IntegerComponent;
import com.nemez.cmdmgr.component.LongComponent;
import com.nemez.cmdmgr.component.ShortComponent;
import com.nemez.cmdmgr.component.StringComponent;

public class Executable extends org.bukkit.command.Command {

	private ArrayList<ExecutableDefinition> commands;
	private ArrayList<HelpPageCommand[]> help;
	private String name;
	private JavaPlugin plugin;
	
	public Executable(String name, ArrayList<HelpPageCommand[]> help) {
		super(name);
		this.help = help;
		this.name = name;
		this.commands = new ArrayList<ExecutableDefinition>();
	}
	
	public void register(ArrayList<Method> methods, JavaPlugin plugin, Object methodContainer) {
		for (HelpPageCommand[] page : help) {
			for (HelpPageCommand cmd : page) {
				if (cmd != null) {
					processLine(cmd.usage.split("\\ "), cmd.permission, cmd.method, methods, methodContainer, plugin, cmd.type);
				}
			}
		}
		
		this.plugin = plugin;
		try {
			final Field cmdMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			cmdMap.setAccessible(true);
			CommandMap map = (CommandMap) cmdMap.get(Bukkit.getServer());
			map.register(name, this);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		if (CommandManager.errors) {
			plugin.getLogger().log(Level.WARNING, "There were parser errors, some commands may not function properly!");
			CommandManager.errors = false;
		}
	}
	
	private void processLine(String[] line, String permission, String method, ArrayList<Method> methods, Object methodContainer, JavaPlugin plugin, Type etype) {
		ArrayList<ICommandComponent> command = new ArrayList<ICommandComponent>();
		if (method == null && line[1].equals("help")) {
			command.add(new ConstantComponent("help"));
			IntegerComponent pageID = new IntegerComponent();
			pageID.argName = "page";
			command.add(pageID);
			ExecutableDefinition def = new ExecutableDefinition(command, permission, null, methodContainer, Type.BOTH);
			commands.add(def);
			return;
		}
		HashMap<Integer, ICommandComponent> methodParams = new HashMap<Integer, ICommandComponent>();
		method = method.trim() + " ";
		String[] methodArray = method.split(" ");
		Method target = null;
		
		for (String s : line) {
			if (s.contains("/")) {
				continue;
			}
			if (s.contains(":")) {
				String[] type = s.split(":");
				String paramName = "";
				switch (type[1].substring(0, type[1].length() - 1)) {
				case "i8":
					ByteComponent comp1 = new ByteComponent();
					comp1.argName = type[0].substring(1);
					paramName = comp1.argName;
					command.add(comp1);
					break;
				case "i16":
					ShortComponent comp2 = new ShortComponent();
					comp2.argName = type[0].substring(1);
					paramName = comp2.argName;
					command.add(comp2);
					break;
				case "i32":
					IntegerComponent comp3 = new IntegerComponent();
					comp3.argName = type[0].substring(1);
					paramName = comp3.argName;
					command.add(comp3);
					break;
				case "i64":
					LongComponent comp4 = new LongComponent();
					comp4.argName = type[0].substring(1);
					paramName = comp4.argName;
					command.add(comp4);
					break;
				case "fp32":
					FloatComponent comp5 = new FloatComponent();
					comp5.argName = type[0].substring(1);
					paramName = comp5.argName;
					command.add(comp5);
					break;
				case "fp64":
					DoubleComponent comp6 = new DoubleComponent();
					comp6.argName = type[0].substring(1);
					paramName = comp6.argName;
					command.add(comp6);
					break;
				case "str":
					StringComponent comp7 = new StringComponent();
					comp7.argName = type[0].substring(1);
					paramName = comp7.argName;
					command.add(comp7);
					break;
				case "bool":
					BooleanComponent comp8 = new BooleanComponent();
					comp8.argName = type[0].substring(1);
					paramName = comp8.argName;
					command.add(comp8);
				default:
					return;
				}
				int index = 0;
				for (int i = 1; i < methodArray.length; i++) {
					if (methodArray[i] != null && !methodArray[i].trim().equals("")) {
						if (methodArray[i].trim().equals(paramName)) {
							methodParams.put(index, command.get(command.size() - 1));
						}
						index++;
					}
				}
			}else{
				command.add(new ConstantComponent(s));
			}
		}
		
		for (Method m : methods) {
			Command[] annotations = m.getAnnotationsByType(Command.class);
			if (annotations == null || annotations.length != 1) {
				plugin.getLogger().log(Level.WARNING, "Invalid method (" + methodArray[0] + ")");
				CommandManager.errors = true;
				System.err.println("Method not found! (" + methodArray[0] + ")");
				return;
			}else{
				if (annotations[0].hook().equals(methodArray[0])) {
					Class<?>[] params = m.getParameterTypes();
					if (params.length -1 != methodParams.size()) {
						System.err.println("error again! :D");
						return;
					}else{
						for (int i = 0; i < params.length; i++) {
							if (i == 0) {
								if (params[0] != CommandSender.class) {
									plugin.getLogger().log(Level.WARNING, "Invalid method (" + methodArray[0] + "): First argument is not CommandSender");
									CommandManager.errors = true;
									return;
								}
							}else{
								ICommandComponent comp = methodParams.get(i - 1);
								if (comp instanceof ByteComponent && params[i] == byte.class) {
									
								}else if (comp instanceof ShortComponent && params[i] == short.class) {
									
								}else if (comp instanceof IntegerComponent && params[i] == int.class) {
									
								}else if (comp instanceof LongComponent && params[i] == long.class) {
									
								}else if (comp instanceof FloatComponent && params[i] == float.class) {
									
								}else if (comp instanceof DoubleComponent && params[i] == double.class) {
									
								}else if (comp instanceof StringComponent && params[i] == String.class) {
									
								}else if (comp instanceof BooleanComponent && params[i] == boolean.class) {
									
								}else{
									plugin.getLogger().log(Level.WARNING, "Invalid method (" + methodArray[0] + "): Invalid method arguments");
									CommandManager.errors = true;
									return;
								}
							}
						}
						target = m;
						break;
					}
				}
			}
		}
		if (target == null) {
			plugin.getLogger().log(Level.WARNING, "Invalid method (" + methodArray[0] + "): Method not found");
			CommandManager.errors = true;
			return;
		}
		if (etype == null) {
			etype = Type.BOTH;
		}
		ExecutableDefinition def = new ExecutableDefinition(command, permission, target, methodContainer, etype);
		commands.add(def);
	}
	
	@Override
	public boolean execute(CommandSender sender, String name, String[] args) {
		ArrayList<ExecutableDefinition> defs = new ArrayList<ExecutableDefinition>();
		defs.addAll(commands);
		for (int i = 0; i < args.length; i++) {
			for (int j = 0; j < defs.size(); j++) {
				if (!defs.get(j).valid(i, args[i])) {
					defs.remove(j);
					j--;
				}
			}
		}
		if (args.length == 0 || defs.size() == 0) {
			printPage(sender, 1);
		}else{
			ExecutableDefinition def = defs.get(0);
			for (ExecutableDefinition d : defs) {
				if (d.isHelp() && args[0].equals("help")) {
					try {
						int page = Integer.parseInt(args[1]);
						printPage(sender, page);
					} catch (Exception e) {
						printPage(sender, 1);
					}
					return true;
				}
			}
			if (def.getPermission() != null && !sender.hasPermission(def.getPermission())) {
				sender.sendMessage(CommandManager.noPermissionFormatting + "You do not have permission to execute this command.");
				return true;
			}
			if (def.getExecType() == Type.PLAYER) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(CommandManager.notAllowedFormatting + "Only players are allowed to run this command.");
					return true;
				}
			}else if (def.getExecType() == Type.CONSOLE) {
				if (sender instanceof Player) {
					sender.sendMessage(CommandManager.notAllowedFormatting + "Only console is allowed to run this command.");
					return true;
				}
			}else if (def.getExecType() == Type.NOBODY) {
				sender.sendMessage(CommandManager.notAllowedFormatting + "Nobody can run this command.");
				return true;
			}
			if (def.getLength() != args.length) {
				printPage(sender, 1);
				return true;
			}
			ArrayList<Object> arguments = new ArrayList<Object>();
			for (int i = 0; i < args.length; i++) {
				if (def.isArgument(i)) {
					arguments.add(def.get(i, args[i]));
				}
			}
			if (!def.invoke(arguments, sender, plugin)) {
				printPage(sender, 1);
			}
		}
		return true;
	}
	
	private void printPage(CommandSender sender, int page) {
		page--;
		if (page < 0 || page >= help.size()) {
			sender.sendMessage(CommandManager.helpInvalidPageFormatting + "Non-existant page (" + (page + 1) + ").\nThere are " + help.size() + " pages.");
		}else{
			HelpPageCommand[] pageData = help.get(page);
			sender.sendMessage(CommandManager.helpPageHeaderFormatting + "### Help Page " + (page + 1) + "/" + (help.size()) + " ###");
			for (HelpPageCommand c : pageData) {
				if (c != null && (c.permission == null || sender.hasPermission(c.permission))) {
					sender.sendMessage(CommandManager.helpUsageFormatting + c.usage);
					sender.sendMessage(CommandManager.helpDescriptionFormatting + c.description);
				}
			}
		}
	}
}
