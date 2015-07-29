package de.mg127.TiaEssentials;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class Config {
	//public Logger log = Logger.getLogger("Minecraft");

	HashMap<String,WarpPoint> warps;
	// String => Name of the Warppoint

	HashMap<String,WarpPoint> graveyards=new HashMap<String,WarpPoint>();
	// String => [world]_[count]

	private HashMap<String,Integer> graveCounter=new HashMap<String,Integer>();
	// String => [world] ; Integer => actual count of graveyards on that world

	HashMap<String,WarpPoint> tempWarps=new HashMap<String,WarpPoint>();
	// String => Name of the Creator / used for asking the Creator / tempWarps.Name is the name of the parppoint

	private HashMap<String,Location> playerStartPoint=new HashMap<String,Location>();
	// active player respawn locations for tutorial
	
	public HashMap<String,ItemSet> ItemSets;
	// String => ItemSetname
	
	public HashMap<String,Boolean> permGroup;
	// String => permission-name
	
	public HashMap<String,Boolean> askPlayerList=new HashMap<String,Boolean>();
	// String => playername, bool => currently allways true for Warppoint overwrite, false is not used
	
	private HashMap<Player,Player> tpaPlayer=new HashMap<Player,Player>();
	// asking player / target player
	
	private HashMap<String,Location> oldLocations=new HashMap<String,Location>();
	// player, oldLocation (from setting to spectator or adventure)
		
	
	private boolean[][] tutorialSpawns= new boolean[10][10];
	private String tutorialWorld;
	private int tutorialDefaultX, tutorialDefaultY, tutorialDefaultZ, tutorialDeltaXZ;

	boolean preventFirespread,
	preventFiredecay,
	weatherChange,
	debug,
	graveyard,
	//atLeastOneGraveyard,
	useSpecialEffects;


	private TiaEssentials plugin;
	public Config(TiaEssentials instance){
		plugin=instance;
	}
	public void checkConfig() {
		FileConfiguration con=plugin.getConfig();
		if (con.getKeys(false).isEmpty()){
			con.options().copyDefaults(true);
			plugin.saveConfig();
		}
		loadkeys();
		if (warps.isEmpty())
			plugin.log.warning(plugin.pName+"warps not loaded");
		if (this.ItemSets.isEmpty())
			plugin.log.warning(plugin.pName+"itemsets not loaded");
		if (permGroup.isEmpty())
			plugin.log.warning(plugin.pName+"permission groups not loaded");
	}
	private void loadkeys() {
		FileConfiguration con=plugin.getConfig();

		warps=new HashMap<String,WarpPoint>();
		ItemSets=new HashMap<String,ItemSet>();
		permGroup=new HashMap<String,Boolean>();

		Set<String> allSettings = null;
		allSettings=con.getConfigurationSection("settings").getKeys(false);
		boolean changedConfig=false;
		this.graveCounter.clear();

		if (!allSettings.contains("PreventWeatherChange")){
			changedConfig=true;
			con.set("settings.PreventWeatherChange", true);}
		if (!allSettings.contains("debug")){
			changedConfig=true;
			con.set("settings.debug", true);}
		if (!allSettings.contains("useGraveyards")){
			changedConfig=true;
			con.set("settings.useGraveyards", true);}
		if (!allSettings.contains("useSpecialEffects")){
			changedConfig=true;
			con.set("settings.useSpecialEffects", true);}
		if (!allSettings.contains("tutorial")){
			changedConfig=true;
			con.set("settings.tutorial.defaultX", 0);
			con.set("settings.tutorial.defaultY", 72);
			con.set("settings.tutorial.defaultZ", 0);
			con.set("settings.tutorial.deltaXZ", 500);
		}

		weatherChange=con.getBoolean("settings.PreventWeatherChange",true);
		debug=con.getBoolean("settings.debug",false);
		graveyard=con.getBoolean("settings.useGraveyards",true);
		useSpecialEffects=con.getBoolean("settings.useSpecialEffects",true);
		this.tutorialDefaultX=con.getInt("settings.tutorial.defaultX");
		this.tutorialDefaultY=con.getInt("settings.tutorial.defaultY");
		this.tutorialDefaultZ=con.getInt("settings.tutorial.defaultZ");
		this.tutorialDeltaXZ=con.getInt("settings.tutorial.deltaXZ");

		if (!allSettings.contains("PermissionDefaults")){
			changedConfig=true;
			con.set("settings.PermissionDefaults.config", true);
			con.set("settings.PermissionDefaults.item", true);
			con.set("settings.PermissionDefaults.tpa", false);
			con.set("settings.PermissionDefaults.tp", true);
			con.set("settings.PermissionDefaults.tpp", true);
			con.set("settings.PermissionDefaults.tph", true);
			con.set("settings.PermissionDefaults.warp", false);
			con.set("settings.PermissionDefaults.setwarp", true);
			con.set("settings.PermissionDefaults.listwarps", false);
			con.set("settings.PermissionDefaults.spawn", false);
			con.set("settings.PermissionDefaults.setspawn", true);
			con.set("settings.PermissionDefaults.home", false);
			con.set("settings.PermissionDefaults.sethome", false);
			con.set("settings.PermissionDefaults.getid", true);
			con.set("settings.PermissionDefaults.time", true);
			con.set("settings.PermissionDefaults.advancedTime", true);
			con.set("settings.PermissionDefaults.spawnmob", true);
			con.set("settings.PermissionDefaults.msg", false);
			con.set("settings.PermissionDefaults.r", false);
			con.set("settings.PermissionDefaults.gamemode", true);
			con.set("settings.PermissionDefaults.advancedHome", true);
			con.set("settings.PermissionDefaults.advancedSethome", true);
			con.set("settings.PermissionDefaults.advancedWarp", true);
			con.set("settings.PermissionDefaults.graveyard", true);
			con.set("settings.PermissionDefaults.clr", true);
			con.set("settings.PermissionDefaults.useCommandBlock", true);
			//con.set("settings.PermissionDefaults.", false);
		}
		if (changedConfig) {
			plugin.saveConfig();
		}

		//warps,homes,spawn
		//name=anything without '_' or home_playername or spawn_worldname
		//type=private,public,home,spawn
		
		warps=getWarpsFromFile(con,"warps");
		
		graveyards=getWarpsFromFile(con,"graveyards");
		
		WarpPoint warp=new WarpPoint();
		for(Iterator<String> i=graveyards.keySet().iterator();i.hasNext();warp=graveyards.get(i.next())){
			String world=warp.location.getWorld().getName();
			if (!graveCounter.containsKey(world))
				graveCounter.put(world, 1);
			else
				graveCounter.put(world, graveCounter.get(world)+1);
		}
		
		if (con.getKeys(false).contains("ItemSets")){
			ConfigurationSection section=con.getConfigurationSection("ItemSets");
			if (section!=null){
				Set<String> ItemList=section.getKeys(false);
				if (ItemList!=null) {
					if (!ItemList.isEmpty()) {
						if (ItemList.size()>0){
							Iterator<String> setIter=ItemList.iterator();
							String setNode;

							while(setIter.hasNext()){
								setNode=setIter.next();
								ItemStack stacks[]={
										getItem(setNode,1),
										getItem(setNode,2),
										getItem(setNode,3),
										getItem(setNode,4),
										getItem(setNode,5),
										getItem(setNode,6),
										getItem(setNode,7),
										getItem(setNode,8),
										getItem(setNode,9)};
								ItemSets.put(setNode,new ItemSet(stacks));
							}
						}
					}
				}
			}
		}
		if (con.getKeys(false).contains("PermissionDefaults")){
			ConfigurationSection section=con.getConfigurationSection("PermissionDefaults");
			if (section!=null) {
				if (section.getKeys(false)!=null) {
					if (section.getKeys(false).size()>0){
						permGroup.put("config",con.getBoolean("settings.PermissionDefaults.config", true));
						permGroup.put("item",con.getBoolean("settings.PermissionDefaults.item", true));
						permGroup.put("false",con.getBoolean("settings.PermissionDefaults.tpa", false));
						permGroup.put("tp",con.getBoolean("settings.PermissionDefaults.tp", true));
						permGroup.put("tpp",con.getBoolean("settings.PermissionDefaults.tpp", true));
						permGroup.put("tph",con.getBoolean("settings.PermissionDefaults.tph", true));
						permGroup.put("warp",con.getBoolean("settings.PermissionDefaults.warp", false));
						permGroup.put("setwarp",con.getBoolean("settings.PermissionDefaults.setwarp", true));
						permGroup.put("listwarps",con.getBoolean("settings.PermissionDefaults.listwarps", false));
						permGroup.put("spawn",con.getBoolean("settings.PermissionDefaults.spawn", false));
						permGroup.put("setspawn",con.getBoolean("settings.PermissionDefaults.setspawn", true));
						permGroup.put("home",con.getBoolean("settings.PermissionDefaults.home", false));
						permGroup.put("sethome",con.getBoolean("settings.PermissionDefaults.sethome", false));
						permGroup.put("getid",con.getBoolean("settings.PermissionDefaults.getid", true));
						permGroup.put("time",con.getBoolean("settings.PermissionDefaults.time", true));
						permGroup.put("advancedTime",con.getBoolean("settings.PermissionDefaults.advancedTime", true));
						permGroup.put("spawnmob",con.getBoolean("settings.PermissionDefaults.spawnmob", true));
						permGroup.put("msg",con.getBoolean("settings.PermissionDefaults.msg", false));
						permGroup.put("r",con.getBoolean("settings.PermissionDefaults.r", false));
						permGroup.put("gamemode",con.getBoolean("settings.PermissionDefaults.gamemode", true));
						permGroup.put("advancedHome",con.getBoolean("settings.PermissionDefaults.advancedHome", true));
						permGroup.put("advancedSethome",con.getBoolean("settings.PermissionDefaults.advancedSethome", true));
						permGroup.put("advancedWarp",con.getBoolean("settings.PermissionDefaults.advancedWarp", true));
						permGroup.put("graveyard",con.getBoolean("settings.PermissionDefaults.graveyard", true));
						permGroup.put("clr",con.getBoolean("settings.PermissionDefaults.clr", true));
						permGroup.put("useCommandBlock",con.getBoolean("settings.PermissionDefaults.useCommandBlock", true));
						//permGroup.put("",con.getBoolean("settings.PermissionDefaults.", true));
					}
				}
			}
		}
		if (con.getKeys(false).contains("tutorial")){
			ConfigurationSection section=con.getConfigurationSection("tutorial");
			if (section!=null){
				if (section.getKeys(false)!=null){
					if (section.getKeys(false).size()>0){
						Set<String> players=section.getKeys(false);
						Iterator<String> playerIter=players.iterator();
						String playerName;

						while (playerIter.hasNext()){
							playerName=playerIter.next();
							Player player=plugin.getServer().getPlayer(playerName);
							player.setMetadata("tutorial",new FixedMetadataValue(plugin,"true"));
						}
					}
				}
			}
		}
		
		//status.playerName.status:
		//status.playerName.lastLocationWP:
		if (con.getKeys(false).contains("status")){
			ConfigurationSection section=con.getConfigurationSection("status");
			if (section!=null){
				if (section.getKeys(false)!=null){
					if (section.getKeys(false).size()>0){
						Set<String> players=section.getKeys(false);
						Iterator<String> playerIter=players.iterator();
						String playerName;

						while (playerIter.hasNext()){
							playerName=playerIter.next();
							Player player=plugin.getServer().getPlayer(playerName);
							String status=con.getString("status."+playerName+".status");
							if (status.equals("spectator")) {
								TiaEssentials.setPlayerSpectator(player);
							}
							if (status.equals("adventure")) {
								TiaEssentials.setPlayerAdventure(player);
							}
						}
					}
				}
			}
		}
	}
	private HashMap<String, WarpPoint> getWarpsFromFile(FileConfiguration con, String input) {
		HashMap<String,WarpPoint> warps=new HashMap<String,WarpPoint>();
		
		if (con.getKeys(false).contains(input)){
			ConfigurationSection section=con.getConfigurationSection(input);
			if (section!=null) {
				if (section.getKeys(false)!=null) {
					if (section.getKeys(false).size()>0){
						Set<String> allWarps=section.getKeys(false);

						Iterator<String> warpIter=allWarps.iterator();
						String warpNode;

						while(warpIter.hasNext()){
							warpNode=warpIter.next();
							warps.put(warpNode, //name of the warp point, internal and shortcut
									new WarpPoint(//creates a new instance of the custom class
											section.getString(warpNode+".type"), //private (global), public: global, local(world), utility
											section.getString(warpNode+".creator"), //player
											section.getString(warpNode+".name"), //display name
											getWorld(section, warpNode),//get world by uuid or name
											section.getDouble(warpNode+".x"),
											section.getDouble(warpNode+".y"),
											section.getDouble(warpNode+".z"),
											section.getDouble(warpNode+".P"),//pitch
											section.getDouble(warpNode+".Y")));//jaw
						}
					}
				}
			}
		}
		
		return warps;
	}
	private World getWorld(ConfigurationSection con, String warpNode) {
		if (con.contains(warpNode+".worlduuid"))
			return plugin.getServer().getWorld(UUID.fromString(con.getString(warpNode+".worlduuid")));
		else
			return plugin.getServer().getWorld(con.getString(warpNode+".world"));
	}
	public void removeWarp(String name) {
		loadkeys();
		warps.remove(name);
		FileConfiguration con=plugin.getConfig();
		if (con.getConfigurationSection("warps").contains(name))
		{
			con.set("warps", null);
			if (!warps.isEmpty()) {
				if (warps.size()>0) {
					for (String key : warps.keySet()){
						con.set("warps."+key+".type", warps.get(key).Type);
						con.set("warps."+key+".creator", warps.get(key).Creator);
						con.set("warps."+key+".world", warps.get(key).location.getWorld().getName());
						con.set("warps."+key+".worlduuid", warps.get(key).location.getWorld().getUID().toString());
						con.set("warps."+key+".x", warps.get(key).location.getX());
						con.set("warps."+key+".y", warps.get(key).location.getY());
						con.set("warps."+key+".z", warps.get(key).location.getZ());
						con.set("warps."+key+".P", warps.get(key).location.getPitch());
						con.set("warps."+key+".Y", warps.get(key).location.getYaw());
					}
				}
			}
			plugin.saveConfig();
		}
	}
	public void addWarp(String name, String type, Player player) {
		addWarp(new WarpPoint(type,player,name),name);
	}
	public void addWarp(WarpPoint wp,String name)
	{
		loadkeys();
		//String name=wp.LongName;
		warps.put(name, wp);
		FileConfiguration con=plugin.getConfig();
		con.set("warps."+name+".type", wp.Type);
		con.set("warps."+name+".creator", wp.Creator);
		con.set("warps."+name+".world", wp.location.getWorld().getName());
		con.set("warps."+name+".worlduuid", wp.location.getWorld().getUID().toString());
		con.set("warps."+name+".name", wp.LongName);
		con.set("warps."+name+".x", wp.location.getX());
		con.set("warps."+name+".y", wp.location.getY()+0.1);
		con.set("warps."+name+".z", wp.location.getZ());
		con.set("warps."+name+".P", wp.location.getPitch());
		con.set("warps."+name+".Y", wp.location.getYaw());
		plugin.saveConfig();
	}
	public void addTempWarp(String name, String type, Player player){
		tempWarps.put(player.getName(), new WarpPoint(type,player,"tmp_"+player.getName()));
	}
	public void overwriteWarp(String creator){
		String name=tempWarps.get(creator).LongName;
		if (warps.containsKey(name)){
			warps.remove(name);
		}
		addWarp(tempWarps.get(creator),name);
		tempWarps.remove(creator);
	}
	public void weather(boolean status) {
		loadkeys();
		FileConfiguration con=plugin.getConfig();
		con.set("settings.WeatherChange", status);
		plugin.saveConfig();
		weatherChange=status;
	}
	private ItemStack getItem(String node,int pos) {
		FileConfiguration con=plugin.getConfig();
		String items=con.getString("ItemSets."+node+"."+Integer.toString(pos));
		int count=1,id=0;
		short dmg=0;
		boolean endsX = false,endsDP = false;
		if (items.contains("x")){
			count=Integer.valueOf(items.substring(items.indexOf("x")+1));
			endsX=true;
		}
		if (items.contains(":")&&items.contains("x")){
			dmg=Short.valueOf(items.substring(items.indexOf(":")+1, items.indexOf("x")));
			endsDP=true;endsX=false;
		}
		else if (items.contains(":")){
			dmg=Short.valueOf(items.substring(items.indexOf(":")+1));
			endsDP=true;
		}
		if (endsDP) {
			id=Integer.valueOf(items.substring(0,items.indexOf(":")));
		} else
			if (endsX) {
				id=Integer.valueOf(items.substring(0,items.indexOf("x")));
			} else {
				id=Integer.valueOf(items);
			}

		return new ItemStack(Material.getMaterial(id),count,dmg);
	}
	public void newItemSet(String node, ItemStack[] set){
		loadkeys();
		if (ItemSets.containsKey(node)) {
			ItemSets.remove(node);
		}
		ItemSets.put(node,new ItemSet(set));
		FileConfiguration con=plugin.getConfig();
		ItemStack item;
		for (int i=0;i<9;i++){
			item=set[i];
			con.set("ItemSets."+node+"."+(i+1),item.getType()+":"+item.getData().getItemType()+"x"+item.getAmount());
		}
		plugin.saveConfig();
	}
	public boolean existsItemSet(String name){
		return ItemSets.containsKey(name);
	}
	public void delItemSet(String node){
		loadkeys();
		if (ItemSets.containsKey(node)){
			ItemSets.remove(node);

			FileConfiguration con=plugin.getConfig();
			if (con.getConfigurationSection("ItemSets").contains(node))
			{
				con.set("ItemSets", null);
				if (!ItemSets.isEmpty()) {
					if (ItemSets.size()>0) {
						for (String key : ItemSets.keySet()) {
							ItemStack[] items=ItemSets.get(key).items;
							ItemStack item;
							for (int i=0;i<9;i++){
								item=items[i];
								con.set("ItemSets."+node+"."+(i+1),item.getType()+":"+item.getData().getItemType()+"x"+item.getAmount());
							}
						}
					}
				}
				plugin.saveConfig();
			}
		}
	}
	//tpa
	public void tpaAskPlayer(Player sender,Player target){
		tpaPlayer.put(target, sender);
	}
	public void tpaAnswer(Player target){
		if (tpaPlayer.keySet().contains(target)){
			Player sender=tpaPlayer.get(target);
			sender.teleport(target.getLocation());
			tpaPlayer.remove(target);
			target.sendMessage(ChatColor.GREEN+"tpa accepted.");
			sender.sendMessage(ChatColor.GREEN+"Your tpa has been accepted.");
		} else {
			target.sendMessage(ChatColor.GREEN+"You have no tpa-request active.");
		}
	}
	//graveyards
	public Location getNearestGraveyard(Location loc) {
		WarpPoint target=null;
		int mindistance=9999999;
		String world=loc.getWorld().getName();
		int x1=loc.getBlockX();
		int z1=loc.getBlockZ();
		for (int i=1;i<this.graveCounter.get(world);i++){
			WarpPoint warp=graveyards.get(world+"_"+i);
			int x2=warp.location.getBlockX();
			int z2=warp.location.getBlockZ();
			int distance=Math.abs(x1-x2)+Math.abs(z1-z2);
			if (distance<mindistance){
				mindistance=distance;
				target=warp;
			}
		}
		return target.location;
	}
	public String getNearestGraveyardName(Location loc) {
		//WarpPoint target=null;
		int i=1;
		int mindistance=9999999;
		String world=loc.getWorld().getName();
		int x1=loc.getBlockX();
		int z1=loc.getBlockZ();
		//log.info(world+"; "+graveCounter.toString());
		for (;i<this.graveCounter.get(world);i++){
			WarpPoint warp=graveyards.get(world+"_"+i);
			int x2=warp.location.getBlockX();
			int z2=warp.location.getBlockZ();
			int distance=Math.abs(x1-x2)+Math.abs(z1-z2);
			if (distance<mindistance){
				mindistance=distance;
				//target=warp;
			}
		}
		return world+"_"+i;
	}
	public boolean existGraveyard(String world) {
		return graveyards.get(world+"_0")!=null; //if there is at least one graveyard on this world, there is one with "_0"
	}
	public void addGraveyard(WarpPoint wp){
		addGraveyard(wp,wp.LongName);
	}
	public void addGraveyard(WarpPoint wp,String name){
		loadkeys();
		graveyards.put(name, wp);
		FileConfiguration con=plugin.getConfig();
		con.set("graveyards."+name+".creator", wp.Creator);
		con.set("graveyards."+name+".world", wp.location.getWorld().getName());
		con.set("graveyards."+name+".worlduuid", wp.location.getWorld().getUID().toString());
		con.set("graveyards."+name+".name",wp.LongName);
		con.set("graveyards."+name+".x", wp.location.getX());
		con.set("graveyards."+name+".y", wp.location.getY()+0.1);
		con.set("graveyards."+name+".z", wp.location.getZ());
		con.set("graveyards."+name+".P", wp.location.getPitch());
		con.set("graveyards."+name+".Y", wp.location.getYaw());
		plugin.saveConfig();
	}
	public void removeGraveyard(String grave){
		loadkeys();
		if (!this.graveyards.containsKey(grave)) {
			return;
		}
		String world=graveyards.get(grave).location.getWorld().getName();
		int i=Integer.valueOf(grave.substring(grave.indexOf("_")+1));
		while(i<this.graveCounter.get(world)){
			this.graveyards.put(world+i, this.graveyards.get(world+i+1));
			i++;
		}
		this.graveyards.remove(i);
		graveCounter.put(world, i--);
		
		
		FileConfiguration con=plugin.getConfig();
		if (con.getConfigurationSection("graveyards").contains(grave))
		{
			con.set("graveyards", null);
			int isOrd=0;
			String lastWorld="";
			if (!graveyards.isEmpty()) {
				if (graveyards.size()>0) {
					for (String key : graveyards.keySet()){
						String key2=graveyards.get(key).location.getWorld().getName();
						String key3=key.replaceFirst(key2+"_", "");
						if (lastWorld!=key2){
							lastWorld=key2;
							isOrd=0;
						}
						int ord=Integer.valueOf(key3);
						String nkey=key;
						if (ord>isOrd){
							nkey=key2+"_"+isOrd;
						}
						isOrd++;
						con.set("graveyards."+nkey+".creator", graveyards.get(key).Creator);
						con.set("graveyards."+nkey+".world", graveyards.get(key).location.getWorld().getName());
						con.set("graveyards."+nkey+".worlduuid", graveyards.get(key).location.getWorld().getUID().toString());
						con.set("graveyards."+nkey+".x", graveyards.get(key).location.getX());
						con.set("graveyards."+nkey+".y", graveyards.get(key).location.getY());
						con.set("graveyards."+nkey+".z", graveyards.get(key).location.getZ());
						con.set("graveyards."+nkey+".P", graveyards.get(key).location.getPitch());
						con.set("graveyards."+nkey+".Y", graveyards.get(key).location.getYaw());
					}
				}
			}
			plugin.saveConfig();
		}
		loadkeys();
	}
	public int newGraveOrd(String world) {
		if (!graveCounter.containsKey(world)){
			graveCounter.put(world, 0);
		}
		int ord=graveCounter.get(world);
		ord++;
		this.graveCounter.put(world,ord);
		return ord;
	}
	//tutorial
	public void CheckNewTutorialStatus(Player player) {
		if (this.tutorialWorld==null) {
			return;
		}
		if (!player.hasMetadata("knownplayer")){
			player.setMetadata("knownplayer",new FixedMetadataValue(plugin,"kp"));
			if (warps.containsKey("tutorial"))
				player.teleport(warps.get("tutorial").location);
		/*	Location target=null;
			boolean ready=false;

			for(int z=0;z<10;z++){
				for(int x=0;x<10;x++){
					if (!this.tutorialSpawns[x][z]){
						ready=true;
						target=new Location(plugin.getServer().getWorld(tutorialWorld),
								tutorialDeltaXZ*x+tutorialDefaultX,
								tutorialDefaultY,
								tutorialDeltaXZ*z+tutorialDefaultZ);
						this.tutorialSpawns[x][z]=true;
						break;
					}
				}
				if(ready) {
					break;
				}
			}
			if (target!=null&&ready){
				playerStartPoint.put(player.getName(), target);
				player.teleport(target);
				//todo: write to disk 									/////todo//////
				//      read tutorial default values
				return;
			}else{
				//todo: alternative when 10x10 is full
			}*/
		}
	}
	public Location getTutorialRespawn(String playerName) {
		return this.playerStartPoint.get(playerName);
	}
	public void savePlayerStatus(Player player) {
		// save player metadata 									/////todo//////
		// save player position
		// save in config
	}
	public void restorePlayerStatus(Player player) {
		// restore player position
		player.teleport(this.oldLocations.get(player.getName()));
		// remove oldLocation from config 									/////todo//////
	}
}