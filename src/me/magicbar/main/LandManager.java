package me.magicbar.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class LandManager { // 위 아래로 : 9칸

	public static HashMap<String, ArrayList<Land>> lands = new HashMap<String, ArrayList<Land>>();

	public static String player_land(String name) {
		String s = "";

		int i = 0;

		for (Land land : lands.get(name))
			s += String.format("[%d] %s\n", i++, land);

		return s;
	}

	public static boolean isAllow(Player p) {
		String name = p.getName();
		for (Map.Entry<String, ArrayList<Land>> entry : lands.entrySet()) {
			if (entry.getKey().equals(name))
				continue;
			else
				for (Land land : entry.getValue())
					if (land.isAllow(name))
						continue;
					else if (land.area.contains(p.getLocation()))
						return false;
		}

		return true;
	}

	public static boolean saveLands(String dir) {
		File directory = new File(dir);

		if (!directory.exists()) {
			directory.mkdirs();
		} else if (!directory.isDirectory()) {
			return false;
		}

		for (Map.Entry<String, ArrayList<Land>> entry : lands.entrySet()) {
			String player_name = entry.getKey();
			ArrayList<Land> data = entry.getValue();

			File f = new File(dir + player_name + ".land");
			FileWriter fw;
			try {
				fw = new FileWriter(f);

				for (Land land : data)
					fw.write(land.getData());

				fw.flush();

				fw.close();
			} catch (IOException e) {
				return false;
			}
		}

		return true;
	}

	public static boolean loadLands(String dir) {
		File directory = new File(dir);

		if (!directory.exists()) {
			Bukkit.getLogger().info("[MagicBar] There was no Directory : " + dir);
			directory.mkdirs();
			return false;
		}

		else if (directory.isFile())
			return false;

		String owners_line = null;
		String area_line = null;

		for (String file : directory.list()) {

			if (!file.endsWith(".land"))
				continue;

			String player_name = file.replace(".land", "");

			ArrayList<Land> land_list = new ArrayList<Land>();

			lands.put(player_name, land_list);

			try {
				BufferedReader br = new BufferedReader(new FileReader(new File(dir + file)));

				while (true) {
					owners_line = br.readLine(); // Delimeter = ","
					area_line = br.readLine(); // Delimeter = "/"

					if (owners_line == null || area_line == null) {
						Bukkit.getLogger().info("[MagicBar] Wrong Lines Count : Land Loads");
						break;
					}

					String[] owners_data = owners_line.split(",");
					HashSet<String> owners = new HashSet<String>();

					for (String s : owners_data)
						owners.add(s);

					Area area = new Area(area_line);

					lands.get(player_name).add(new Land(owners, area));
				}

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
}

class Land {
	HashSet<String> owners = new HashSet<String>();

	Area area;

	public Land(String owner, Location p, int scale) {
		p = p.getBlock().getLocation();

		Location a = p.clone();
		Location b = p.clone();

		a.add(-1 * scale, -9, -1 * scale);
		b.add(scale + 1, 8, scale + 1);

		fillArea(a, b);

		this.owners.add(owner);
		this.area = new Area(a, b);
		
		Bukkit.getPlayer(owner).sendMessage("[MagicBar] 땅이 생성되었습니다! : " + this);
	}

	public Land(HashSet<String> owners, Area area) {
		this.owners = owners;
		this.area = area;
	}

	public void fillArea(Location a, Location b) {
		int y = a.getBlockY() + b.getBlockY();
		y /= 2;

		World world = a.getWorld();

		for (int i = a.getBlockX(); i < b.getBlockX(); ++i) {
			for (int j = a.getBlockZ(); j < b.getBlockZ(); ++j) {
				world.getBlockAt(i, y, j).setType(Material.BEDROCK);
			}
		}
	}

	public void allow(String name) {
		owners.add(name);
	}

	public boolean isAllow(String name) {
		return owners.contains(name);
	}

	public String toString() {
		return "소유자 : " + ownersToString() + " / 좌표 : " + area.toString();
	}
	
	public String getData() {
		return ownersToString() + "\n" + area.toString() + "\n";
	}
	
	public String ownersToString() {
		String s = "";
		
		for (String t : this.owners)
			s += t + ",";
		
		s = s.substring(0, s.length()-1);
		
		return s;
	}
}

class Area {
	Location a, b;

	public Area(String data) {
		String[] datas = data.split("/");
		String[] A = datas[0].split(",");
		String[] B = datas[1].split(",");

		if (A.length == 3 && B.length == 3) {
			a = new Location(Bukkit.getWorld("world"), Integer.parseInt(A[0]), Integer.parseInt(A[1]),
					Integer.parseInt(A[2]));
			b = new Location(Bukkit.getWorld("world"), Integer.parseInt(B[0]), Integer.parseInt(B[1]),
					Integer.parseInt(B[2]));
		} else {
			a = new Location(Bukkit.getWorld("world"), 0, 0, 0);
			b = new Location(Bukkit.getWorld("world"), 0, 0, 0);
		}
	}

	public Area(Location a, Location b) {
		this.a = a;
		this.b = b;
	}

	public boolean contains(Location t) {
		boolean c = a.getBlockX() < t.getBlockX() && t.getBlockX() < b.getBlockX()
				|| b.getBlockX() < t.getBlockX() && t.getBlockX() < a.getBlockX();

		c &= a.getBlockY() < t.getBlockY() && t.getBlockY() < b.getBlockY()
				|| b.getBlockY() < t.getBlockY() && t.getBlockY() < a.getBlockY();

		c &= a.getBlockZ() < t.getBlockZ() && t.getBlockZ() < b.getBlockZ()
				|| b.getBlockZ() < t.getBlockZ() && t.getBlockZ() < a.getBlockZ();

		return c;
	}

	public String toString() {
		return String.format("%d,%d,%d/%d,%d,%d", a.getBlockX(), a.getBlockY(), a.getBlockZ(), b.getBlockX(),
				b.getBlockY(), b.getBlockZ());
	}
}
