package de.mg127.TiaEssentials.RessourceManagement;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ChestBlock;

public class RMBuilding {
	private static BaseItemStack[] insertNewItem(BaseItemStack[] stack, ItemStack item){
		if (stack==null) {
			return null;
		}
		if (item==null) {
			return stack;
		}
		int remain=item.getAmount();
		for (int i=0; i<stack.length;i++){
			if (stack[i].getType()==item.getTypeId()&&stack[i].getAmount()<item.getMaxStackSize()){
				if (stack[i].getAmount()+remain<item.getMaxStackSize()){
					stack[i].setAmount(stack[i].getAmount()+remain);
					remain=0;
				}
				else{
					remain=item.getAmount()-(item.getMaxStackSize()-stack[i].getAmount());
					stack[i].setAmount(item.getMaxStackSize());
				}
			}
			if (remain<=0) {
				break;
			}
		}
		return stack;
	}
	private final int ID, type;
	private final String owner, name;
	private int stage, inLink, outLink;
	private float votingBonus;

	private final Location chestLoc;

	public RMBuilding(int ID, int type, String name, String owner, Location loc){
		this.ID=ID;
		this.type=type;
		this.stage=1;
		this.votingBonus=0;
		this.name=name;
		this.owner=owner;
		this.chestLoc=loc;
	}
	public RMBuilding(int ID, int type, String name, String owner, Location loc, int stage, int votingBonus, int inLink, int outLink){
		this.ID=ID;
		this.type=type;
		this.name=name;
		this.owner=owner;
		this.stage=stage;
		this.votingBonus=votingBonus/100;
		this.chestLoc=loc;
		this.inLink=inLink;
		this.outLink=outLink;
	}
	public void setInput(int count){
		this.inLink=count;
	}
	public void setOutput(int count){
		this.outLink=count;
	}
	public int getInput(){
		return this.inLink;
	}
	public int getOutput(){
		return this.outLink;
	}
	
	public void addVotingBonus(int vb){
		this.votingBonus=this.votingBonus+vb/100;
	}
	public void age(){
		this.age(0.99f);
	}
	public void age(float percent){
		this.votingBonus=this.votingBonus*percent;
	}
	public void generateItems(){
		final Block targetBlock=this.chestLoc.getBlock();
		if (targetBlock==null) {
			return;
		}

		if (targetBlock.getType().equals(BlockType.CHEST)){
			final ChestBlock targetChest=(ChestBlock)targetBlock;
			BaseItemStack[] stack=targetChest.getItems();
			List<ItemStack> items=RMTypes.getItems(this.chestLoc,this.type,this.stage,this.votingBonus);
			for(final Iterator<ItemStack> i=items.iterator();i.hasNext();){
				final ItemStack is=i.next();
				if (is==null){
					break;
				}
				stack= insertNewItem(stack,is);
			}
			targetChest.setItems(stack);
		}
	}
	
	public int getType(){
		return this.type;
	}
	public int getID(){
		return this.ID;
	}
	public String getName(){
		return this.name;
	}
	public String getOwner(){
		return this.owner;
	}
	public int getProductivity(Material mat){
		/////todo/////
		return 0;
	}
	public int getStage(){
		return this.stage;
	}
	public int getVotingBonus(){
		return (int)(this.votingBonus*100);
	}
	public void levelUp(){
		this.votingBonus=this.votingBonus/2;
		this.stage++;
	}
	public void setStage(int s){
		this.stage=s;
	}
	public void setVotingBonus(int vb){
		this.votingBonus=vb/100;
	}
}
