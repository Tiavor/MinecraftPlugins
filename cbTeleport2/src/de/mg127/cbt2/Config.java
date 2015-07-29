package de.mg127.cbt2;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Config {

	public long delayPosCheck=10;
	public Boolean portAllEntities=true;
	public int delayAllCheck=5;//multiplikator
	public HashMap<String,Connection> connections=new HashMap<String,Connection>();
	
	private String prefix=ChatColor.GOLD+"[cbt]";
	
	public void checkConfig() {
		// TODO Auto-generated method stub
		
		
	}

	public boolean worldHasPort(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	public void checkTeleport(Entity target) {
		String world=target.getLocation().getWorld().getName();
		Location targetLoc=connections.get(world).getTarget(target.getLocation());
		if (targetLoc==null)
			return;
		target.teleport(targetLoc);
	}

	public boolean pThresh(Location loc) {
		String world=loc.getWorld().getName();
		Connection con=connections.get(world);
		return (con.teleportHightBottom+con.playerTDistance>loc.getY()||
				con.teleportHightTop-con.playerTDistance<loc.getY());
	}


	public void sendMessage(Player target, int i) {
		//placeholder for translation picker
		switch(i){
			case (1):
				target.sendMessage(prefix+"teleporting all entities with cbt");
			case (2):
				target.sendMessage(prefix+"teleporting only players with cbt");
			case (3):
				target.sendMessage(prefix+"timer changed");
			case (4):
				target.sendMessage(prefix+"threshold for this world changed");
			case (5):
				target.sendMessage(prefix+"this world has no connection");
			case (6):
				target.sendMessage(prefix+"");
			case (7):
				target.sendMessage(prefix+"");
		}
		// TODO Auto-generated method stub
		
	}
}
