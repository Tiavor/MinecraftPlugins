package de.mg127.TiaEssentials.RessourceManagement;

import org.bukkit.entity.Player;

import de.mg127.TiaEssentials.TiaEssentials;

public class RM {

	RMConfig config=null;
	//TiaEssentials plugin;
	public RM(TiaEssentials instance){
		config=new RMConfig(instance);
		//plugin=instance;
	}
	public void rmCommand(Player player, String[] args) {
		
		/////todo/////	add building, add to voting list
		/////todo/////	remove building, remove from voting list
		/////todo/////	upgrade building
		/////todo/////	get nearest building name/id
		/////todo/////	show stats of one building
		/////todo/////	show global stats
	}
	/////todo/////
}
