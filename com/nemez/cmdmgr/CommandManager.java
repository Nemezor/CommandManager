package com.nemez.cmdmgr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.nemez.cmdmgr.component.ArgumentComponent;
import com.nemez.cmdmgr.component.BooleanComponent;
import com.nemez.cmdmgr.component.ByteComponent;
import com.nemez.cmdmgr.component.ChainComponent;
import com.nemez.cmdmgr.component.ConstantComponent;
import com.nemez.cmdmgr.component.DoubleComponent;
import com.nemez.cmdmgr.component.FloatComponent;
import com.nemez.cmdmgr.component.ICommandComponent;
import com.nemez.cmdmgr.component.IntegerComponent;
import com.nemez.cmdmgr.component.LongComponent;
import com.nemez.cmdmgr.component.ShortComponent;
import com.nemez.cmdmgr.component.StringComponent;
import com.nemez.cmdmgr.util.BranchStack;
import com.nemez.cmdmgr.util.Executable;
import com.nemez.cmdmgr.util.HelpPageCommand;
import com.nemez.cmdmgr.util.Property;
import com.nemez.cmdmgr.util.Type;

/**
 *  Example command.cmd
 *  
 *  command home:
 *  	set [string:name]:
 *  		run home_set name
 *  		help Set a new home
 *  		perm home.set
 *  	del [string:name]:
 *  		run home_del name
 *  		help Delete home\n&CCannot be undone!
 *  		perm home.del
 *  	list:
 *  		run home_list
 *  		help Show all homes
 *  		perm home.list
 *  	[string:name]:
 *  		run home_tp name
 *  		help Teleport to specified home
 *  		perm home.tp
 *
 *  Generated in-game command structure:
 *  (will only show commands the user has permission to execute)
 *  
 *  /home set <name>
 *  /home del <name>
 *  /home list
 *  /home <name>
 *  /home help
 *  
 *   Java code:
 *   
 *   @Command(hook="home_set")
 *   public void executeHomeSet(String name) {
 *     ...
 *   }
 *   
 *   @Command(hook="home_del")
 *   public void executeHomeDelete(String name) {
 *     ...
 *   }
 *   
 *   @Command(hook="home_list")
 *   public void executeHomeList() {
 *     ...
 *   }
 *   
 *   @Command(hook="home_tp")
 *   public void executeHomeTeleport(String name) {
 *     ...
 *   }
 */

public class CommandManager {

	public static boolean debugOutput = false;
	public static boolean errors = false;
	
	public static String helpDescriptionFormatting = "§b";
	public static String helpUsageFormatting = "§6";
	public static String helpPageHeaderFormatting = "§a";
	public static String helpInvalidPageFormatting = "§c";
	public static String noPermissionFormatting = "§c";
	public static String notAllowedFormatting = "§c";
	
	public static boolean registerCommand(String cmdSourceCode, Object commandHandler, JavaPlugin plugin) {
		if (cmdSourceCode == null || commandHandler == null || plugin == null) {
			return false;
		}
		Method[] methods = commandHandler.getClass().getMethods();
		ArrayList<Method> finalMethods = new ArrayList<Method>();
		
		for (Method m : methods) {
			if (m.getAnnotationsByType(Command.class).length > 0 && (m.getModifiers() & Modifier.STATIC) == 0) {
				finalMethods.add(m);
			}
		}
		return parse(cmdSourceCode, finalMethods, plugin, commandHandler);
	}
	
