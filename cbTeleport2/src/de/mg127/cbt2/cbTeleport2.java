package de.mg127.cbt2;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class cbTeleport2 extends JavaPlugin implements Listener{
	
	public Logger log = Logger.getLogger("Minecraft");
	public Config config;
	private boolean taskisrunning;
	
	@Override
	public void onEnable() {
		final PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this,this);

		config.checkConfig();
		if (config.portAllEntities){
			taskisrunning=true;
			this.getServer().getScheduler().runTaskTimer(this, new TimedWorker(config), 120, config.delayPosCheck);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (args.length>0&&(sender.hasPermission("cbt")||sender.isOp())){
			if (args[0].equalsIgnoreCase("cbt")){
				if(args.length==2){
					if (args[1].equalsIgnoreCase("enable")){
						
						return true;						
					}
					if (args[1].equalsIgnoreCase("disable")){
						
						return true;
					}
				}
				if (args.length==3){
					if (args[1].equalsIgnoreCase("teleportall")){
						if (args[2].equalsIgnoreCase("on")){
							if (taskisrunning)
								this.getServer().getScheduler().cancelTasks(this);
							this.getServer().getScheduler().runTaskTimer(this, new TimedWorker(config), 120, config.delayPosCheck);
							taskisrunning=true;
							config.sendMessage((Player)sender,1);
							return true;
						}
						if (args[2].equalsIgnoreCase("off")){
							if (taskisrunning)
								this.getServer().getScheduler().cancelTasks(this);
							taskisrunning=false;
							config.sendMessage((Player)sender,2);
							return true;
						}
					}
				}
				if (args.length>3){
					if (args[1].equalsIgnoreCase("teleportall")){
						if (args[2].equalsIgnoreCase("timer")&&args[3].matches("^[0-9]+$")){
							if (taskisrunning){
								this.getServer().getScheduler().cancelTasks(this);
								config.delayAllCheck=Integer.valueOf(args[3]);
								this.getServer().getScheduler().runTaskTimer(this, new TimedWorker(config), 120, config.delayPosCheck);
							}else
								config.delayAllCheck=Integer.valueOf(args[3]);
							config.sendMessage((Player)sender, 3);
							return true;
						}
						if (args[2].equalsIgnoreCase("threshold")&&args[3].matches("^[0-9]+$")){
							String world=((Player)sender).getWorld().getName();
							if (config.worldHasPort(world)){
								config.connections.get(world).playerTDistance=Integer.valueOf(args[3]);
								//getworld->getconnection->playerTDistance
								config.sendMessage((Player)sender, 4);
							}else
								config.sendMessage((Player)sender, 5);
							return true;
						}
					}

					if (args[1].equalsIgnoreCase("set")){
						if (args[2].equalsIgnoreCase("bottom")){

							return true;
						}
						if (args[2].equalsIgnoreCase("top")){
							return true;
						}
					}
				}
				help(args,sender);
				return true;
			}
		}
		return false;
	}

	private void help(String[] args, CommandSender sender) {
		// TODO Auto-generated method stub
		
	}
}
