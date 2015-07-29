package de.mg127.TiaEssentials.Voting;

import java.sql.Time;
import java.util.HashMap;
import java.util.Iterator;

public class Voted {
	private int count;
	private HashMap<Integer,Time> lastTimeVoted=new HashMap<Integer,Time>();
	private HashMap<Integer,Integer> voteCounter=new HashMap<Integer,Integer>();
	public Voted(){
		count=0;
	}
	public Voted(String votes, String vCounter){
		this.count=0;
		int b,c,d,index=0;
		while(index<votes.length()){
			b=votes.indexOf("!", index)-1;
			c=b+2;
			d=votes.indexOf("!", c)-1;
			index=d+2;
			
			Integer ID=Integer.valueOf(votes.substring(index, b));
			this.addVote(ID, Time.valueOf(votes.substring(c, d)));
		}
	}
	public boolean hasVoted(int ID){
		
		return false;
	}
	
	public void addVote(int ID, Time time){
		this.lastTimeVoted.put(ID, time);
		if (this.voteCounter.containsKey(ID))
			this.voteCounter.put(ID, this.voteCounter.get(ID)+1);
		else
			this.voteCounter.put(ID, 1);
		this.count++;
	}
	public int getVoteCount(){
		return count;
	}
	public int getVoteCount(int ID){
		if(this.voteCounter.containsKey(ID))
			return this.voteCounter.get(ID);
		else
			return 0;
	}
	public String getString(){
		String ret="";
		Iterator<Integer> i=lastTimeVoted.keySet().iterator();
		while(i.hasNext()){
			Object j=i.next();
			ret=ret+j.toString()+"!"+lastTimeVoted.get(j)+"!";
		}
		return ret;
	}
}