	public static boolean registerCommand(File sourceFile, Object commandHandler, JavaPlugin plugin) {
		StringBuilder src = new StringBuilder();
		String buf = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
			while ((buf = reader.readLine()) != null) {
				src.append(buf);
			}
			reader.close();
		} catch (Exception e) {
			plugin.getLogger().log(Level.WARNING, "Error while loading command file. (" + sourceFile.getAbsolutePath() + ")");
			plugin.getLogger().log(Level.WARNING, e.getCause().toString());
			errors = true;
			return false;
		}
		return registerCommand(src.toString(), commandHandler, plugin);
	}
	
	public static boolean registerCommand(InputStream sourceStream, Object commandHandler, JavaPlugin plugin) {
		StringBuilder src = new StringBuilder();
		String buf = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(sourceStream));
			while ((buf = reader.readLine()) != null) {
				src.append(buf);
			}
			reader.close();
		} catch (Exception e) {
			plugin.getLogger().log(Level.WARNING, "Error while loading command file. (" + sourceStream.toString() + ")");
			plugin.getLogger().log(Level.WARNING, e.getCause().toString());
			errors = true;
			return false;
		}
		return registerCommand(src.toString(), commandHandler, plugin);
	}
	
	private static boolean parse(String source, ArrayList<Method> methods, JavaPlugin plugin, Object methodContainer) {
		char[] chars = source.toCharArray();
		StringBuilder buffer = new StringBuilder();
		String cmdName = null;
		boolean insideType = false;
		boolean gettingName = false;
		char previous = '\0';
		ChainComponent currentChain = new ChainComponent();
		BranchStack stack = new BranchStack();
		ArgumentComponent currentArgComp = null;
		Property currentProp = Property.NONE;
		int bracketCounter = 0;
		int line = 0;
		
		for (int i = 0; i < chars.length; i++) {
			char current = chars[i];
			
			if (current == '\n') {
				line++;
			}
			
			if (current == ':') {
				if (insideType) {
					if (currentArgComp != null) {
						plugin.getLogger().log(Level.WARNING, "Syntax error at line " + line + ": Already defining a type.");
						errors = true;
						return false;
					}else{
						currentArgComp = resolveComponentType(buffer.toString());
						buffer = new StringBuilder();
						if (currentArgComp == null) {
							plugin.getLogger().log(Level.WARNING, "Type error at line " + line + ": Invalid type.");
							errors = true;
							return false;
						}
					}
				}else{
					buffer.append(':');
				}
			}else if (current == ';') {
				if (previous == '\\') {
					buffer.append(';');
				}else{
					if (stack.get() == null) {
						plugin.getLogger().log(Level.WARNING, "Syntax error at line " + line + ": Not in code section.");
						errors = true;
						return false;
					}
					if (currentProp == Property.HELP) {
						stack.get().help = buffer.toString();
					}else if (currentProp == Property.EXECUTE) {
						stack.get().execute = buffer.toString();
					}else if (currentProp == Property.PERMISSION) {
						stack.get().permission = buffer.toString().trim();
					}else if (currentProp == Property.TYPE) {
						stack.get().type = resolveExecutionType(buffer.toString().trim());
						if (stack.get().type == null) {
							plugin.getLogger().log(Level.WARNING, "Attribute error at line " + line + ": Invalid attribute value. (" + buffer.toString().trim() + ").");
							errors = true;
							return false;
						}
					}else{
						plugin.getLogger().log(Level.WARNING, "Attribute error at line " + line + ": Invalid attribute type.");
						errors = true;
						return false;
					}
					currentProp = Property.NONE;
					buffer = new StringBuilder();
				}
			}else if (current == '{') {
				bracketCounter++;
				if (gettingName && cmdName == null) {
					cmdName = buffer.toString().trim();
				}else{
					if (currentArgComp == null) {
						if (buffer.toString().trim().length() > 0) {
							currentChain.append(new ConstantComponent(buffer.toString().trim()));
						}
					}else{
						currentChain.append(currentArgComp);
						currentArgComp = null;
					}
				}
				buffer = new StringBuilder();
				ChainComponent top = stack.get();
				if (top != null) {
					top.append(currentChain);
				}
				stack.push(currentChain);
				currentChain = new ChainComponent();
			}else if (current == '}') {
				bracketCounter--;
				ChainComponent popped = stack.pop();
				if (popped == null) {
					plugin.getLogger().log(Level.WARNING, "Syntax error at line " + line + ": Too many closing brackets.");
					errors = true;
					return false;
				}
				if (bracketCounter == 0) {
					postProcess(cmdName, popped, methods, plugin, methodContainer); // \o/
					buffer = new StringBuilder();
					cmdName = null;
					insideType = false;
					gettingName = false;
					previous = '\0';
					currentChain = new ChainComponent();
					stack = new BranchStack();
					currentArgComp = null;
					currentProp = Property.NONE;
					continue;
				}
				currentChain = new ChainComponent();
			}else if (current == ' ') {
				if (currentProp != Property.NONE) {
					buffer.append(' ');
				}else{
					if (buffer.toString().equals("command") && !gettingName && cmdName == null) {
						gettingName = true;
					}else if (buffer.toString().equals("help")) {
						currentProp = Property.HELP;
					}else if (buffer.toString().equals("run")) {
						currentProp = Property.EXECUTE;
					}else if (buffer.toString().equals("perm")) {
						currentProp = Property.PERMISSION;
					}else if (buffer.toString().equals("type")) {
						currentProp = Property.TYPE;
					}else{
						if (gettingName && cmdName == null) {
							cmdName = buffer.toString().trim();
						}else{
							if (currentArgComp == null) {
								if (buffer.toString().trim().length() > 0) {
									currentChain.append(new ConstantComponent(buffer.toString().trim()));
								}
							}else{
								currentChain.append(currentArgComp);
								currentArgComp = null;
							}
						}
					}
					buffer = new StringBuilder();
				}
			}else if (current == '[') {
				if (currentProp != Property.NONE) {
					buffer.append('[');
				}else if (insideType) {
					plugin.getLogger().log(Level.WARNING, "Syntax error at line " + line + ": Invalid type declaration.");
					errors = true;
					return false;
				}else{
					insideType = true;
				}
			}else if (current == ']') {
				if (currentProp != Property.NONE) {
					buffer.append(']');
				}else if (insideType) {
					insideType = false;
					if (currentArgComp == null) {
						// this should never happen though, it should error out at the top when the type is ""
						plugin.getLogger().log(Level.WARNING, "Type error at line " + line + ": Type has to type?");
						errors = true;
						return false;
					}else{
						currentArgComp.argName = buffer.toString();
						buffer = new StringBuilder();
					}
				}else{
					plugin.getLogger().log(Level.WARNING, "Syntax error at line " + line + ": Not in type declaration.");
					errors = true;
					return false;
				}
			}else if (current == '&' && currentProp == Property.HELP) {
				if (previous == '\\') {
					buffer.append('&');
				}else{
					buffer.append('§');
				}
			}else if (current == 'n' && currentProp == Property.HELP) {
				if (previous == '\\') {
					buffer.append('\n');
				}else{
					buffer.append('n');
				}
			}else if (current == 't' && currentProp == Property.HELP) {
				if (previous == '\\') {
					buffer.append('\t');
				}else{
					buffer.append('t');
				}
			}else if (current == '\\' && currentProp == Property.HELP) {
				if (previous == '\\') {
					buffer.append('\\');
				}
			}else if (current != '\r' && current != '\n' && current != '\t') {
				buffer.append(current);
			}
			previous = current;
		}
		
		return true;
	}
	
	private static ArgumentComponent resolveComponentType(String type) {
		switch (type) {
		case "string":
			return new StringComponent();
		case "int":
		case "integer":
			return new IntegerComponent();
		case "short":
			return new ShortComponent();
		case "long":
			return new LongComponent();
		case "byte":
			return new ByteComponent();
		case "float":
			return new FloatComponent();
		case "double":
			return new DoubleComponent();
		case "bool":
		case "boolean":
			return new BooleanComponent();
		}
		return null;
	}
	
	private static Type resolveExecutionType(String type) {
		switch (type) {
		case "player":
			return Type.PLAYER;
		case "both":
		case "any":
		case "all":
			return Type.BOTH;
		case "server":
		case "console":
			return Type.CONSOLE;
		case "none":
		case "nobody":
			return Type.NOBODY;
		}
		return null;
	}
	
	private static void postProcess(String cmdName, ChainComponent components, ArrayList<Method> methods, JavaPlugin plugin, Object methodContainer) {
		Executable cmd = new Executable(cmdName, constructHelpPages(cmdName, components));
		cmd.register(methods, plugin, methodContainer);
	}
	
	private static ArrayList<HelpPageCommand[]> constructHelpPages(String cmdName, ChainComponent root) {
		String[] rawLines = constructHelpPagesRecursive(root).split("\r");
		ArrayList<HelpPageCommand[]> pages = new ArrayList<HelpPageCommand[]>();
		ArrayList<String> lines = new ArrayList<String>();
		HelpPageCommand[] page = new HelpPageCommand[5];
		
		for (int i = 0; i < rawLines.length; i++) {
			if (rawLines[i].length() > 0 && !rawLines[i].equals("\0null\0null\0null\0null\0")) {
				lines.add(rawLines[i]);
			}
		}
		
		boolean firstPass = true;
		int i;
		for (i = 0; i < lines.size(); i++) {
			if (i % 5 == 0 && !firstPass) {
				pages.add(page);
				page = new HelpPageCommand[5];
			}
			String[] cmd = lines.get(i).split("\0");
			page[i % 5] = new HelpPageCommand(cmd[1], "/" + cmdName + " " + cmd[0], cmd[3], cmd[2], Type.parse(cmd[4]));
			firstPass = false;
		}
		if (i % 5 == 0) {
			pages.add(page);
			page = new HelpPageCommand[5];
		}
		page[i % 5] = new HelpPageCommand(cmdName + ".help", "/" + cmdName + " help <page:i32>", "Shows help.", null, Type.BOTH);
		pages.add(page);
		
		return pages;
	}
	
	private static String constructHelpPagesRecursive(ICommandComponent component) {
		String data = "";
		
		if (component instanceof ChainComponent) {
			ChainComponent comp = (ChainComponent) component;
			ArrayList<String> leaves = new ArrayList<String>();
			String chain = "";
			data += "\r";
			for (ICommandComponent c : comp.getComponents()) {
				String temp = constructHelpPagesRecursive(c);
				if (c instanceof ChainComponent) {
					temp = temp.replaceAll("\r", "\r" + chain);
					leaves.add(temp);
				}else{
					chain += temp;
				}
			}
			data += chain + "\0" + comp.permission + "\0" + comp.execute + "\0" + comp.help + "\0" + Type.get(comp.type) + "\0";
			for (String s : leaves) {
				data += s;
			}
		}else{
			data += component.getComponentInfo() + " ";
		}
		
		return data;
	}
}
