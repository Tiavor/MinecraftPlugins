package de.mg127.TiaEssentials.RessourceManagement;


import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class RMTypes {
	/*type:						bsp mats
	 * 1 holzfäller				log, stick
	 * 2 steinbruch				sandstone, cleanstone
	 * 3 mine					ore
	 * 4 feld					wheat, potato, carrot
	 * 5 tierzucht				spawneggs, spawned pig/cow/chicken
	 * 6 handelshaus			money, random mats
	 * 7 
	 * 8 
	 * 9 
	 * 10 
	 * 11 sägewerk				planks, half planks
	 * 12 steinmetz				stonebrick, chiseld stonebrick
	 * 13 schmelze				bar
	 * 14 mühle					mehl*
	 * 15 schlachter			rohes fleisch
	 * 16 schiff				money, random mats
	 * 17 steinbrechwerk		cobble, gravel, sand
	 * 18 
	 * 19 
	 * 20 
	 * 21 schreiner				button, wooden stairs, sign, chest, bowl,   
	 * 22 
	 * 23 schmiede
	 * 24 bäckerei
	 * 25 fleischer
	 * 26 bank
	 * 27 
	 * 28 
	 * 29 
	 * 30 
	 * 31
	 * 32
	 * 33 kunstschmiede
	 * 34
	 * 35
	 * 36
	 * 37
	 * 38
	 * 39
	*/
	private static Material getMat(int type,Biome biome, int stage){
		return null;/////todo/////
	}
	private static Material getMat(int type,int stage){
		return null;/////todo/////
	}
	private static int getAmmount(int type,Biome biome,int stage){
		return 0;/////todo/////
	}
	private static int getAmmount(int type,int stage){
		return 0;/////todo/////
	}
	private static MaterialData getData(int type, Biome biome, int stage) {
		// TODO Auto-generated method stub
		return null;
	}
	private static MaterialData getData(int type, int stage) {
		// TODO Auto-generated method stub
		return null;
	}
	private static boolean isBiomeNeeded(int type){
		final List<Integer> liste=Arrays.asList(1,2,3,4,5);
		return liste.contains(type);
	}
	private static boolean isAdjecentBuildingNeeded(int type){
		final List<Integer> liste=Arrays.asList(11,12,13,14,15,21,23,24,25,33);
		return liste.contains(type);
	}
	public static List<ItemStack> getItems(Location chestLoc,int type, int stage, float votingBonus) {
		List<ItemStack> items=new ArrayList<ItemStack>();
		
		for(final ItemStack is : items){
			
			if (isBiomeNeeded(type)){
				Biome biome=chestLoc.getBlock().getBiome();
				if (isAdjecentBuildingNeeded(type)){
				/////todo/////
				}else{
					is.setType(getMat(type,biome,stage));
					is.setData(getData(type,biome,stage));
					is.setAmount((int)(getAmmount(type,biome,stage)*votingBonus));
				}	
			}else{
				if (isAdjecentBuildingNeeded(type)){
				/////todo/////
				}else{
				/////todo/////
				}
			}
		
			items.add(is);
		}
		return items;
	}
}
