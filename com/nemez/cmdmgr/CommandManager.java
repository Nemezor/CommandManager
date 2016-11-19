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
import com.nemez.cmdmgr.component.EmptyComponent;
import com.nemez.cmdmgr.component.FloatComponent;
import com.nemez.cmdmgr.component.ICommandComponent;
import com.nemez.cmdmgr.component.IntegerComponent;
import com.nemez.cmdmgr.component.LongComponent;
import com.nemez.cmdmgr.component.OptionalComponent;
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

	/* Debugging toggle to generate help pages with types, e.g: str, i8, i32, fp64, etc. */
	public static boolean debugHelpMenu = false;
	/* Internal boolean to keep track of errors during resolving */
	public static boolean errors = false;

	/* Switches for color and formatting in the built-in help message and pagination text */
	public static String helpDescriptionFormatting = "&b";
	public static String helpUsageFormatting = "&6";
	public static String helpPageHeaderFormatting = "&a";
	public static String helpInvalidPageFormatting = "&c";
	public static String noPermissionFormatting = "&c";
	public static String notAllowedFormatting = "&c";
	
	/**
	 * Registers a command from a String of source code
	 * 
	 * @param cmdSourceCode source code
	 * @param commandHandler instance of a class where your java functions are located
	 * @param plugin your plugin class
	 * @return success - if command was processed and registered successfully
	 */
	public static boolean registerCommand(String cmdSourceCode, Object commandHandler, JavaPlugin plugin) {
		if (cmdSourceCode == null || commandHandler == null || plugin == null) {
			return false;
		}
		/* get the class definition of the 'commandHandler' and get its functions */
		Method[] methods = commandHandler.getClass().getMethods();
		ArrayList<Method> finalMethods = new ArrayList<Method>();

		/* extract all the functions annotated with @Command that are not static */
		for (Method m : methods) {
			if (m.getAnnotationsByType(Command.class).length > 0 && (m.getModifiers() & Modifier.STATIC) == 0) {
				finalMethods.add(m);
			}
		}
		return parse(cmdSourceCode, finalMethods, plugin, commandHandler);
	}
	
	/**
	 * Registers a command from a source File
	 * 
	 * @param sourceFile file containing source code
	 * @param commandHandler instance of a class where your java functions are located
	 * @param plugin your plugin class
	 * @return success - if command was processed and registered successfully
	 */
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
	
	/**
	 * Registers a command from an InputStream
	 * 
	 * @param sourceStream input stream containing source code
	 * @param commandHandler instance of a class where your java functions are located
	 * @param plugin your plugin class
	 * @return success - if command was processed and registered successfully
	 */
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
	
	/**
	 * Parses the source code into an abstract command syntax
	 * 
	 * @param source String containing command source code
	 * @param methods ArrayList of methods gathered from the plugin's handler class
	 * @param plugin plugin to register commands as
	 * @param methodContainer class containing method handles
	 * @return success - if command parsing and registration was successful
	 */
	private static boolean parse(String source, ArrayList<Method> methods, JavaPlugin plugin, Object methodContainer) {
		/* source code */
		char[] chars = source.toCharArray();
		/* temporary buffer */
		StringBuilder buffer = new StringBuilder();
		/* name of the command we are parsing */
		String cmdName = null;
		/* if we are currently gathering chars from a type definition */
		boolean insideType = false;
		/* if we are currently gathering chars from the command name */
		boolean gettingName = false;
		/* the previous char, used for backslash escaping */
		char previous = '\0';
		/* the current 'array' of sub-commands we are parsing */
		ChainComponent currentChain = new ChainComponent();
		/* storage for the current sub-command if it branches into more sub-commands */
		BranchStack stack = new BranchStack();
		/* the current argument we are parsing */
		ArgumentComponent currentArgComp = null;
		/* the current property of the sub-command we are getting */
		Property currentProp = Property.NONE;
		/* a counter for how deep in the stack we are, used to figure out if we exited a sub-command or the master command */
		int bracketCounter = 0;
		/* line counter */
		int line = 0;
		// buffer for '...' and '"' properties of string types
		StringBuilder sideBuffer = new StringBuilder();

		/* iterate over all characters */
		for (int i = 0; i < chars.length; i++) {
			/* get current char */
			char current = chars[i];

			/* increment line counter */
			if (current == '\n') {
				line++;
			}

			/* current is a colon, we just switched from arguments 'type' to its 'name' */
			/* <type:name> */
			/*      ^      */
			if (current == ':') {
				/* are we inside a type? */
				if (insideType) {
					/* are we already defining an argument? */
					if (currentArgComp != null) {
						/* yes, we are, throw an error */
						plugin.getLogger().log(Level.WARNING, "Syntax error at line " + line + ": Already defining a type.");
						errors = true;
						return false;
					}else{
						/* okay, resolve what type this is */
						currentArgComp = resolveComponentType(buffer.toString());
						buffer = new StringBuilder();
						/* type didn't fit any definition, throw an error */
						if (currentArgComp == null) {
							plugin.getLogger().log(Level.WARNING, "Type error at line " + line + ": Invalid type.");
							errors = true;
							return false;
						}
					}
				}else{
					/* not inside a type, probably just a string in the help property */
					buffer.append(':');
				}
			/* current is a semicolon, we just finished a property line */
			/* help this is an example; */
			/*                        ^ */
			}else if (current == ';') {
				/* semicolon is bashslash escaped, treat it as a normal character */
				if (previous == '\\') {
					buffer.append(';');
				}else{
					/* there is nothing on the stack, we are defining properties of 'nothing', throw an error */
					if (stack.get() == null) {
						plugin.getLogger().log(Level.WARNING, "Syntax error at line " + line + ": Not in code section.");
						errors = true;
						return false;
					}
					/* we are defining the 'help' property, set it to what we just gathered */
					if (currentProp == Property.HELP) {
						stack.get().help = buffer.toString();
					/* same as above, except its the function to run */
					}else if (currentProp == Property.EXECUTE) {
						stack.get().execute = buffer.toString();
					/* same again, but with the permission, and as that should not contain spaces, trim it */
					}else if (currentProp == Property.PERMISSION) {
						stack.get().permission = buffer.toString().trim();
					/* execution type, check if its a valid one and set it */
					}else if (currentProp == Property.TYPE) {
						stack.get().type = resolveExecutionType(buffer.toString().trim());
						/* not a valid type, throw an error */
						if (stack.get().type == null) {
							plugin.getLogger().log(Level.WARNING, "Attribute error at line " + line + ": Invalid attribute value. (" + buffer.toString().trim() + ").");
							errors = true;
							return false;
						}
					/* currently not defining anything, throw an error */
					}else{
						plugin.getLogger().log(Level.WARNING, "Attribute error at line " + line + ": Invalid attribute type.");
						errors = true;
						return false;
					}
					/* reset buffer and current property */
					currentProp = Property.NONE;
					buffer = new StringBuilder();
				}
			/* current is an opening curly bracket, we just entered a sub-command property definition */
			}else if (current == '{') {
				/* increment bracket counter */
				bracketCounter++;
				/* are we getting the name of the command? */
				if (gettingName && cmdName == null) {
					/* set the command name to what we just gathered (trimmed) */
					cmdName = buffer.toString().trim();
				}else{
					/* are we currently in an argument? */
					if (currentArgComp == null) {
						/* no, but if there is something that looks like text, put it into the current subcommand as a constant */
						if (buffer.toString().trim().length() > 0) {
							currentChain.append(new ConstantComponent(buffer.toString().trim()));
						}
					}else{
						/* yes, put it into the current subcommand */
						/* could happen when there are no 'spaces' */
						/* [str:example]{ */
						/*              ^ */
						currentChain.append(currentArgComp);
						currentArgComp = null;
					}
				}
				/* reset buffer */
				buffer = new StringBuilder();
				/* get whatever is at the top of the stack */
				ChainComponent top = stack.get();
				if (top != null) {
					/* if it's not null, add our sub-command we just finished to it */
					top.append(currentChain);
				}
				/* push the current sub-command onto the stack */
				stack.push(currentChain);
				/* reset our current sub-command */
				currentChain = new ChainComponent();
			/* current is a closing curly bracket, we just finished a property section */
			}else if (current == '}') {
				/* decrement the bracket counter */
				bracketCounter--;
				/* pop whatever was on the stack */
				ChainComponent popped = stack.pop();
				/* if it's null, throw an error */
				if (popped == null) {
					plugin.getLogger().log(Level.WARNING, "Syntax error at line " + line + ": Too many closing brackets.");
					errors = true;
					return false;
				}
				/* go through all it's sub-commands and set their properties accordingly */
				/* 'type' and 'permission' are inherited to sub-commands this way */
				for (ICommandComponent comp : popped.getComponents()) {
					if (comp instanceof ChainComponent) {
						if (((ChainComponent) comp).type == null) {
							((ChainComponent) comp).type = popped.type;
						}
						if (((ChainComponent) comp).permission == null) {
							((ChainComponent) comp).permission = popped.permission;
						}
					}
				}
				/* we just exited the main command, do more magic */
				if (bracketCounter == 0) {
					postProcess(cmdName, popped, methods, plugin, methodContainer); // \o/
					/* reset everything in case the user defined another command after this one */
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
			/* current is a space, we just finished defining which property we are about to set */
			}else if (current == ' ') {
				/* we are already defining a property, append it as text */
				if (currentProp != Property.NONE) {
					buffer.append(' ');
				}else{
					/* we got the 'command' definition, the name of the command will follow */
					if (buffer.toString().equals("command") && !gettingName && cmdName == null) {
						gettingName = true;
					/* we got other properties, their values will follow */
					}else if (buffer.toString().equals("help")) {
						currentProp = Property.HELP;
					}else if (buffer.toString().equals("run")) {
						currentProp = Property.EXECUTE;
					}else if (buffer.toString().equals("perm")) {
						currentProp = Property.PERMISSION;
					}else if (buffer.toString().equals("type")) {
						currentProp = Property.TYPE;
					/* we didn't get any of those, we are probably in the middle of a sub-command definition */
					/* example [int:value] { */
					/*        ^           ^  */
					}else{
						/* we are getting the name and we didn't set it yet, set it */
						if (gettingName && cmdName == null) {
							cmdName = buffer.toString().trim();
						}else{
							/* we aren't defining a type, put the current text into the sub-command as a constant */
							if (currentArgComp == null) {
								if (buffer.toString().trim().length() > 0) {
									currentChain.append(new ConstantComponent(buffer.toString().trim()));
								}
								/* we are defining a command, put it into the sub-command */
							}else{
								currentChain.append(currentArgComp);
								currentArgComp = null;
							}
						}
					}
					/* reset the buffer */
					buffer = new StringBuilder();
				}
			/* current is an opening square bracket, we just started a type definition */
			}else if (current == '[') {
				/* we are defining a property, treat it as text */
				if (currentProp != Property.NONE) {
					buffer.append('[');
				/* we are already inside of a type definition, throw an error */
				}else if (insideType) {
					plugin.getLogger().log(Level.WARNING, "Syntax error at line " + line + ": Invalid type declaration.");
					errors = true;
					return false;
				}else{
					/* we just entered a type definition */
					insideType = true;
				}
			/* current is a closing square bracket, we just finished a type definition */
			}else if (current == ']') {
				/* we are defining a property, treat it as text */
				if (currentProp != Property.NONE) {
					buffer.append(']');
				/* we are inside of a type */
				}else if (insideType) {
					insideType = false;
					/* current argument type is null, throw an error */
					if (currentArgComp == null) {
						currentArgComp = resolveComponentType(buffer.toString());
						buffer = new StringBuilder();
						if (currentArgComp instanceof EmptyComponent) {
							
						}else{
							/* should never happen */
							plugin.getLogger().log(Level.WARNING, "Type error at line " + line + ": Type has no type?");
							errors = true;
							return false;
						}
					}else{
						/* set the value of the current type and reset the buffer */
						currentArgComp.argName = buffer.toString();
						buffer = new StringBuilder();
						if (currentArgComp instanceof StringComponent) {
							StringComponent strComp = (StringComponent) currentArgComp;
							strComp.infinite = sideBuffer.toString().contains("...");
						}
						sideBuffer = new StringBuilder();
					}
				}else{
					/* we are not defining a type, throw an error */
					plugin.getLogger().log(Level.WARNING, "Syntax error at line " + line + ": Not in type declaration.");
					errors = true;
					return false;
				}
			/* typical escape sequences and such */
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
				if (currentArgComp != null && current == '.') {
					if (currentArgComp instanceof StringComponent) {
						sideBuffer.append(current);
					}else{
						plugin.getLogger().log(Level.WARNING, "Syntax error at line " + line + ": '...' is invalid for non-string types.");
						errors = true;
						return false;
					}
				}else{
					buffer.append(current);
				}
			}
			previous = current;
		}
		
		return true;
	}
	
	/**
	 * Resolves the string into a type, or null if invalid
	 * 
	 * @param type string you want to evaluate
	 * @return the type class or null if invalid
	 */
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
		case "optional":
		case "opt":
		case "flag":
			return new OptionalComponent();
		case "empty":
		case "null":
			return new EmptyComponent();
		}
		return null;
	}
	
	/**
	 * Resolves the string into a property, or null if invalid
	 * 
	 * @param type string you want to evaluate
	 * @return the property enum or null if invalid
	 */
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
		components.execute = null;
		components.help = null;
		components.permission = null;
		components.type = null;
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
			data += chain + "\0" + ((comp.permission == null || comp.permission.equals("-none-")) ? null : comp.permission) + "\0" + comp.execute + "\0" + comp.help + "\0" + Type.get(comp.type) + "\0";
			for (String s : leaves) {
				data += s;
			}
		}else{
			data += component.getComponentInfo() + " ";
		}
		
		return data;
	}
}
