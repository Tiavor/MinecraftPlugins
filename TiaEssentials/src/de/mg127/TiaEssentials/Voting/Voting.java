package de.mg127.TiaEssentials.Voting;


import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import de.mg127.TiaEssentials.TiaEssentials;

public class Voting{
	
	VotingConfig config=null;
	TiaEssentials plugin;
	public Voting(TiaEssentials instance){
		config=new VotingConfig(instance);
		plugin=instance;
	}
	public void voteCommand(Player player, String[] args){
		if (config.isVotingActive()){
			if (args.length>0){
				//vote [ID] - activates voting for building [ID]
				if (args[0].matches("^[0-9]+$")){
					int ID=Integer.valueOf(args[0]);
					if (config.isVotingActive(ID)){
						if (args.length==1){
							//if (!player is participating in an voting)
								TiaEssentials.setPlayerSpectator(player);
							player.teleport(config.getTarget(ID));
							player.sendMessage(ChatColor.GOLD+"Bewerte \""+config.getName(ID)+"\" (10 = top, 1 = flop)");
							player.sendRawMessage(generateRawmessage(ID));
						}else
						if (args.length==2){
							if (args[1].matches("^[0-9]+$")){
								endWithVote(player,Integer.valueOf(args[1]));
							}
						}
					}else
						player.sendMessage(ChatColor.GOLD+"Die gewählte Voting-ID ist nicht aktiv.");
				}else{
					//optional arguments
					//vote all - list all active votings
					//vote end - ends actual voting without vote
					if (args[0].equalsIgnoreCase("all")){
						config.listAllVotings(player);
						return;
					}
					if (args[0].equalsIgnoreCase("end")){
						this.endWithoutVote(player);
						return;
					}
					if (args[0].equalsIgnoreCase("admin")&&plugin.PlayerHasPermission(player, "voteadmin")){
						//admin commands
						if (args.length>1&&args[1].matches("^[0-9]+$")){
							if (args[1].equalsIgnoreCase("add")){
								this.addObjToVoting(Integer.valueOf(args[1]));
							}if (args[1].equalsIgnoreCase("remove")){
																				/////todo/////
							}
							//manual start, end, cancel
							//should be automatic
							//todo: automatic selection, start and end of a voting
							if (args[1].equalsIgnoreCase("start")){
																				/////todo/////
							}if (args[1].equalsIgnoreCase("end")){
																				/////todo/////
							}if (args[1].equalsIgnoreCase("cancel")){
																				/////todo/////
							}
						}else{
							player.sendMessage(ChatColor.GOLD+"/vote admin add [buildingID]");
							player.sendMessage(ChatColor.GOLD+"/vote admin remove [id]");
							player.sendMessage(ChatColor.GOLD+"/vote admin start [id]");
							player.sendMessage(ChatColor.GOLD+"/vote admin end [id]");
							player.sendMessage(ChatColor.GOLD+"/vote admin cancel [id]");
						}
					}
					else{
						player.sendMessage(ChatColor.GOLD+"ungültiges Voting-ID Format oder keine Rechte für diesen Befehl");
						player.sendMessage(ChatColor.GOLD+"/vote [ID] [1-10] - zum bewerten");
						player.sendMessage(ChatColor.GOLD+"/vote end - zum abbrechen");
					}
				}
			}else{
				//vote     - list all votings you haven't participated yet
				config.listAllLegalVotings(player);
			}
		}
		else{
			player.sendMessage(ChatColor.GOLD+"keine Wahlen aktiv!");
		}
	}
	private void endWithoutVote(Player player){
		config.endPVote(player.getName());
		TiaEssentials.setPlayerNormal(player);
	}
	private void endWithVote(Player player, int vote){
		config.endPVote(player.getName(), vote);
		TiaEssentials.setPlayerNormal(player);
	}
	public void endVote(int ID){
		config.endVote(ID);
	}
	public void addObjToVoting(int BID){
		
	}
	private String generateRawmessage(int ID){
		String ret=ChatColor.GOLD+"";																/////todo/////
		return ret;
	}
}
