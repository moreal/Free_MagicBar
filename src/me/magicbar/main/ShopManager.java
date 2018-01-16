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
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
			directory.mkdirs();
			return false;
		}
		else if (directory.isFile())
			return false;

		for (String file : directory.list()) {
			if (!file.endsWith(".shop"))
				continue;

			try {
				ArrayList<ItemStack> items = new ArrayList<ItemStack>();

				BufferedReader br = new BufferedReader(new FileReader(new File(file)));

				String title = br.readLine();
				String line = null;

				while ((line = br.readLine()) != null) {
					if (line.equals("pass"))
						items.add(new ItemStack(Material.AIR));

					ItemStack item = null;

					String[] datas = line.split(" ");

					if (datas.length != 4) {
						Bukkit.getLogger().info("[MagicBar] Wrong Format File : " + file);
					}
					
					try {
						int item_code = Integer.parseInt(datas[0]);
						item = new ItemStack(item_code);
					} catch (NumberFormatException e) {
						item = new ItemStack(Material.getMaterial(datas[0]));
					}

					try {
						int amount = Integer.parseInt(datas[1]);
						int buyprice = Integer.parseInt(datas[2]);
						int sellprice = Integer.parseInt(datas[3]);
						
						ArrayList<String> lores = new ArrayList<String>();
						
						lores.add("구매가 [좌클릭] : " + buyprice);
						lores.add("판매가 [우클릭] : " + sellprice);
						
						item.setAmount(amount);
						item.getItemMeta().setLore(lores);
						
					} catch (NumberFormatException e) {
						break;
					}
				}

				if (items.size() == 27)
					shops.add(new Shop(title, items));
				
				br.close();
			} catch (FileNotFoundException e) {
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
