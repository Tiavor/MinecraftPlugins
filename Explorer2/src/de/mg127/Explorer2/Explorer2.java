package de.mg127.Explorer2;

import java.util.ConcurrentModificationException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Explorer2 extends JavaPlugin implements Listener{
	public Logger log = Logger.getLogger("Minecraft");
	BukkitScheduler timer=Bukkit.getServer().getScheduler();
	
	int radius,x=0,y=0,quadrant=0,speed=10,count=0,totalcount=0,total,step=7;
	double angle=0,innerradius=0;
	boolean round,pause,lock,deactivated,radial;
	Player player;
	Location loc,centerLoc;
	Server server;
	World world;

	public void onEnable() {
		server=this.getServer();
		server.getPluginManager().registerEvents(this, this);
	}
	public void onDisable() {
		
	}
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event)
    {
    	if (event.isCancelled())
    		return;
    	if (event.getPlayer().getName().equals(player.getName()))
    	{
    		event.setCancelled(true);
    	}
    }
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		pause=true;
	}
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){ 
		try{
			if(cmd.getName().equalsIgnoreCase("expl")){
				// /expl [radius] [world] ['r'] ['c']
				if (sender.isOp()){
					player=(Player)sender;
					// explore 2000 [r]
					// explore pause
					// explore speed 20
					if (args.length>0){
						for (int i=0;i<args.length;i++){
							if ((!round)&&args[i].equalsIgnoreCase("round")){
								round=true;radial=false;
							}else
							if (args[i].matches("^[0-9]+$")){
								radius=Math.round(Integer.valueOf(args[i])/16);
							}else
							if (args[i].equalsIgnoreCase("q")&&(!radial)){
								int q=Integer.valueOf(args[i+1]);
								if (q<0||q>3)
									return true;
								quadrant=q;
								x=0;y=0;
								sender.sendMessage(ChatColor.AQUA+"Quadrant Changed");
								return true;
							}else
							if (args[i].equalsIgnoreCase("step")&&(radial)){
								step=Integer.valueOf(args[i+1]);
								sender.sendMessage(ChatColor.AQUA+"Step Changed to "+step+", default=7");
								if (step<1)
									step=1;
								return true;
							}else
							if (args[i].equalsIgnoreCase("orbital")&&(!round)){
								if (args.length>i+1)
								if (args[i+1].matches("^[0-9]+$")){
									if (args[i+2].matches("^[0-9]+$")){
										radius=Math.round(Integer.valueOf(args[i+2])/16);
										innerradius=Integer.valueOf(args[i+1])/16;
										radial=true;
										sender.sendMessage(ChatColor.AQUA+"Exploration started, will be orbital.");
										break;
									}
								}
								return true;
							}else
							if (args[i].equalsIgnoreCase("pause")){
								if (pause){
									pause=false;
									sender.sendMessage(ChatColor.AQUA+"Exploration continued");
									world.save();
									startExploring();
									break;
								}
								else{
									pause=true;
									sender.sendMessage(ChatColor.AQUA+"Exploration paused");
									return true;
								}
							}else
							if (args[i].equalsIgnoreCase("speed")){
								if (args.length>i)
									try{
										speed=Integer.valueOf(args[i+1]);
										sender.sendMessage(ChatColor.AQUA+"changed speed to "+speed+", default=10");
										return true;
									}catch (Exception e){
										sender.sendMessage(ChatColor.AQUA+"invalid speed");
										return true;
									}
							}else
							if (args[i].equalsIgnoreCase("help")&&args.length==1){
								sender.sendMessage(ChatColor.AQUA+"/expl help , /expl help settings , /expl help orbital");
								sender.sendMessage(ChatColor.YELLOW+"/expl [radius] "+ChatColor.AQUA+"to explore a square quadrant by quadrant");
								sender.sendMessage(ChatColor.YELLOW+"/expl [radius] round "+ChatColor.AQUA+"to explore a circle quadrant by quadrant");
								sender.sendMessage(ChatColor.YELLOW+"/expl orbital [innerRadius] [outerRadius] "+ChatColor.AQUA+"to explore a circle orbital");
								sender.sendMessage(ChatColor.YELLOW+"/expl pause "+ChatColor.AQUA+"toggle stop and continuing of the exploration");
								sender.sendMessage(ChatColor.YELLOW+"            "+ChatColor.AQUA+"settings may be adjusted while paused");
								return true;
							}else
							if (args[i].equalsIgnoreCase("help")&&args.length>1){
								if (args[i+1].equalsIgnoreCase("settings")){
								sender.sendMessage(ChatColor.YELLOW+"/expl q [0-3] "+ChatColor.AQUA+"set the actual quadrant (not for orbital method)");
								sender.sendMessage(ChatColor.YELLOW+"/expl speed [5-15]"+ChatColor.AQUA+"speed*2 = seconds between teleports, default=10");
								sender.sendMessage(ChatColor.YELLOW+""+ChatColor.AQUA+"do not go below 5, it may cause errors and freeze the server");
								sender.sendMessage(ChatColor.YELLOW+"/expl steps [5-20] "+ChatColor.AQUA+"will change the distance between two teleports");
								sender.sendMessage(ChatColor.YELLOW+""+ChatColor.AQUA+"default=7");
								return true;
								}if (args[i+1].equalsIgnoreCase("orbital")){
								sender.sendMessage(ChatColor.YELLOW+"/expl orbital [innerRadius] [outerRadius] "+ChatColor.AQUA+"");
								sender.sendMessage(ChatColor.YELLOW+""+ChatColor.AQUA+"will explore from innerRadius to outerRadius by circumnavigate the map");
								sender.sendMessage(ChatColor.YELLOW+""+ChatColor.AQUA+"innerRadius cannot be below 5");
								return true;
								}
							}
							else
								return true;
						}
					}else
						return true;
					if (lock)
						return true;
					sender.sendMessage(ChatColor.AQUA+"To boldy go where ...");
					log.info("Started exploring of world "+player.getWorld());
					world=player.getWorld();
					world.save();
					server.savePlayers();
					centerLoc=player.getLocation();
					lock=true;
					if (radial){
						float counter=0;
						double ir=innerradius;
						double an=angle;
						while (an<360&&ir<radius){
							if (an>=360){
								an=0;
								ir+=5;
								counter+=speed/2;
							}else{
								an+=1/(step*180/(Math.PI*ir));
								counter+=speed/2;
							}
						}
						total=(int) (counter/(speed/2));
						totalcount=0;
						String time="";
						if (counter>0)
							time=" "+(int)counter%60+"s"+time;
						counter=(counter-counter%60)/60;
						if (counter>0)
							time=" "+(int)counter%60+"m"+time;
						counter=(counter-counter%60)/60;
						if (counter>0)
							time=" "+(int)counter%24+"h"+time;
						counter=(counter-counter%24)/24;
						if (counter>0)
							time=" "+(int)counter+"d"+time;
						sender.sendMessage(ChatColor.AQUA+"this will take around"+time);
					}
					startExploring();
				}
				else
					sender.sendMessage(ChatColor.AQUA+"You need to be OP to send you exploring.");
				return true;
			}
		}
		catch(Exception e) {
	        log.severe("[explorer]: shit happend");
	        e.printStackTrace();
		}return false;
	}
	private void startExploring() {
		if (deactivated||pause)
			return;
		timer.scheduleAsyncDelayedTask(this, new Runnable(){
			public void run() {
				teleport();
			}
		},speed*10);
	}

	private void teleport() {
		if (!player.isOnline())
			return;
		if (deactivated||pause)
			return;
		if (innerradius<5)
			innerradius=5;
		if (radial){
			if (angle>=360&&innerradius>=radius){
			//player.sendMessage("Explored "+totalcount/total);
	    		deactivated=true;
	    		server.broadcastMessage(ChatColor.AQUA+"exploration completed!");
	    		world.save();
	    		player.teleport(centerLoc);
	    		lock=false;
	    		player=null;
	    		//unload();
	    		return;
	    	}else
			if (angle>=360){
				angle=0;
				innerradius+=5;
    			world.save();
    			server.broadcastMessage(ChatColor.AQUA+"Radius is now "+(int)innerradius*16);
			}else{
				angle+=step*180/(Math.PI*innerradius);
			}
			double a=Math.sin(angle)*innerradius;
			double b=Math.cos(angle)*innerradius;
			if (angle>90&&angle<=180){
				a=-a;
			}else if (angle>180&&angle<=270){
				a=-a;
				b=-b;
			}else if (angle>270){
				a=-a;
			}
			x=(int) a;
			y=(int) b;
		}else
			
		if (quadrant==0){  //x+  y+
    		if (x>=radius&&y>=radius){/*
    			quadrant=1;
    			x=0;y=-step;
    			server.broadcastMessage(ChatColor.AQUA+"explored 25%");
    			world.save();*/
    			
    			//from q=3
        		deactivated=true;
        		server.broadcastMessage(ChatColor.AQUA+"exploration completed!");
        		world.save();
        		player.teleport(centerLoc);
        		lock=false;
        		player=null;
        		return;
    		}
    		else
    			if(x>=radius){
    				x=0;
    				y+=5;
    				//unload();
    			}else{
    				x+=5;
    			}
    		
    	}else if(quadrant==1){ //x+ y-
    		if (x>=radius&&y<=-radius){
    			quadrant=2;
    			x=-step;y=0;
    			server.broadcastMessage(ChatColor.AQUA+"explored 50%");
    			world.save();
    			//unload();
    		}
    		else
    			if(x>=radius){
    				x=0;
    				y-=step;
    				//unload();
    			}else{
    				x+=step;
    			}
    		
    	}else if (quadrant==2){//x- y-
    		if (x<=-radius&&y<=-radius){
    			quadrant=3;
    			x=-step;y=-step;
    			server.broadcastMessage(ChatColor.AQUA+"explored 75%");
    			world.save();
    			//unload();
    		}
    		else
    			if(x<=-radius){
    				x=0;
    				y-=step;
    				//unload();
    			}else{
    				x-=step;
    			}
    		
    	}else if (quadrant==3){//x- y+
    		if (x<=-radius&&y>=radius){
    			deactivated=true;
    			server.broadcastMessage(ChatColor.AQUA+"exploration completed!");
    			world.save();
    			player.teleport(centerLoc);
    			lock=false;
    			player=null;
    			//unload();
    			return;
    		}
    		else
    			if(x<=-radius){
    				x=0;
    				y+=step;
    				//unload();
    			}else{	
    				x-=step;
    			}	
    	}
    	if (round&&(!radial)){
    		double r=Math.sqrt(x*x+y*y);
    		if (r>radius){
    			int sp=speed;
    			speed=1;
    	    	startExploring();
    	    	speed=sp;
    	    	return;
    		}
    	}
    	//if (count>10)
    		//unload();
    	loc=new Location(player.getWorld(),x*16+centerLoc.getX(),145,y*16+centerLoc.getZ());
    	loc.setPitch(player.getLocation().getPitch());
    	loc.setYaw(player.getLocation().getYaw());
    	player.teleport(loc);
	    startExploring();
	    count+=1;
	    totalcount+=1;
	}
	@SuppressWarnings("unused")
	private void unload() {
		try{
		Chunk[] chunks=world.getLoadedChunks();
		int i;
		for(i=0;i<chunks.length;i++){
			if (chunks[i].isLoaded())
				chunks[i].unload(true,true);
		}
		}catch(ConcurrentModificationException e){
			log.warning("[Explorer]: an error occured, i'm not shure if all chunks are saved.");
		}
		count=0;
	}
}
