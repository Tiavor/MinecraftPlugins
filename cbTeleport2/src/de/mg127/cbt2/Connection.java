package de.mg127.cbt2;

import org.bukkit.Location;

//HashMap<WorldName,Connection>
public class Connection {
	int teleportHightTop, teleportHightBottom, playerTDistance;
	Location targetTop, targetBottom;// world=target world, x,z=offset, y=target hight
	boolean relativTop,relativBottom;// if false->teleport to target location
	int mulTop, mulBottom;
	
	public Connection(){
		this.teleportHightBottom=5;
		this.teleportHightTop=240;
		this.targetTop=null;
		this.targetBottom=null;
	}
	
	public boolean isInRange(int pos) {
		return pos<=teleportHightBottom||pos>=teleportHightTop;
	}

	public Location getTarget(Location loc) {
		double y=loc.getY();
		if (y<teleportHightBottom){
			if (!relativBottom)
				return targetBottom;
			return getTarget(loc,targetBottom,mulBottom);
		}else
		if(y>teleportHightTop){
			if (!relativTop)
				return targetTop;
			return getTarget(loc,targetTop,mulTop);
		}
		return null;
	}
	private Location getTarget(Location loc,Location tLoc,double mul){
		double dX=tLoc.getX(),dZ=tLoc.getZ(),y=tLoc.getBlockY(),x,z;
		x=(loc.getX()+dX)*mul;
		z=(loc.getZ()+dZ)*mul;
		return new Location(tLoc.getWorld(),x,y,z,loc.getYaw(),loc.getPitch());
	}
}