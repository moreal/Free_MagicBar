package me.magicbar.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LandManager {
	class Land {
		HashSet<String> owners = new HashSet<String>();

		Area area;

		public Land(String owner, Location a, Location b) {
			this.owners.add(owner);
			this.area = new Area(a, b);
		}
		
		public void allow(String name) {
			owners.add(name);
		}
		
		public boolean isAllow(String name) {
			return owners.contains(name);
		}
		
		public String toString() {
			return "소유자 : " + owners.toString() + " / 좌표 : " + area.toString();
		}
		
		class Area {
			Location a, b;

			public Area(Location a, Location b) {
				this.a = a;
				this.b = b;
			}

			public boolean contains(Location t) {
				boolean c = a.getBlockX() < t.getBlockX() && t.getBlockX() < b.getBlockX() ||
							b.getBlockX() < t.getBlockX() && t.getBlockX() < a.getBlockX();
				
				c &= a.getBlockY() < t.getBlockY() && t.getBlockY() < b.getBlockY() ||
					 b.getBlockY() < t.getBlockY() && t.getBlockY() < a.getBlockY();
				
				c &= a.getBlockZ() < t.getBlockZ() && t.getBlockZ() < b.getBlockZ() ||
					 b.getBlockZ() < t.getBlockZ() && t.getBlockZ() < a.getBlockZ();
				
				return c;
			}
		
			public String toString() {
				return "[ " + a.toString() + " : " + b.toString() + " ]";
			}
		}
	}
	
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
}
