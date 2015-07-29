package de.mg127.TiaEssentials;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

//import de.mg127.TiaEssentials.RessourceManagement.RM;
//import de.mg127.TiaEssentials.Voting.Voting;

public class TiaEssentials extends JavaPlugin implements Listener{
	protected final Logger log = Logger.getLogger("Minecraft");
	@SuppressWarnings("unused")
	private WorldEditPlugin WEApi;
	private final Config config = new Config(this);
	//private final Voting voting = new Voting(this);
	//private final RM ressource = new RM(this);
	protected final String pName="[TiaEssentials] ",
	pDebug="[TiaEssentials Debug] ",
	pBase="TiaEssentials.",
	pAdmin="admin.",
	pUser="user.";

	private final HashMap<String,Location> respawnLoc=new HashMap<String,Location>();

	@SuppressWarnings("unused")
	private boolean thor, askPlayer, useWE, userMonitor;

	@Override
	public void onEnable() {
		final PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this,this);
		if (pm.isPluginEnabled("WorldEdit")){
			useWE=true;
			WEApi=(WorldEditPlugin) pm.getPlugin("WorldEdit");
		}
		config.checkConfig();
	}
	@Override
	public void onDisable(){}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if (event.isCancelled()) {
			return;
		}

		final Player player=event.getPlayer();
		/*if (userMonitor){ //new: player uses gamemode 3
			if (player.hasMetadata("status")){
				if (player.getMetadata("status").equals("spectator")){
					event.setCancelled(true);
					return;
				}
			}
		}*/
		//only specified player have access to the commandblock
		if (event.getClickedBlock().getType().equals(Material.COMMAND)){
			if (!PlayerHasPermission(player,"useCommandBlock")){
				event.setCancelled(true);
			}
		}
		//Thor lightning option
		if (thor){
			if (player.getItemInHand().getType().equals(Material.GOLD_BOOTS)&&
					PlayerHasPermission(player,"thor")&&
					(event.getAction().equals(Action.RIGHT_CLICK_AIR)||
							event.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
				final List<Block> blocks=player.getLineOfSight(null, 100);
				for(int i=0;i<100;i++) {
					if (blocks.get(i).getType()!=Material.AIR){
						player.getWorld().strikeLightning(blocks.get(i).getLocation());
						return;
					}
				}
			}
		}
	}
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		if (event.getBlockPlaced().getType().equals(Material.COMMAND)){
			if (PlayerHasPermission(event.getPlayer(),"useCommandBlock")) {
				event.setCancelled(true);
			}
		}
	}
	//infinite flint&steel for dispenser
	@EventHandler
	public void onBlockIgniteEvent(BlockIgniteEvent event){
		if (event.getCause().equals(IgniteCause.FLINT_AND_STEEL)) {
			try{
				if (event.getIgnitingEntity().getType().equals(Material.DISPENSER)||
						event.getIgnitingBlock().getType().equals(Material.DISPENSER)){
					final Dispenser dis=(Dispenser) event.getIgnitingBlock();
					final Inventory inv=dis.getInventory();
					final int size=inv.getSize();
					final int i=0;
					while (i<size){
						final ItemStack item=inv.getItem(i);
						if (item.getType().equals(Material.FLINT_AND_STEEL)){
							final int dmg=item.getDurability();
							if (dmg!=0){
								item.setDurability((short) (item.getDurability()-1));
								inv.setItem(i,item);
								break;
							}
						}
					}
				}
			}catch(final Exception e){}
		}
	}
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event){
		if (event.isCancelled()) {
			return;
		}
		if (userMonitor){
			final Player player=event.getPlayer();
			if (player.hasMetadata("status")){
				if (player.getMetadata("status").equals("spectator")){
					event.setCancelled(true);
				}
			}
		}
	}
	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event){
		if (userMonitor){
			final Player player=event.getPlayer();
			if (player.hasMetadata("status")){
				if (player.getMetadata("status").equals("spectator")){
					player.setHealth(20);
					player.setExhaustion(20);
				}
			}
		}
	}
	//respawn
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		if (event.getEntity() instanceof Player){
			//if (event.getEntityType().equals(EntityType.PLAYER)){
			final String playerName=event.getEntity().getName();
			if (event.getEntity().hasMetadata("tutorial")){
				respawnLoc.put(playerName, config.getTutorialRespawn(playerName));
				return;
			}

			if(config.graveyard){
				final Location loc=event.getEntity().getLocation();
				if(config.existGraveyard(loc.getWorld().getName())){
					final Location target=config.getNearestGraveyard(event.getEntity().getLocation());
					respawnLoc.put(playerName, target);
					if (config.debug) {
						log.info(pDebug+" "+event.getEntity().getName()+" died, setting nearest graveyard as respawn");
					}
				}else
					if (existWarp("---globalspawn---")){
						respawnLoc.put(playerName, config.getNearestGraveyard(config.warps.get("---globalspawn---").location));
						if (config.debug) {
							log.info(pDebug+" "+playerName+" died, setting globalSpawn as respawn");
						}
					}
				if(config.useSpecialEffects&&event.getEntity().getLastDamageCause().equals(EntityType.ZOMBIE)){
					loc.getWorld().strikeLightningEffect(loc);
					loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
				}
			}
		}
	}
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerRespawn(PlayerRespawnEvent event){
		final Player player=event.getPlayer();

		//graveyard respawn
		if (!respawnLoc.isEmpty()) {
			event.setRespawnLocation(respawnLoc.get(player.getName()));
		} else
			//normal respawn
			if (existWarp("spawn_"+player.getWorld().getName())) {
				event.setRespawnLocation(config.warps.get("spawn_"+player.getWorld().getName()).location);
			} else{
				if (existWarp("---globalspawn---")){
					event.setRespawnLocation(config.warps.get("---globalspawn---").location);
				} else {
					event.setRespawnLocation(player.getWorld().getSpawnLocation());
				}
			}
	}
	//get new player (first join)
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event){
		config.CheckNewTutorialStatus(event.getPlayer());
	}

	//tpa
	@EventHandler
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event){
		if (event.isCancelled()) {
			return;
		}
		if (askPlayer&&!config.askPlayerList.isEmpty()) {
			if (config.askPlayerList.get(event.getPlayer().getName())){
				if (event.getMessage().equalsIgnoreCase("y")){
					config.overwriteWarp(event.getPlayer().getName());
					event.getPlayer().sendMessage(ChatColor.GOLD+"spawnpoint created");
					config.askPlayerList.remove(event.getPlayer().getName());
					if (config.askPlayerList.isEmpty()) {
						askPlayer=false;
					}
					event.setCancelled(true);
				}
				else
				if (event.getMessage().equalsIgnoreCase("n")){
					config.tempWarps.remove(event.getPlayer().getName());
					event.getPlayer().sendMessage(ChatColor.GOLD+"spawnpoint creating canceled");
					config.askPlayerList.remove(event.getPlayer().getName());
					if (config.askPlayerList.isEmpty()) {
						askPlayer=false;
					}
					event.setCancelled(true);
				}
			}
		}
	}
	//prevent weather change
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event){
		if (event.isCancelled()||!config.weatherChange) {
			return;
		}
		event.setCancelled(true);
	}

	//////////////////////////
	//////// commands ////////
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		Player player=null;

		//console
		if (!(sender instanceof Player)){
			if (cmd.equals("time")){
				timeCommand(null,null,args);
				return true;
			}
			if (cmd.equals("msg")&&args.length>1)
			{	
				Player target=this.getServer().getPlayer(args[0]);
				if (target!=null){
					String msg="";
					for(int i=1;i<args.length;i++){
						msg=msg+args[i]+" ";
					}
					target.sendMessage(ChatColor.LIGHT_PURPLE+args[1]+"[wisper from] Console(Admin): "+msg);
				}
				return true;
			}
			if (cmd.equals("gamemode")&&args.length==2){
				gamemodeConsoleCommand(sender,args);
				return true;
			}
			if (cmd.equals("warp")&&args.length==2){
				Player target=this.getServer().getPlayer(args[0]);
				if (target!=null&&config.warps.containsKey(args[1])){
					target.teleport(config.warps.get(args[1]).location);
					return true;
				}
				log.info(pName+"warp or player not found");
				return true;
			}

			//ptime
			if (cmd.getName().equalsIgnoreCase("ptime")&&args.length==2) {
				Player target=null;
				target=this.getServer().getPlayer(args[0]);
				long time=getTimeFromString(args[1]);
				if (target==null||time==0)
					return true;
				if (args[0].equalsIgnoreCase("reset")){
					target.resetPlayerTime();
				}else
					target.setPlayerTime(time, false);
				return true;
			}
			//pweather
			if (cmd.getName().equalsIgnoreCase("pweather")) {
				if (args.length==2){
					Player target=null;
					target=this.getServer().getPlayer(args[0]);
					if (target==null)
						return true;
					if (args[0].equalsIgnoreCase("reset")){
						target.resetPlayerWeather();
					}else{
						WeatherType type=null;
						if (args[2].equalsIgnoreCase("clear"))
							type=WeatherType.CLEAR;
						if (args[2].equalsIgnoreCase("rain"))
							type=WeatherType.DOWNFALL;
						if (type!=null)
							target.setPlayerWeather(type);
					}
				}
				else
					log.info(pBase+"valid arguments for /pweather [player] [weather] are 'rain' and 'clear'");
				return true;
			}
			log.info(pName+"command not allowed to be used from the console");
			return true;
		}
		player=(Player) sender;
		
		//this.getServer().dispatchCommand(this.getServer().getConsoleSender(), "");
		//this.getServer().getConsoleSender().sendRawMessage("");
		
		//vanilla override
		if (cmd.equals("biome")){
			biomeCommand(player,args);
			return true;
		}
		if (cmd.equals("gamemode")){
			gamemodeCommand(player,args);
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("op")&&args.length==1) {
			if(player.isOp()) {
				Player target=getServer().getPlayer(args[0]);
				if (target!=null)
					target.setOp(true);
			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("whitelist")&&args.length>0&&PlayerHasPermission(player, "whitelist")){
			if (args[0].equalsIgnoreCase("list")){
				this.sendLongMessage(player, getServer().getWhitelistedPlayers().toString(), ChatColor.GOLD);
			}
			if (args[0].equalsIgnoreCase("add")&&args.length>1){
				getServer().getPlayer(args[0]).setWhitelisted(true);
			}
			if (args[0].equalsIgnoreCase("remove")&&args.length==1){
				getServer().getPlayer(args[0]).setWhitelisted(false);
			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("ban")&&args.length==1&&PlayerHasPermission(player, "whitelist")){
			getServer().getPlayer(args[0]).setWhitelisted(false);
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("gamemode")&&PlayerHasPermission(player, "gamemode")) {
			if (PlayerHasPermission(player,"gamemode")){
				if (args.length==0) {
					gamemodeCommand(player);
				} else {
					gamemodeCommand(player,args);
				}
				return true;
			}
		}
		if (cmd.getName().equalsIgnoreCase("time")) {
			if (PlayerHasPermission(player,"time")){
				timeCommand(player, sender, args);
				return true;
			}
		}

		//own commands
		if (cmd.getName().equalsIgnoreCase("tia")) {
			if (PlayerHasPermission(player,"tia")) {
				return tiaCommand(player,args);
			} else
			{
				helpCommand(player,1);
				return true;
			}
		}
		//item
		if (cmd.getName().equalsIgnoreCase("item")||cmd.getName().equalsIgnoreCase("give")) {
			if (PlayerHasPermission(player,"item")){
				return itemCommand(sender,player,args);
			}
		}
		//teleports
		// tp
		if (cmd.getName().equalsIgnoreCase("tp")) {
			if (PlayerHasPermission(player,"tp")){
				switch (args.length){
				
				case (1):{//tp target
					final Player target=getServer().getPlayer(args[0]);
					if (target!=null){
						player.teleport(target);
						return true;
					}
					else{
						sender.sendMessage(ChatColor.GOLD+"target player not found");
						return true;
					}
				}
				case (2):{//tp target1 target2
					final Player target1=getServer().getPlayer(args[0]);
					final Player target2=getServer().getPlayer(args[1]);
					if (target1!=null&&target2!=null){
						target1.teleport(target2);
						return true;
					}
					else{
						sender.sendMessage(ChatColor.GOLD+"target player not found");
						return true;
					}
				}
				case (3):{//tp x y z
					if (args[1].matches("^[0-9]+$")&&
							(args[0].matches("^[0-9]+$")||args[0].startsWith("-")&&args[0].substring(1).matches("^[0-9]+$"))&&
							(args[2].matches("^[0-9]+$")||args[2].startsWith("-")&&args[2].substring(1).matches("^[0-9]+$"))){
						player.teleport(new Location(player.getWorld(),Double.valueOf(args[0]),Double.valueOf(args[1]),Double.valueOf(args[2])));
						return true;
					}
				}
				case (4):{// tp player x y z
					if (args[2].matches("^[0-9]+$")&&
							(args[1].matches("^[0-9]+$")||args[1].startsWith("-")&&args[1].substring(1).matches("^[0-9]+$"))&&
							(args[3].matches("^[0-9]+$")||args[3].startsWith("-")&&args[3].substring(1).matches("^[0-9]+$"))){
						final Player target=getServer().getPlayer(args[0]);
						if (target!=null){
							target.teleport(new Location(target.getWorld(),Double.valueOf(args[0]),Double.valueOf(args[1]),Double.valueOf(args[2])));
							return true;
						}
						else{
							sender.sendMessage(ChatColor.GOLD+"target player not found");
							return true;
						}
					}
				}
				case (5)://tp player world x y z
					if (args[3].matches("^[0-9]+$")&&
							(args[2].matches("^[0-9]+$")||args[2].startsWith("-")&&args[2].substring(1).matches("^[0-9]+$"))&&
							(args[4].matches("^[0-9]+$")||args[4].startsWith("-")&&args[4].substring(1).matches("^[0-9]+$"))){
						final Player target=getServer().getPlayer(args[0]);
						final World world=getServer().getWorld(args[1]);
						if (target!=null&&world!=null){
							target.teleport(new Location(world,Double.valueOf(args[0]),Double.valueOf(args[1]),Double.valueOf(args[2])));
							return true;
						}
						else{
							sender.sendMessage(ChatColor.GOLD+"target player not found");
							return true;
						}
					}
				}
				sender.sendMessage(ChatColor.GOLD+"wrong arguments for /tp player");
				return true;
			}
		}
		// tpa (ask)
		if (cmd.getName().equalsIgnoreCase("tpa")||cmd.getName().equalsIgnoreCase("tpask")||cmd.getName().equalsIgnoreCase("tpaccept")) {
			if (PlayerHasPermission(player,"tpa")&&args.length>0){
				tpaCommand(player,args);
			} else {
				config.tpaAnswer(player);
			}
		}

		// tpp (point)
		if (cmd.getName().equalsIgnoreCase("tpp")||cmd.getName().equalsIgnoreCase("tppos")) {
			if (PlayerHasPermission(player,"tpp")){
			}
		}
		// tph (here)
		if (cmd.getName().equalsIgnoreCase("tph")||cmd.getName().equalsIgnoreCase("tphere")) {
			if (PlayerHasPermission(player,"tph")){
				if (args.length==1){
					final Player target=getServer().getPlayer(args[0]);
					if (target!=null){
						target.teleport(player);
						return true;
					}
					else{
						sender.sendMessage(ChatColor.GOLD+"target player not found");
						return true;
					}

				}
				else{
					sender.sendMessage(ChatColor.GOLD+"wrong arguments for /tph");
					return true;
				}
			}
		}
		// tpha
		if (cmd.getName().equalsIgnoreCase("tpha")) {
			if (PlayerHasPermission(player,"tpha")){
				final Player[] players=getServer().getOnlinePlayers();
				Player target;
				for (int i=0;i<=players.length;i++){
					target=players[i];
					if (!target.getName().equals(player.getName())) {
						target.teleport(player);
					}
					target.sendMessage(ChatColor.GOLD+"you have been teleportet to "+player.getName());
					player.sendMessage(ChatColor.GOLD+"you teleported "+target.getName()+" to you");
				}
			}
		}
		// warp
		if (cmd.getName().equalsIgnoreCase("warp")&&args.length==1){
			if (PlayerHasPermission(player,"warp")||PlayerHasPermission(player, "advancedWarp")){
				if (existWarp(args[0])){
					if (PlayerHasPermission(player, "advancedWarp")){
						Location target=config.warps.get(args[0]).location;
						if (!this.getServer().getWorlds().contains(target.getWorld())){
							sender.sendMessage(ChatColor.GOLD+"your target world is not existant or unloaded");
							return true;
						}
						player.teleport(target);
						sender.sendMessage(ChatColor.GOLD+"Woosh!");
						return true;
					}

					if (!args[0].contains("home_")&&
							!args[0].contains("spawn_")&&
							!args[0].contains("lastPos_")&&
							!args[0].contains("tut_")&&
							!args[0].contains("---globalspawn---")){
						if (config.warps.get(args[0]).Type.equals("private")){
							if (player.getName()!=config.warps.get(args[0]).Creator){
								sender.sendMessage(ChatColor.GOLD+"You have no permissions for this warppoint");
								return true;
							}
							player.teleport(config.warps.get(args[0]).location);
							sender.sendMessage(ChatColor.GOLD+"Woosh!");
							return true;
						}
					}
					sender.sendMessage(ChatColor.GOLD+"You have no permissions for this warppoint");
					return true;
				}
				sender.sendMessage(ChatColor.GOLD+"That warp doesn't exist");
				return true;
			}
		}
		// setwarp
		if (cmd.getName().equalsIgnoreCase("setwarp")&&args.length>0) {
			if (PlayerHasPermission(player,"setwarp")){
				final String name=args[0];

				if (args.length>1 && args.toString().substring(args[0].length()-1).contains("-p")){
					//private mode
					setWarp(name,"private",player);
					if (config.debug) {
						player.sendMessage(ChatColor.GOLD+"private warppoint "+ChatColor.RED+name+" created");
					} else {
						player.sendMessage(ChatColor.GOLD+"private warppoint created");
					}
					return true;
				}
				else{
					//public mode
					setWarp(name,"public",player);
					if (config.debug) {
						player.sendMessage(ChatColor.GOLD+"public warppoint "+ChatColor.RED+name+" created");
					} else {
						player.sendMessage(ChatColor.GOLD+"public warppoint created");
					}
					return true;
				}
			}
			player.sendMessage(ChatColor.GOLD+"/setwarp (name) [-p]");
			player.sendMessage(ChatColor.GOLD+"-p = private warp");
		}
		// delwarp
		if (cmd.getName().equalsIgnoreCase("delwarp")&&args.length>=1) {
			if (PlayerHasPermission(player,"delwarp")){
				if (existWarp(args[0])){
					config.removeWarp(args[0]);
					sender.sendMessage(ChatColor.GOLD+"deleted that warppoint");
					return true;
				}
				else{
					sender.sendMessage(ChatColor.GOLD+"A warp with that name doesn't exist");
					return true;
				}
			}
		}
		// setspawn
		if (cmd.getName().equalsIgnoreCase("setspawn")) {
			if (PlayerHasPermission(player,"setspawn")){
				if (args.length==1){
					if (args[0].equalsIgnoreCase("-g")){
						setWarp("---globalspawn---","spawn",player);
						sendLongMessage(player, "global spawn created.",ChatColor.GOLD);
						sendLongMessage(player, "respawning players on all worlds, except worlds with local spawns, will teleport to this point from now on",ChatColor.GOLD);
					}
				} else {
					setWarp("spawn_"+player.getWorld().getName(),"spawn",player);
				}
				player.getWorld().setSpawnLocation(player.getLocation().getBlockX(),
						player.getLocation().getBlockY(),
						player.getLocation().getBlockZ());
				sendLongMessage(player, "spawn for this world created.",ChatColor.GOLD);
				sendLongMessage(player, "respawning players on this world will teleport to this point from now on",ChatColor.GOLD);
				return true;
			}
		}
		// spawn
		if (cmd.getName().equalsIgnoreCase("spawn")||cmd.getName().equalsIgnoreCase("respawn")) {
			if (PlayerHasPermission(player,"spawn")){
				if (existWarp("spawn_"+player.getWorld().getName())){
					player.teleport(config.warps.get("spawn_"+player.getWorld().getName()).location);
					sender.sendMessage(ChatColor.GOLD+"Woosh!");
				}
				else{
					player.teleport(new Location(player.getWorld(),0,player.getWorld().getHighestBlockYAt(0, 0)+1,0));
					sender.sendMessage(ChatColor.GOLD+"Woosh!");
				}
				return true;

			}
		}
		// home
		if (cmd.getName().equalsIgnoreCase("home")) {
			if (args.length>=1&&PlayerHasPermission(player,"advancedHome")){
				if (existWarp("home_"+args[0])){
					player.teleport(config.warps.get("home_"+args[0]).location);
					player.sendMessage(ChatColor.GOLD+"Warped "+args[0]+" to his/her home.");
				} else {
					player.sendMessage(ChatColor.GOLD+"This player does not have a homepoint!");
				}
				return true;
			}
			else if(PlayerHasPermission(player,"home")){
				if (existWarp("home_"+player.getName())){
					player.teleport(config.warps.get("home_"+player.getName()).location);
					player.sendMessage(ChatColor.GOLD+"Wellcome Home!");
				} else {
					player.sendMessage(ChatColor.GOLD+"Set a homepoint first!");
				}
				return true;
			}
		}
		// sethome
		if (cmd.getName().equalsIgnoreCase("sethome")){
			if (args.length>0&&PlayerHasPermission(player,"advancedSethome")){
				setWarp("home_"+args[0],"home",player);
				getServer().getPlayer(args[0]).setBedSpawnLocation(player.getLocation());
				player.sendMessage(ChatColor.GOLD+"Home set for player "+args[0]);
				return true;
			}
			if (PlayerHasPermission(player,"sethome")){
				setWarp("home_"+player.getName(),"home",player);
				player.setBedSpawnLocation(player.getLocation());
				player.sendMessage(ChatColor.GOLD+"Your new homepoint will be here.");
				return true;
			}
		}
		// listwarps
		if (cmd.getName().equalsIgnoreCase("listwarps")&&PlayerHasPermission(player,"listwarps")){
			String msg="";

			if (args.length==1){
				//todo: ask for different warp types to be listed												/////todo//////

			}else{
				msg=ChatColor.GOLD+"public warps: "+ChatColor.WHITE;
				for (final String key : config.warps.keySet()){
					if (!key.contains("private_")&&
							!key.contains("home_")&&
							!key.contains("spawn_")&&
							!key.contains("lastPos_")&&
							!key.contains("tut_")&&
							!key.contains("---")) {
						msg=msg+key+", ";
					}
				}
			}
			msg=msg.substring(0,msg.length()-2);
			sendLongMessage(player,msg);
			return true;
		}
		//
		//getid
		if(cmd.getName().equalsIgnoreCase("getid")&&PlayerHasPermission(player,"getid")){
			final List<Block> blocks=player.getLineOfSight(null, 50);
			for(int i=0;i<60;i++) {
				if (blocks.get(i).getTypeId()!=0){
					sender.sendMessage("Name="+blocks.get(i).getType().toString()+" ID="+blocks.get(i).getTypeId()+" Data="+blocks.get(i).getData());
					return true;
				}
			}
			return true;
		}
		//spawnmob
		if (cmd.getName().equalsIgnoreCase("spawnmob")&&args.length>=1) {
			if (PlayerHasPermission(player,"spawnmob")){
				if (args.length==1){
					spawnMob(getPosWherePlayerLooks(player),args[0],1);
					return true;
				}
				if (args.length==2) {
					if (args[1].matches("^[0-9]+$")){
						spawnMob(getPosWherePlayerLooks(player),args[0],Integer.valueOf(args[1]));
						return true;
					}
				}
				player.sendMessage(ChatColor.GOLD+"//spawnmob (mobname) [count]");
			}
		}
		//ptime
		if (cmd.getName().equalsIgnoreCase("ptime")&&args.length>=1) {
			Player target=null;
			long time=0;
			if (args.length==2){
				target=this.getServer().getPlayer(args[0]);
				time=getTimeFromString(args[1]);
			}
			if (args.length==1){
				target=player;
				time=getTimeFromString(args[0]);
			}
			if (target==null||time==0)
				return true;
			if (args[0].equalsIgnoreCase("reset")){
				target.resetPlayerTime();
			}else
			target.setPlayerTime(time, false);
		}
		//pweather
		if (cmd.getName().equalsIgnoreCase("pweather")&&args.length>=1) {
			if (args.length==2){
				Player target=null;
				target=this.getServer().getPlayer(args[0]);
				if (target==null)
					return true;
				if (args[0].equalsIgnoreCase("reset")){
					target.resetPlayerWeather();
				}else{
					WeatherType type=null;
					if (args[2].equalsIgnoreCase("clear"))
						type=WeatherType.CLEAR;
					if (args[2].equalsIgnoreCase("rain"))
						type=WeatherType.DOWNFALL;
					if (type!=null)
						target.setPlayerWeather(type);
				}
			}
			else
				sender.sendMessage(pBase+"/pweather [player] [rain/clear]");
			return true;
		}
		//text//
		// me

		//schematic save mcedit
		if (cmd.getName().equalsIgnoreCase("save")&&args.length==1){
			getServer().dispatchCommand(player,"schematic save mcedit "+args[0]);
			return true;
		}

		//graveyard
		if (cmd.getName().equalsIgnoreCase("gy")||cmd.getName().equalsIgnoreCase("graveyard")) {
			if (PlayerHasPermission(player, "graveyard")){
				if (args.length>0){
					if (args[0].equalsIgnoreCase("add")){
						final int ord=config.newGraveOrd(player.getLocation().getWorld().getName());
						final WarpPoint wp=new WarpPoint("", player, player.getLocation().getWorld().getName()+"_"+ord);
						config.addGraveyard(wp);
						player.sendMessage(ChatColor.GOLD+"graveyard created! name: "+wp.LongName);
						return true;
					}
					if (args[0].equalsIgnoreCase("remove")){
						if (args.length==1){
							if (config.existGraveyard(player.getWorld().getName())){
								final String grave=config.getNearestGraveyardName(player.getLocation());
								config.removeGraveyard(grave);
								player.sendMessage(ChatColor.GOLD+"Graveyard "+grave+" removed");
								return true;
							}
						}
						else{
							if (config.graveyards.containsKey(args[1])){
								config.removeGraveyard(args[1]);
								player.sendMessage(ChatColor.GOLD+"Graveyard "+args[1]+" removed");
								return true;
							}
							player.sendMessage(ChatColor.GOLD+"could not find "+args[1]);
							return true;
						}
					}
					if (args[0].equalsIgnoreCase("get")){
						if (config.existGraveyard(player.getWorld().getName())){
							final String gy=config.getNearestGraveyardName(player.getLocation());
							if (gy==null){
								player.sendMessage(ChatColor.GOLD+"there is no graveyard in this world");
								return true;
							}
							final Location target=config.graveyards.get(gy).location;
							final double x1=player.getLocation().getX();
							final double z1=player.getLocation().getZ();
							final double x2=target.getX();
							final double z2=target.getZ();
							final int distance=(int) Math.round(Math.sqrt((x1-x2)*(x1-x2)+(z1-z2)*(z1-z2)));
							player.sendMessage(ChatColor.GOLD+"Neares Graveyard is "+gy+" ; distance: "+distance+
									" at X:"+target.getBlockX()+" Y:"+target.getBlockY()+" Z:"+target.getBlockZ());
							return true;
						}
						player.sendMessage(ChatColor.GOLD+"there is no graveyard in this world");
						return true;
					}
					if (args[0].equalsIgnoreCase("port")){
						if (args.length==1){
							player.teleport(config.getNearestGraveyard(player.getLocation()));
							return true;
						}else{
							if (args.length==2){
								String gy=config.getNearestGraveyardName(player.getLocation());
								if (gy!=null)									
									player.teleport(config.graveyards.get(gy).location);
								else
									player.sendMessage(ChatColor.GOLD+"there is no graveyard in this world");
								return true;
							}
						}
					}
				}
				player.sendMessage(ChatColor.GOLD+"/graveyard add");
				player.sendMessage(ChatColor.GOLD+"/graveyard remove (graveyard)");
				player.sendMessage(ChatColor.GOLD+"/graveyard get");
				player.sendMessage(ChatColor.GOLD+"/graveyard port (graveyard)");
				return true;
			}
		}
		if (cmd.getName().equalsIgnoreCase("vote")){
			//voting.voteCommand(player, args);
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("rm")&&player.hasPermission("rm")){
			//ressource.rmCommand(player,args);
			return true;
		}
		return false;
	}
	////////////////////////////////
	////// command functions ///////

	private long getTimeFromString(String string) {
		if (string.length()==0)
			return 0;
		if (string.matches("^[0-9]+$"))
			return Long.valueOf(string);
		if (string.contains(":")&&string.length()>3){
			if (string.indexOf(":")>=string.length()-2||string.indexOf(":")==0)
				return 0;
			long time=0;
			if (string.substring(0, string.lastIndexOf(":")).matches("^[0-9]+$")){
				time=Long.valueOf(string.substring(0, string.lastIndexOf(":")));
				if (time<7){
					time=time+17;
				}	
				else{
					time=time-7;
				}
				time=time*1000;
			}
			if (string.substring(string.lastIndexOf(":")+1,string.length()).matches("^[0-9]+$")){
				time=time+Long.valueOf(string.substring(string.lastIndexOf(":")+1,string.length()));
			}
		}	
		return 0;
	}
	private void biomeCommand(Player player, String[] args) {
		if (args.length==0){
			player.sendMessage("Biome: "+player.getWorld().getBiome(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ()));
			return;
		}
		if (args[0].equals("set")){
			if (args.length==2){
				player.getWorld().setBiome(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ(), Biome.valueOf(args[1]));
			}else{
				player.sendMessage("available biome: "+Biome.values().toString());
			}
				
		}
	}

	private void tpaCommand(Player player,String[] args) {
		final Player target=getServer().getPlayer(args[0]);
		if (target!=null){
			if (!target.isEmpty()){
				player.sendMessage(ChatColor.GRAY+"You asked "+target.getName()+" for allowence to teleport to him.");
				target.sendMessage(ChatColor.GREEN+player.getName()+" is asking you, if he is allowed to teleport to your position");
				target.sendMessage(ChatColor.GRAY+"type /tpa to accept the inquiry");
				config.tpaAskPlayer((Player) player, target);
				return;
			}
		}
		player.sendMessage(ChatColor.GOLD+"target player not found");
	}
	// help command
	private void helpCommand(Player player,int page){
		float count=0;
		if (PlayerHasPermission(player,"tia")){
			count++;if(Math.ceil((count-0.1)/5)<=page) {
				player.sendMessage(ChatColor.GOLD+"/tia ... see /tia for more help");
			}if(count/5>page) {
				return;
			}}
		if (PlayerHasPermission(player,"config")){
			count++;if(Math.ceil((count-0.1)/5)<=page) {
				player.sendMessage(ChatColor.GOLD+"/tia config - change config values");
			}if(count/5>page) {
				return;
			}}
		if (PlayerHasPermission(player,"item")){
			count++;if(Math.ceil((count-0.1)/5)<=page) {
				player.sendMessage(ChatColor.GOLD+"/item [player] (item-id:[dmg]) [count]");
			}if(count/5>page) {
				return;
			}}
		if (PlayerHasPermission(player,"tp")){
			count++;if(Math.ceil((count-0.1)/5)<=page) {
				player.sendMessage(ChatColor.GOLD+"/tp (player1) [player2] - teleports player1 or you to player2 w/o asking");
			}if(count/5>page) {
				return;
			}}
		if (PlayerHasPermission(player,"tpa")){
			count++;if(Math.ceil((count-0.1)/5)<=page) {
				player.sendMessage(ChatColor.GOLD+"/tpa [player] - ask player if you could teleport to him/her");
			}if(count/5>page) {
				return;
			}}
		//if (PlayerHasPermission(player,"tpp")){
		//	count++;if(Math.ceil((count-0.1)/5)<=page) {
		//		player.sendMessage(ChatColor.GOLD+"/tpp - ");
		//	}if(count/5>page) {
		//		return;
		//	}}
		/*if (PlayerHasPermission(player,"")){
			 count++;if(Math.ceil((count-0.1)/5)<=page)player.sendMessage(ChatColor.GOLD+"");if(count/5>page)return;}
		/*if (PlayerHasPermission(player,"")){
			 count++;if(Math.ceil((count-0.1)/5)<=page)player.sendMessage(ChatColor.GOLD+"");if(count/5>page)return;}
		/*if (PlayerHasPermission(player,"")){
			 count++;if(Math.ceil((count-0.1)/5)<=page)player.sendMessage(ChatColor.GOLD+"");if(count/5>page)return;}
		/*if (PlayerHasPermission(player,"")){
			 count++;if(Math.ceil((count-0.1)/5)<=page)player.sendMessage(ChatColor.GOLD+"");if(count/5>page)return;}
		/*if (PlayerHasPermission(player,"")){
			 count++;if(Math.ceil((count-0.1)/5)<=page)player.sendMessage(ChatColor.GOLD+"");if(count/5>page)return;}
		/*if (PlayerHasPermission(player,"")){
			 count++;if(Math.ceil((count-0.1)/5)<=page)player.sendMessage(ChatColor.GOLD+"");if(count/5>page)return;}
		/*if (PlayerHasPermission(player,"")){
			 count++;if(Math.ceil((count-0.1)/5)<=page)player.sendMessage(ChatColor.GOLD+"");if(count/5>page)return;}
		/*if (PlayerHasPermission(player,"")){
			 count++;if(Math.ceil((count-0.1)/5)<=page)player.sendMessage(ChatColor.GOLD+"");if(count/5>page)return;}
		/*if (PlayerHasPermission(player,"")){
			 count++;if(Math.ceil((count-0.1)/5)<=page)player.sendMessage(ChatColor.GOLD+"");if(count/5>page)return;}
		 */
		//if (PlayerHasPermission(player,"")){
		//	 count++;if(Math.ceil((count-0.1)/5)<=page)player.sendMessage(ChatColor.GOLD+"");if(count/5>page)return;}
		///////////////////////////////////////////////////////////////////
		//..................todo: help...................................//
		///////////////////////////////////////////////////////////////////
	}
	// config - command
	private boolean tiaCommand(Player player, String[] args) {
		if (args.length>0){
			//help for this all TiaEssential commands
			if (args[0].equalsIgnoreCase("help")) {
				if (args.length>1){
					if (args.length==1&&args[0].matches("^[0-9]+$")) {
						helpCommand(player,Integer.valueOf(args[0]));
					}
				} else {
					helpCommand(player,1);
				}
			}
			//for changing live in the config.yml
			if (args[0].equalsIgnoreCase("config")&&
					PlayerHasPermission(player, "config")){
				if (args.length>2){
					if (args[1].equalsIgnoreCase("weather")){
						if (args[2].equalsIgnoreCase("on")){
							config.weather(true);
							return true;
						}else
							if (args[2].equalsIgnoreCase("off")){
								config.weather(false);
								return true;
							}
					}
				}
				if (args.length>1) {
					if (args[1].equalsIgnoreCase("reload")){
						config.checkConfig();
						player.sendMessage(ChatColor.GOLD+"reload complete");
						return true;
					}
				}
				player.sendMessage(ChatColor.GOLD+"/tia config weather (on/off)");
				player.sendMessage(ChatColor.GOLD+"/tia config reload");
				return true;
			}
			//thors hammer, use golden
			if (args[0].equalsIgnoreCase("thor")&&
					PlayerHasPermission(player,"thor")){
				if (thor){
					player.sendMessage(ChatColor.GOLD+"disabled Thors Hammer");
					thor=false;
				}
				else{
					player.sendMessage(ChatColor.GOLD+"enabled Thors Hammer");
					thor=true;
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("itemset")){
				if(args.length==3){
					if (args[1].equalsIgnoreCase("save")&&
							PlayerHasPermission(player,"itemset.save")){
						final PlayerInventory inv=player.getInventory();
						final ItemStack stacks[]={
								inv.getItem(0),
								inv.getItem(1),
								inv.getItem(2),
								inv.getItem(3),
								inv.getItem(4),
								inv.getItem(5),
								inv.getItem(6),
								inv.getItem(7),
								inv.getItem(8)};
						config.newItemSet(args[2], stacks);
						player.sendMessage(ChatColor.GOLD+"itemset saved as "+args[2]);
						return true;
					}
					if (args[1].equalsIgnoreCase("load")&&
							PlayerHasPermission(player,"itemset.load")){
						if (!config.existsItemSet(args[2])){
							player.sendMessage(ChatColor.GOLD+"that itemset doesn't exist");
							return true;
						}
						final PlayerInventory inv=player.getInventory();
						config.ItemSets.get(args[2]).putItems(inv);
						player.sendMessage(ChatColor.GOLD+"itemset "+args[2]+" loaded!");
						return true;
					}
					if (args[1].equalsIgnoreCase("del")&&
							PlayerHasPermission(player,"itemset.save")){
						if (!config.existsItemSet(args[2])){
							player.sendMessage(ChatColor.GOLD+"that itemset doesn't exist");
							return true;
						}
						config.delItemSet(args[2]);
						player.sendMessage(ChatColor.GOLD+"itemset "+args[2]+" deleted!");
						return true;
					}
				}
				if (args.length==2) {
					if (args[1].equalsIgnoreCase("list")&&
							PlayerHasPermission(player,"itemset.load")){
						String result="";
						Set<String> keys;
						keys=config.ItemSets.keySet();
						final Iterator<String> iter=keys.iterator();

						if (iter.hasNext()) {
							result+=iter.next();
						}
						while(iter.hasNext()){
							result+=", ";
							result+=iter.next();
						}
						sendLongMessage(player,result,ChatColor.GOLD);
						return true;
					}
				}
				player.sendMessage(ChatColor.GOLD+"/tia itemset save [name]");
				player.sendMessage(ChatColor.GOLD+"/tia itemset load [name]");
				player.sendMessage(ChatColor.GOLD+"/tia itemset del [name]");
				player.sendMessage(ChatColor.GOLD+"/tia itemset list");
				return true;
			}
		}
		player.sendMessage(ChatColor.GOLD+"/tia config");
		player.sendMessage(ChatColor.GOLD+"/tia thor");
		player.sendMessage(ChatColor.GOLD+"/tia itemset");
		return true;
	}
	//gamemode [target] [mode]  -> for console usage
	private void gamemodeConsoleCommand(CommandSender sender,String[] args){
		if (args.length>1) {
			final Player target=getServer().getPlayer(args[0]);
			if (target==null){
				log.info(pName+"Cannot find "+args[0]);
				return;
			}
			if (args[1].equalsIgnoreCase("creative")||args[1].equals("1")||args[1].contains("crea")) {
				target.setGameMode(GameMode.CREATIVE);
			} else
				if (args[1].equalsIgnoreCase("survival")||args[1].equals("0")||args[1].contains("surv")) {
					target.setGameMode(GameMode.SURVIVAL);
				} else
					if (args[1].equalsIgnoreCase("adventure")||args[1].equals("2")||args[1].contains("adv")) {
						target.setGameMode(GameMode.ADVENTURE);
					} else
					{
						log.info(pName+"wrong paramter");
						log.info(pName+"/gamemode (target) [gamemode]");
						log.info(pName+"gamemodes: 0 Survival; 1 Creative; 2 Adventure");
						return;
					}
			
		} else{
			log.info(pName+"you need to adress a target and a mode");
			log.info(pName+"gamemode [target] [gamemode]");
		}
	}
	//gamemode (target) [mode]
	private void gamemodeCommand(Player player, String[] args) {
		if (args.length>1){
			final Player target=getServer().getPlayer(args[0]);
			if (target==null){
				player.sendMessage(ChatColor.GOLD+"Cannot find "+args[0]);
				return;
			}
			if (args[1].equalsIgnoreCase("creative")||args[1].equals("1")||args[1].contains("crea")) {
				target.setGameMode(GameMode.CREATIVE);
			} else
				if (args[1].equalsIgnoreCase("survival")||args[1].equals("0")||args[1].contains("surv")) {
					target.setGameMode(GameMode.SURVIVAL);
				} else
					if (args[1].equalsIgnoreCase("adventure")||args[1].equals("2")||args[1].contains("adv")) {
						target.setGameMode(GameMode.ADVENTURE);
					} else
					{
						player.sendMessage(ChatColor.GOLD+"wrong paramter");
						player.sendMessage(ChatColor.GOLD+"/gamemode (target) [gamemode]");
						player.sendMessage(ChatColor.GOLD+"gamemodes: 0 Survival; 1 Creative; 2 Adventure");
						return;
					}
			if (target.getGameMode().equals(GameMode.SURVIVAL)){
				target.sendMessage(ChatColor.GOLD+"Your GameMode is now Survival");
				player.sendMessage(ChatColor.GOLD+"GameMode from "+target.getName()+" set to Survival");
			}
			if (target.getGameMode().equals(GameMode.CREATIVE)){
				target.sendMessage(ChatColor.GOLD+"Your GameMode is now Creative");
				player.sendMessage(ChatColor.GOLD+"GameMode from "+target.getName()+" set to Creative");
			}
			if (target.getGameMode().equals(GameMode.ADVENTURE)){
				target.sendMessage(ChatColor.GOLD+"Your GameMode is now Adventure");
				player.sendMessage(ChatColor.GOLD+"GameMode from "+target.getName()+" set to Adventure");
			}
		}
		else{
			if (args[0].equalsIgnoreCase("creative")||args[0].equals("1")||args[0].contains("cre")){
				player.setGameMode(GameMode.CREATIVE);
				player.sendMessage(ChatColor.GOLD+"Your GameMode is now Creative");
			}
			else
				if (args[0].equalsIgnoreCase("survival")||args[0].equals("0")||args[0].contains("sur")){
					player.setGameMode(GameMode.SURVIVAL);
					player.sendMessage(ChatColor.GOLD+"Your GameMode is now Survival");
				}
				else
					if (args[0].equalsIgnoreCase("adventure")||args[0].equals("2")||args[0].contains("adv")){
						player.setGameMode(GameMode.ADVENTURE);
						player.sendMessage(ChatColor.GOLD+"Your GameMode is now Adventure");
					}
					else
					{
						player.sendMessage(ChatColor.GOLD+"wrong paramter");
						player.sendMessage(ChatColor.GOLD+"gamemode (target) [gamemode]");
						player.sendMessage(ChatColor.GOLD+"gamemodes: 0 Survival; 1 Creative; 2 Adventure");
						return;
					}
		}
	}
	//gamemode    -> toggles state
	private void gamemodeCommand(Player player) {
		if (player.getGameMode().equals(GameMode.SURVIVAL)){
			player.setGameMode(GameMode.CREATIVE);
			player.sendMessage(ChatColor.GOLD+"Your GameMode is now Creative");
		}
		else
			if (player.getGameMode().equals(GameMode.ADVENTURE)){
				player.setGameMode(GameMode.SURVIVAL);
				player.sendMessage(ChatColor.GOLD+"Your GameMode is now Survival");
			}
			else{
				//if (player.getGameMode().equals(GameMode.CREATIVE)){
				player.setGameMode(GameMode.ADVENTURE);
				player.sendMessage(ChatColor.GOLD+"Your GameMode is now Adventure");
			}
	}

	
	// item - give command
	// item [target] (id)[:dmg] [count]
	// item [target] (itemname)[:dmg] [count]
	private boolean itemCommand(CommandSender sender,Player player, String[] args) {
		if (args.length==1){
			addItemToInventory(player,args[0],1);
			return true;
		}else
		if (args.length==2){ 
			if (args[1].matches("^[0-9]+$")){ //item target/itemname id/count
				if (Material.getMaterial(args[0])!=null){ //item itemname count
					addItemToInventory(player,args[0],Integer.valueOf(args[1]));
					return true;
				}else{//item target id
					final Player target=getServer().getPlayer(args[0]);
						if (target!=null)
							addItemToInventory(target,args[1],1);
						else
							sender.sendMessage(ChatColor.GOLD+"could not find target player");
						return true;
				}
			}else{ //item target item 
				final Player target=getServer().getPlayer(args[0]);
				if (target!=null)
					addItemToInventory(target,args[1],1);
				else
					sender.sendMessage(ChatColor.GOLD+"could not find target player");
				return true;
			}	
		}else
		if (args.length==3){ //item target item count
			final Player target=getServer().getPlayer(args[0]);
			if (target!=null&&args[2].matches("^[0-9]+$"))
				addItemToInventory(target,args[1],Integer.valueOf(args[2]));
			else
				sender.sendMessage(ChatColor.GOLD+"could not find target player");
			return true;
		}
		
		sender.sendMessage(ChatColor.GOLD+"wrong arguments for /item");
		return true;
	}
	// time command
	private void timeCommand(Player player,CommandSender sender,String[] args) {
		Boolean ret=false;
		World world=null;

		if (player==null){ //=>console -> player==null
			if (args.length!=2) {
				return;
			}
			// time x world
			try{
				world=getServer().getWorld(args[1]);
			}catch(final Exception e){
				if (config.debug) {
					e.printStackTrace();
				}
				log.warning(pName+"(automatic timeset) world not found");ret=true;
			}
			if (ret||world==null) {
				return;
			}
			if (args[0].matches("^[0-9]+$")){
				world.setTime(Long.valueOf(args[0]));
				if (config.debug) {
					log.info(pDebug+"automatic timeset:"+world+" ("+args[0]+")");
				}
			}
			return;
		} // console end
		// player begin
		if (args.length==1) {
			world=player.getWorld();
		}
		if (args.length==2) {
			try{
				world=getServer().getWorld(args[1]);
			}catch(final Exception e){
				if (config.debug) {
					e.printStackTrace();
				}
				log.warning(pName+"world not found");
				player.sendMessage(ChatColor.GOLD+"could not find this world");ret=true;
			}
		}
		if (ret||world==null) {
			return;
		}
		if(args[0].equalsIgnoreCase("stop")&&PlayerHasPermission(player,"advancedTime")){
			world.setGameRuleValue("doDaylightCycle", "false");
			player.sendMessage(ChatColor.GOLD+"locked the time for world: "+world.getName());
			return;
		}
		if(args[0].equalsIgnoreCase("release")&&PlayerHasPermission(player,"advancedTime")){
			world.setGameRuleValue("doDaylightCycle", "true");
			player.sendMessage(ChatColor.GOLD+"the daylightcycle on "+world.getName()+" is normal again");
			return;
		}
		if (args[0].matches("^[0-9]+$")){
			world.setTime(Long.valueOf(args[0]));
			player.sendMessage(ChatColor.GOLD+"Time set to "+args[0]);
			return;
		}
		if (args[0].equalsIgnoreCase("day")){
			world.setTime(1000);
			player.sendMessage(ChatColor.GOLD+"Time set to 2000");
			return;
		}
		if (args[0].equalsIgnoreCase("night")){
			world.setTime(14000);
			player.sendMessage(ChatColor.GOLD+"Time set to 14000");
			return;
		}
		if (args[0].equalsIgnoreCase("sunrise")){
			world.setTime(22500);
			player.sendMessage(ChatColor.GOLD+"Time set to 22500");
			return;
		}
		if (args[0].equalsIgnoreCase("sunset")){
			world.setTime(12500);
			player.sendMessage(ChatColor.GOLD+"Time set to 12500");
			return;
		}
		
		sender.sendMessage(ChatColor.GOLD+"/time (timevalue) [world]");
		sender.sendMessage(ChatColor.GOLD+"/time day [world]");
		sender.sendMessage(ChatColor.GOLD+"/time night [world]");
		sender.sendMessage(ChatColor.GOLD+"/time stop [world]      -> stop the time");
		sender.sendMessage(ChatColor.GOLD+"/time release [world]  -> release the timestop");
	}

	//// subprocedures ////
	private Location getPosWherePlayerLooks(Player player){
		final List<Block> blocks=player.getLineOfSight(null, 50);
		Block playerLooksAt=null;
		Location target;
		for(int i=0;i<150;i++) {
			if (blocks.get(i).getType()!=Material.AIR){
				playerLooksAt=blocks.get(i);
				break;
			}
		}
		if (playerLooksAt==null){
			target=player.getLocation();
		} else {
			target=playerLooksAt.getRelative(0, 1, 0).getLocation();
		}
		return target;
	}
	private void spawnMob(Location target, String string, Integer count) {
		if (string.equalsIgnoreCase("skeleton")||string.contains("leton")||string.contains("Ske")||string.contains("ske")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.SKELETON);
			}
			if (config.debug) {
				log.info(pDebug+"spawning skeletons at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("creeper")||string.contains("eper")||string.contains("cre")||string.contains("Cre")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.CREEPER);
			}
			if (config.debug) {
				log.info(pDebug+"spawning creeper at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("zombie")||string.contains("om")&&!string.contains("pig")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.ZOMBIE);
			}
			if (config.debug) {
				log.info(pDebug+"spawning zombies at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("pigzombie")||string.equalsIgnoreCase("zombiepig")
				||string.contains("om")&&string.contains("pig")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.PIG_ZOMBIE);
			}
			if (config.debug) {
				log.info(pDebug+"spawning pigzombies at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("enderman")||string.contains("man")||string.contains("erma")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.ENDERMAN);
			}
			if (config.debug) {
				log.info(pDebug+"spawning enderman at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("blaze")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.BLAZE);
			}
			if (config.debug) {
				log.info(pDebug+"spawning blaze at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("magmacube")||string.contains("Mag")||string.contains("mag")||string.contains("cube")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.MAGMA_CUBE);
			}
			if (config.debug) {
				log.info(pDebug+"spawning magmacubes at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("slime")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.SLIME);
			}
			if (config.debug) {
				log.info(pDebug+"spawning slimes at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("pig")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.PIG);
			}
			if (config.debug) {
				log.info(pDebug+"spawning pigs at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("sheep")||string.contains("she")||string.contains("She")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.SHEEP);
			}
			if (config.debug) {
				log.info(pDebug+"spawning sheep at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("cow")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.COW);
			}
			if (config.debug) {
				log.info(pDebug+"spawning cows at "+target.toString());
			}
			return;
		}
		if (string.contains("cow")||string.contains("Cow")||string.contains("mush")||string.contains("Mush")||string.contains("moo")||string.contains("Moo")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.MUSHROOM_COW);
			}
			if (config.debug) {
				log.info(pDebug+"spawning mooshroomcows at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("huhn")||string.equalsIgnoreCase("chicken")||string.contains("icken")||string.contains("Chi")||string.contains("chi")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.CHICKEN);
			}
			if (config.debug) {
				log.info(pDebug+"spawning chicken at "+target.toString());
			}
			return;
		}

		if (string.equalsIgnoreCase("cavespider")||string.contains("cave")||string.contains("Cave")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.CAVE_SPIDER);
			}
			if (config.debug) {
				log.info(pDebug+"spawning cavespider at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("spider")||string.contains("spi")||string.contains("Spi")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.SPIDER);
			}
			if (config.debug) {
				log.info(pDebug+"spawning spider at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("ghast")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.GHAST);
			}
			if (config.debug) {
				log.info(pDebug+"spawning ghast at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("silverfish")||string.contains("sil")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.SILVERFISH);
			}
			if (config.debug) {
				log.info(pDebug+"spawning silverfish at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("enderdragon")||string.contains("drag")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.ENDER_DRAGON);
			}
			if (config.debug) {
				log.info(pDebug+"spawning enderdragon at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("giant")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.GIANT);
			}
			if (config.debug) {
				log.info(pDebug+"spawning giant zombie at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("snowman")||string.contains("sno")||string.contains("Sno")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.SNOWMAN);
			}
			if (config.debug) {
				log.info(pDebug+"spawning snowman at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("squid")||string.contains("sq")||string.contains("Sq")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.SQUID);
			}
			if (config.debug) {
				log.info(pDebug+"spawning squids at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("villager")||string.contains("vil")||string.contains("Vil")||string.contains("test")||string.contains("Test")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.VILLAGER);
			}
			if (config.debug) {
				log.info(pDebug+"spawning villager at "+target.toString());
			}
			return;
		}
		if (string.equalsIgnoreCase("wolf")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.WOLF);
			}
			if (config.debug) {
				log.info(pDebug+"spawning wolves at "+target.toString());
			}
			return;
		}
		if (string.contains("fleder")||string.contains("bat")){
			while(count>0){
				count--;
				target.getWorld().spawnEntity(target, EntityType.BAT);
			}
			if (config.debug) {
				log.info(pDebug+"spawning bats at "+target.toString());
			}
			return;
		}
		//add new mobs																				/////todo/////
		// horse
		// baby zombie
		// villager zombie
		// minecarts
		// entities (riding other entities)^n
		//
	}
	private void setWarp(String name, String type, Player player) {
		if (existWarp(name)){
			config.removeWarp(name);
			if (config.debug) {
				log.info(pDebug+"removing warp:"+name+" player:"+player.getName());
			}
		}
		if (config.debug) {
			log.info(pDebug+"adding warppoint:"+name+" type:"+type+" creator:"+player.getName()+" world:"+player.getWorld());
		}
		config.addWarp(name,type,player);
	}
	private boolean existWarp(String name) {
		return config.warps.containsKey(name);
	}

	private void addItemToInventory(Player target, String item, int ammount){
		if (item.contains(":")){
			Material mat=getItem(item);
			Short dmg=getDmg(item);
			if (mat!=null&&dmg!=null)
				target.getInventory().addItem(new ItemStack(mat,ammount,dmg));
			else
				target.sendMessage(ChatColor.GOLD+"could not find material or meta value");
		}else{
			if (item.matches("^[0-9]+$")){
				target.getInventory().addItem(new ItemStack(Integer.valueOf(item),ammount));
			}else{
				Material mat=Material.getMaterial(item);
				if (mat!=null)
					target.getInventory().addItem(new ItemStack(mat,ammount));
				else
					target.sendMessage(ChatColor.GOLD+"could not find material");
			}
		}
	}
	public Short getDmg(String string) {
		if (config.debug) {
			log.info(pDebug+"item(getDmg)= "+string);
		}
		if (!string.contains(":")||string.length()<3)
			return null;
		final String ret=string.substring(string.indexOf(":")+1);
		if (ret.length()>2||!ret.matches("^[0-9]+$")||ret.length()==0)
			return null;
		return Short.valueOf(ret);
	}
	public Material getItem(String string) {
		if (config.debug) {
			log.info(pDebug+"item(getItem)= "+string);
		}
		if (!string.contains(":")||string.length()<3)
			return null;
		final String ret=string.substring(0,string.indexOf(":"));
		if (ret==null||ret.length()==0)
			return null;
		if (ret.matches("^[0-9]+$")){
			return Material.getMaterial(Integer.valueOf(ret));
		}
		return Material.getMaterial(string);
	}
	
	public void sendLongMessage(Player player, String msg){
		sendLongMessage(player,msg,ChatColor.WHITE);
	}
	public void sendLongMessage(Player player, String msg, ChatColor color) {

		if (msg.length()>89){
			int lo=99,securecount=0;
			while (lo>89){
				lo=msg.substring(0, lo).lastIndexOf(" ")-1;
				if (config.debug) {
					log.info(pDebug+"dividing msg string; found space at "+String.valueOf(lo));
				}
				securecount++;
				if (securecount>20||lo<2) {
					break;
				}
			}
			if (color.equals(ChatColor.WHITE)) {
				player.sendMessage(msg.substring(0,lo));
			} else {
				player.sendMessage(color+msg.substring(0,lo));
			}
			if (lo>0) {
				sendLongMessage(player,msg.substring(lo+2),color);
			}
			if (config.debug) {
				log.info(pDebug+"dividing msg string; "+msg);
			}
		} else {
			player.sendMessage(color+msg);
		}

	}
	public boolean PlayerHasPermission(Player player, String permission){
		if (config.debug) {
			log.info(pDebug+"asking for permission: Player: "+player.getName()+" Permission: "+permission);
		}
		if (config.permGroup.isEmpty()) {
			return player.isOp();
		}
		final boolean admin=config.permGroup.get(permission);
		if (player.hasPermission(pBase+pAdmin+"all")) {
			return true;
		}
		if (player.hasPermission(pBase+pAdmin+permission)) {
			return true;
		}
		if (admin) {
			if (player.isOp()) {
				return true;
			} else{
				player.sendMessage(ChatColor.RED+"You have no "+ChatColor.WHITE+permission+" permission");
				return false;
			}
		}
		if (player.hasPermission(pBase+pUser+"all")) {
			return true;
		}
		if (player.hasPermission(pBase+pUser+permission)) {
			return true;
		}
		if (PlayerHasPermission(player,permission,""))
			return true;
		player.sendMessage(ChatColor.RED+"You have no "+ChatColor.WHITE+permission+" permission");
		if (config.debug) {
			log.info(pDebug+"no permission found");
		}
		return false;
	}
	public boolean PlayerHasPermission(Player player, String permission, String advanced){
		if (player.hasMetadata(permission)){
			player.removeMetadata(permission, this);
			return true;
		}
		if (player.hasMetadata(permission+"_"+advanced)){
			player.removeMetadata(permission+"_"+advanced, this);
			return true;
		}
		return false;
	}

	static public void setPlayerSpectator(Player player) {
		player.setAllowFlight(true);
		player.setCanPickupItems(false);
		player.setGameMode(GameMode.ADVENTURE);
		player.saveData();
	}
	static public void setPlayerAdventure(Player player){
		player.setAllowFlight(false);
		player.setCanPickupItems(true);
		player.setGameMode(GameMode.ADVENTURE);
		player.saveData();
	}
	static public void setPlayerNormal(Player player){
		player.setAllowFlight(false);
		player.setCanPickupItems(true);
		player.setGameMode(GameMode.SURVIVAL);
		player.saveData();
	}
}
