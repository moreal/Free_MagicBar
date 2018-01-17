package me.magicbar.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

public class MoneyManager {
	private static HashMap<String, Long> moneys = new HashMap<String, Long>();

	public static long getMoney(String name) {
		return moneys.get(name);
	}

	public static void setMoney(String name, long money) {
		moneys.put(name, money);
	}

	public static void addMoney(String name, long money) {
		money += moneys.get(name);
		moneys.put(name, money);
	}

	public static boolean saveMoney(String path) {
		File data = new File(path);
		FileWriter fw;
		try {
			fw = new FileWriter(data);

			for (Map.Entry<String, Long> entry : moneys.entrySet())
				fw.write(entry.getKey() + ":" + entry.getValue() + "\n");
			
			fw.flush();
			
			fw.close();
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	public static boolean loadMoney(String path) {
		File data = new File(path);
		try {
			BufferedReader br = new BufferedReader(new FileReader(data));
			String line = null;
			String arr[] = null;
			try {
				while ((line = br.readLine()) != null) {
					arr = line.split(":");
					moneys.put(arr[0], Long.parseLong(arr[1]));
				}
				
				br.close();
			} catch (NumberFormatException e) {
				Bukkit.getLogger().info("[MagicBar] There was Wrong Number Format ( Func : loadMoney ) / " + path);
				return false;
			} catch (IOException e) {
				return false;
			}
		} catch (FileNotFoundException e) {
			Bukkit.getLogger().info("[MagicBar] There was No File ( Func : loadMoney ) / " + path);
			return false;
		}

		return true;
	}
	
	public static boolean checkMoneyFile (String path) {
		return new File(path).exists();
	}
	
	public static boolean createMoneyFile (String path) {
		try {
			return new File(path).createNewFile();
		} catch (IOException e) {
			return false;
		}
	}
	
	public static int length() {
		return moneys.size();
	}
}
