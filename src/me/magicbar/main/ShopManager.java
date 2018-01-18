package me.magicbar.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

public class ShopManager {

	public static ArrayList<Shop> shops = new ArrayList<Shop>();
	public static HashMap<String, Shop> match = new HashMap<String, Shop>();

	public static void showShop(CommandSender sender) {
		showShop(sender, sender.getName());
	}
	
	public static void showShop(CommandSender sender, String name) {
		if (sender instanceof Player)
			((Player) sender).openInventory(match.get(name).inv);
		else
			sender.sendMessage("[MagicBar] you are not player");
	}

	public static boolean loadShops(String dir) {
		File directory = new File(dir);

		if (!directory.exists())
		{
			Bukkit.getLogger().info("[MagicBar] There was no Directory : " + dir);
			directory.mkdirs();
			return false;
		}
		
		else if (directory.isFile())
			return false;

		for (String file : directory.list()) {
			Bukkit.getLogger().info("[MagicBar] Check File : " + file);
			
			if (!file.endsWith(".shop"))
			{
				Bukkit.getLogger().info("[MagicBar] Wrong Format name : " + file);
				continue;
			}

			try {
				ArrayList<ItemStack> items = new ArrayList<ItemStack>();

				BufferedReader br = new BufferedReader(new FileReader(new File(dir + file)));

				String title = br.readLine();
				
				Bukkit.getLogger().info("[MagicBar] the title is " + title);
				
				String line = null;

				while ((line = br.readLine()) != null) {
					
					Bukkit.getLogger().info("[MagicBar] Check Line of [" + file + "] : " + line);
					
					if (line.equals("pass"))
					{
						items.add(new ItemStack(Material.AIR, 1));
						continue;
					}

					ItemStack item = null;

					String[] datas = line.split(" ");

					if (datas.length != 4) {
						Bukkit.getLogger().info("[MagicBar] Wrong Format File : " + file);
						continue;
					}
					
					int item_code = 0, meta_data = 0;
					
					try {
						meta_data = Byte.parseByte(datas[1]);
					} catch (NumberFormatException e) {
						Bukkit.getLogger().info("[MagicBar] Wrong Format Line : " + line);
					}
					
					try {
						item_code = Integer.parseInt(datas[0]);
						item = new ItemStack(Material.getMaterial(item_code), 1, (byte) meta_data);
					} catch (NumberFormatException e) {
						item = new ItemStack(Material.getMaterial(datas[0]), 1, (byte) meta_data);
					}

					int buyprice, sellprice;
					ArrayList<String> lores = new ArrayList<String>();
					

					try {
						buyprice = Integer.parseInt(datas[2]);
						lores.add(ChatColor.WHITE + "구매가 [좌클릭] : " + ChatColor.GREEN + buyprice);
					} catch (NumberFormatException e) {}
					
					try {
						sellprice = Integer.parseInt(datas[3]);
						lores.add(ChatColor.WHITE + "판매가 [우클릭] : " + ChatColor.GREEN + sellprice);
					} catch (NumberFormatException e) {}
					
					if (lores.size() == 0) {
						items.add(new ItemStack(Material.AIR));
						continue;
					}
					ItemMeta meta = item.getItemMeta();
					meta.setLore(lores);
					item.setData(new MaterialData(item_code, (byte) meta_data));
					
					Bukkit.getLogger().info(String.format("[MagicBar] Set Lore : ", lores));
					
					item.setItemMeta(meta);
					items.add(item);
				}
				
				Bukkit.getLogger().info("[MagicBar] a number of values is : " + items.size());
				
				if (items.size() == 27)
					shops.add(new Shop(title, items));
				
				br.close();
			} catch (FileNotFoundException e) {
				Bukkit.getLogger().info("[MagicBar] There was no file : " + file);
				return false;
			} catch (IOException e) {
				return false;
			}
		}

		return true;
	}

	public static void shuffle() {
		ArrayList<Player> players = new ArrayList<Player>();
		
		for (Player p : Bukkit.getOnlinePlayers())
			players.add(p);
		
		Collections.shuffle(players);
		Collections.shuffle(shops);
		
		match.clear();
		
		for (int i = 0; i < players.size(); ++i)
			match.put(players.get(i).getName(), shops.get(i % shops.size()));
		
	}

	public static boolean hasShop (String title) {
		
		for (Shop s : shops)
			if (s.inv.getTitle().equals(title))
				return true;
			
		return false;
	}
}

class Shop {
	Inventory inv = null;

	public Shop(String title, ArrayList<ItemStack> items) {
		inv = Bukkit.createInventory(null, 27, title);

		for (int i = 0; i < items.size(); ++i)
			inv.setItem(i, items.get(i));
	}
}
