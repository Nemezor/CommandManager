
import java.io.File;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.CommandManager;

public class CmdMgrTest extends JavaPlugin {

	public void onEnable() {
		CommandManager.registerCommand(new File("plugins/test.cmd"), this, this);
	}
	
	public void onDisable() {
		
	}
	
	@Command(hook="home_empty")
	public boolean executeHomeNull(CommandSender sender) {
		sender.sendMessage("You executed an empty /home");
		return true;
	}
	
	@Command(hook="home_set")
	public boolean executeSetHome(CommandSender sender, String name) {
		sender.sendMessage("You executed:");
		sender.sendMessage("/home set " + name);
		return true;
	}
	
	@Command(hook="home_set_coords")
	public boolean executeSetHomeBasedOnCoords(CommandSender sender, String name, int x, int y, int z) {
		sender.sendMessage("You executed:");
		sender.sendMessage("/home set " + name + " " + x + " " + y + " " + z);
		return true;
	}
	
	@Command(hook="home_del")
	public void executeDelHome(CommandSender sender, String name, boolean aFlag) {
		sender.sendMessage("You executed:");
		sender.sendMessage("/home del " + name);
		sender.sendMessage("-a - " + aFlag);
	}
	
	@Command(hook="home_list")
	public void executeListHomes(CommandSender sender) {
		sender.sendMessage("You executed:");
		sender.sendMessage("/home list");
	}
	
	@Command(hook="home_tp")
	public void executeTeleportHome(CommandSender sender, String name) {
		sender.sendMessage("You executed:");
		sender.sendMessage("/home " + name);
	}
	
	@Command(hook="noskope")
	public boolean executeMagik(CommandSender sender) {
		sender.sendMessage("You executed!!!!!!!!!:");
		sender.sendMessage("/home yolo swag");
		return true;
	}
}
