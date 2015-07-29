package de.mg127.TiaEssentials;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WarpPoint {
	public Location location;
	public String Creator, Type, LongName, WellcomeMessage;
	public Boolean showName,showWellcome;
	public WarpPoint(){
		//null constructor
	}
	public WarpPoint(String type, Player creator){
		Type=type;
		location=creator.getLocation();
		Creator=creator.getName();
	}
	public WarpPoint(String type, Player creator, String name){
		Type=type;
		location=creator.getLocation();
		Creator=creator.getName();
		LongName=name;
	}
	public WarpPoint(String type, String creator, String name, Location loc){
		Type=type;
		Creator=creator;
		location=loc;
		LongName=name;
	}
	public WarpPoint(String type, String creator, String name, World destination, Double x, Double y, Double z, Double Pitch, Double Yaw){
		location=new Location(destination,x,y,z);
		location.setPitch(Pitch.floatValue());
		location.setYaw(Yaw.floatValue());
		Creator=creator;
		Type=type;
		LongName=name;
	}
	public WarpPoint(String creator, World destination, Double x, Double y, Double z, Double Pitch, Double Yaw){
		location=new Location(destination,x,y,z);
		location.setPitch(Pitch.floatValue());
		location.setYaw(Yaw.floatValue());
		Creator=creator;
	}
}
