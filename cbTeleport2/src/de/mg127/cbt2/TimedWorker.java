package de.mg127.cbt2;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class TimedWorker implements Runnable{
	
	Config config;
	public TimedWorker(Config config){
		this.config=config;
	}
	private int timer=0;
	
	public void run(){
		timer++;
		if (timer>config.delayAllCheck){
			transferAllEntities();
			timer=0;
		}
	}
	
	private void transferAllEntities(){
		
		Iterator<World> worlds = Bukkit.getServer().getWorlds().iterator();
		while (worlds.hasNext()){
			World world=worlds.next();
			if (config.worldHasPort(world.getName())){
				Iterator<Player> players= world.getPlayers().iterator();
				while (players.hasNext()){
					Player player=players.next();
					Location loc=player.getLocation();
					if (config.pThresh(loc)){
						Entity[] entities=loc.getChunk().getEntities();
						for(int i=0;i<entities.length;i++){
							config.checkTeleport(entities[i]);
						}
					}
				}
			}
		}
	}
}
