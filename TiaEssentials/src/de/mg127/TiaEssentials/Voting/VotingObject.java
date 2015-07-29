package de.mg127.TiaEssentials.Voting;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class VotingObject {
	private int votingsReceived;
	private float meanVotes;
	private final String name;
	private final int buildingID;
	private final Location teleportLocation;
	private List<Integer> newVotes=new ArrayList<Integer>();
	private List<String> newPlayerVoted=new ArrayList<String>();

	VotingObject(String name, int bID, Location target){
		this.name=name;
		this.buildingID=bID;
		this.teleportLocation=target;
	}
	VotingObject(String name, int bID, Location target, int vReceived, float mean){
		this.name=name;
		this.buildingID=bID;
		this.votingsReceived=vReceived;
		this.meanVotes=mean;
		this.teleportLocation=target;
	}
	public int getBID(){
		return this.buildingID;
	}
	public Location getLocation(){
		return this.teleportLocation;
	}
	public String getName(){
		return this.name;
	}
	public float getVoteMean(){
		return this.meanVotes;
	}
	public int getVoteCount(){
		return this.votingsReceived;
	}
	public boolean addNewVote(int vote,String playerName){
		if (this.newPlayerVoted.contains(playerName))
			return false;
		newVotes.add(vote);
		this.newPlayerVoted.add(playerName);
		return true;
	}
	public void finalizeVote(){
		if (this.votingsReceived==0){
			meanVotes=mean(newVotes);
			this.votingsReceived=newVotes.size();
		}else{
			final float meanNew=mean(newVotes);
			meanVotes=(meanNew+meanVotes)/2;
			this.votingsReceived+=newVotes.size();
		}
		newVotes.clear();
		newPlayerVoted.clear();
	}
	private static float mean(List<Integer> list){
		Integer sum = 0;
		if(!list.isEmpty()) {
			for (final Integer i : list) {
				sum += i;
			}
			return sum.floatValue() / list.size();
		}
		return sum;
	}
	public boolean isVoteLegal(String player){
		return !this.newPlayerVoted.contains(player);
	}
}
