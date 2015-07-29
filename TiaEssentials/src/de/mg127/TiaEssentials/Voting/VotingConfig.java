package de.mg127.TiaEssentials.Voting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import de.mg127.TiaEssentials.FileConfig;
import de.mg127.TiaEssentials.TiaEssentials;

public class VotingConfig {

	private boolean votingIsActive;
	private List<Integer> activeVotes=new ArrayList<Integer>();
	private HashMap<String,Voted> playerVotes = new HashMap<String,Voted>();
	private HashMap<Integer,VotingObject> votingObj = new HashMap<Integer,VotingObject>();
	private HashMap<String,ActivePlayer> activePlayers = new HashMap<String,ActivePlayer>(); // playername


	private FileConfig votes,aVotes; //aVotes = active votings
	public VotingConfig(TiaEssentials instance){
		votes=new FileConfig(instance,"votes");
		aVotes=new FileConfig(instance,"voting_active");
	}

	public void checkConfig() {
		FileConfiguration vcon=votes.getConfig();
		FileConfiguration avcon=aVotes.getConfig();
		if (vcon.getKeys(false).isEmpty()){
			vcon.options().copyDefaults(true);
			votes.saveConfig();
		}
		if (avcon.getKeys(false).isEmpty()){
			avcon.options().copyDefaults(true);
			aVotes.saveConfig();
		}
		loadkeys();
	}
	private void loadkeys() {
		votes.reloadConfig();
		aVotes.reloadConfig();
		FileConfiguration vcon=votes.getConfig();
		FileConfiguration avcon=aVotes.getConfig();

		Set<String> vObjects = null;
		Set<String> aVoter = null;
		Set<String> aObj = null;
		Set<String> vConfig = null;

		///////////////////
		//// votes.yml ////
		vObjects=vcon.getConfigurationSection("votedObjects").getKeys(false);
		if(!vObjects.isEmpty()){
			//todo: load config entries											/////todo/////
			
		}
		vConfig=vcon.getConfigurationSection("settings").getKeys(false);
		if(!vConfig.isEmpty()){
			//todo: load config entries											/////todo/////
			
		}
		////////////////////
		//// aVotes.yml ////
		aVoter=avcon.getConfigurationSection("activePlayers:").getKeys(false);
		if(!aVoter.isEmpty()){
			//todo: load config entries											/////todo/////
			
		}
		aObj=avcon.getConfigurationSection("activeObjects:").getKeys(false);
		if(!aObj.isEmpty()){
			//todo: load config entries											/////todo/////
			
		}
	}

	public void activateVote(int ID){
		if (votingObj.containsKey(ID))
			activeVotes.add(ID);
		/////todo/////write to activeVoting.yml
	}
	public void endVote(int ID){
		if (activeVotes.contains(ID)&&votingObj.containsKey(ID)) {
			this.votingObj.get(ID).finalizeVote();
		}
		/////todo/////write to activeVoting.yml
	}
	public void endPVote(String player){
		if (player==null) {
			return;
		}
		if (player.isEmpty()) {
			return;
		}
		if (!activePlayers.containsKey(player)) {
			return;
		}
		activePlayers.remove(player);
		/////todo/////write to activeVoting.yml
	}
	public void endPVote(String player, int voteValue){
		if (player==null) { return; }
		if (player.isEmpty()) {	return; }
		if (!activePlayers.containsKey(player)) { return; }
		if (!this.activeVotes.isEmpty()) { return; }
		if (!this.activeVotes.contains(this.activePlayers.get(player).votingID)) { return; }

		int votingID=this.activePlayers.get(player).votingID;
		VotingObject obj=this.votingObj.get(votingID);
		if (obj.addNewVote(voteValue,player)){ //adds new vote if player is not listed
			this.votingObj.put(votingID, obj);
			activePlayers.remove(player);
			/////todo/////write to activeVoting.yml
		}
	}
	public void addNewObjectToVote(int buildingID, Location target){
		String name=null;												/////todo:///// get name from building ID
		int ID=0;														/////todo:///// generate new ID
		this.votingObj.put(ID, new VotingObject(name,buildingID,target));
		/////todo/////write to votes.yml
	}
	public void removeObject(int ID){
		this.votingObj.remove(ID);
		/////todo/////write to votes.yml
	}
	public boolean isVotingActive(){
		return this.votingIsActive;
	}
	public boolean isVotingActive(int ID){
		return this.activeVotes.contains(ID);
	}
	public Location getTarget(int ID){
		return this.votingObj.get(ID).getLocation();
	}

	public void listAllVotings(Player player) {
		Iterator<Integer> votingsIter=this.activeVotes.iterator();
		player.sendMessage(ChatColor.GOLD+"Liste aller aktiven Votings");
		player.sendMessage(ChatColor.YELLOW+"gelb"+ChatColor.GOLD+"=noch keine Bewertung; "+
				   		   ChatColor.GREEN+"grün"+ChatColor.GOLD+"=Bewertung abgegeben");
		player.sendMessage(ChatColor.GOLD+"ID: Name");
		while (votingsIter.hasNext()){
			Integer ID = votingsIter.next();
			if (this.votingObj.get(ID).isVoteLegal(player.getName()))
				player.sendMessage(ChatColor.YELLOW+ID.toString()+": "+ChatColor.GOLD+this.votingObj.get(ID).getName());
			else
				player.sendMessage(ChatColor.GREEN+ID.toString()+": "+ChatColor.GOLD+this.votingObj.get(ID).getName());
		}
	}

	public void listAllLegalVotings(Player player){
		Iterator<Integer> votingsIter=this.activeVotes.iterator();
		player.sendMessage(ChatColor.GOLD+"Liste aller ausstehenden Votings");
		player.sendMessage(ChatColor.GOLD+"ID: Name");
		while (votingsIter.hasNext()){
			Integer ID = votingsIter.next();
			if (this.votingObj.get(ID).isVoteLegal(player.getName()))
				//{"text": "", "extra": [{"text":"[1]","color":"green","clickEvent":{"action":"run_command","value":"/vote 1"}}]}
				player.sendRawMessage("{\"text\": \"\", \"extra\": [{\"text\":\"["+ID.toString()+"]\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/vote "+ID.toString()+"\"}}]}"+": "+ChatColor.GOLD+this.votingObj.get(ID).getName());
		}
	}
	public String getName(int ID) {
		return this.votingObj.get(ID).getName();
	}
}
