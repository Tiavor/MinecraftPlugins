package de.mg127.TiaEssentials;


import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;

public class ItemSet {
	ItemStack items[];
	
	public void putItems(PlayerInventory inv){
		for (int i=0;i<9;i++)
			inv.clear(i);
		for(int i=0;i<9;i++){
			inv.setItem(i, items[i]);
		}
	}
	
	public ItemSet(ItemStack[] inputItems){
		items=inputItems;
	}
}
