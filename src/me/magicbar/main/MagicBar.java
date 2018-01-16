package me.magicbar.main;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.magicbar.main.LandManager.Land;

/*
 * MagicBar Project
 * By Gangguen
 * 
 * component
 * - 도박(보류)
 * - GUI
 *
 * - 나침반 - 어부
 * - 시계 - 농부
 * - CD - 광부
 * - ? - 요리사
 * - ? - 사냥꾼
 * - ? - 사육사
 * 
 * - 승리 조건
 * 
 * - 아ㅣ아ㅣㄹ나어란어ㅏㄹ너ㅏㅣ 승리
 * - 땅 다 잃을 경우 패배
 * - 땅을 강제로 사가지고 땅을 하나도 가지고 있지 않을 경우 마지막 땅을 가져간 사람에게 구속된다.
 * 
 * - 자본가 승리
 * - 돈모아서 아이템 사면 승리
 * 
 * - 탐험가 승리
 * - 맵에서 아이템을 모두 찾아 모으면 승리
 */

public class MagicBar extends JavaPlugin implements Listener {

	final String money_path = "./MagicBar/Money.dat";

	public void onEnable() {
		Bukkit.getLogger().info("[MagicBar] Plugin is on!!");

		getServer().getPluginManager().registerEvents(this, this);
		
		if (MoneyManager.checkMoneyFile(money_path))
			MoneyManager.loadMoney(money_path);
		else
			MoneyManager.createMoneyFile(money_path);
		
		ShopManager.loadShops("./MagicBar/Shops/");
	}

	public void onDisable() {
		Bukkit.getLogger().info("[MagicBar] Plugin is off!!");

		if (MoneyManager.length() > 0)
			MoneyManager.saveMoney(money_path);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (command.equals("mb")) {
			if (args.length == 0) { // 추가 명령어 없을 경우
				show_help(sender);
			} else if (args[0].equals("money")) {
				if (args.length == 1) {
					sender.sendMessage("[MagicBar] " + sender.getName() + "has"
							+ MoneyManager.getMoney(sender.getName()) + " won");
				} else if (args.length == 4) {
					if (args[1].equals("send")) {
						if (Bukkit.getServer().getPlayer(args[3]) != null) {
							long money = MoneyManager.getMoney(sender.getName());
							long add_money = 0;

							try {
								add_money = Long.parseLong(args[2]);
							} catch (NumberFormatException e) {
								show_help(sender);
								return true;
							}

							if (add_money < 0) {
								sender.sendMessage("[개발자] 수작부리지 마세요~^^");
								return true;
							}

							money -= add_money;

							if (money < 0) {
								sender.sendMessage("[MagicBar] 현재 돈이 부족합니다!! ㅠㅠ : 가지고 있는 돈 / 보내려고 했던 돈 : "
										+ (money + add_money) + " / " + args[2]);
								return true;
							}

							MoneyManager.setMoney(sender.getName(), money);
							MoneyManager.setMoney(args[3], MoneyManager.getMoney(args[3]) + add_money);

						} else
							sender.sendMessage("[MagicBar] 해당 플레이어는 없는 플레이어 입니다 : " + args[3]);
					} else {
						show_help(sender);
					}

				} else {
					show_help(sender);
				}
			} else if (args[1].equals("land")) {
				if (args.length == 1)
					LandManager.player_land(sender.getName());
				else if (args.length == 4) {
					if (Bukkit.getServer().getPlayer(args[3]) != null) {
						if (args[1].equals("give")) {
							try {
								int code = Integer.parseInt(args[2]);
								if (0 <= code && code < LandManager.lands.get(sender.getName()).size()) {
									LandManager.Land tmp = LandManager.lands.get(sender.getName()).get(code);
									LandManager.lands.get(sender.getName()).remove(code);
									LandManager.lands.get(args[3]).add(tmp);
								}
								else show_help(sender);
							} catch (NumberFormatException e) {
								show_help(sender);
								return true;
							}
						} else if (args[1].equals("allow")) {
							try {
								int code = Integer.parseInt(args[2]);
								if (0 <= code && code < LandManager.lands.get(sender.getName()).size()) {
									LandManager.lands.get(sender.getName()).get(code).allow(args[3]);
								}
								else show_help(sender);
							} catch (NumberFormatException e) {
								show_help(sender);
								return true;
							}
						} else {
							show_help(sender);
						}
					} else {
						show_help(sender);
					}
				} else {
					show_help(sender);
				}
			} else if (args[0].equals("shop")) {
				if (args.length == 2 && sender.isOp()) {
					if (args[1].equals("shuffle")) {
						ShopManager.shuffle(); // Shuffle and Match
					} else if (args[1].equals("list")) {
						sender.sendMessage("[MagicBar] 상점 리스트");
						for (Shop s : ShopManager.shops) {
							sender.sendMessage(s.inv.getTitle());
						}
					} else {
						if (Bukkit.getPlayer(args[1]) != null) {
							ShopManager.showShop(sender, args[1]);
						}
					}
				}
				
				ShopManager.showShop(sender);
				
			} else {
				show_help(sender);
			}
		}

		return true;
	}

	public static String player_help() {
		return "/mb money : 돈을 보여줍니다.\n" + "/mb money send <Money> <Player>: 돈을 보냅니다.\n"
				+ "/mb land : 소유하고 있는 땅을 보여줍니다\n" + "/mb land give <Number> <Player> : 땅을 줍니다\n"
				+ "/mb land allow <Number> <Player> : 플레이어가 해당 땅에 들어올 수 있도록 합니다\n" + "/mb shop : 상점을 볼 수 있습니다";
	}

	public static String bukkit_help() {
		return player_help() + "/mb shop list : 상점들의 목록을 볼 수 있습니다\n" + "/mb shop <Name> : 해당 상점을 볼 수 있습니다\n"
				+ "/mb shop shuffle : 직업(상점) 을 섞습니다";
	}

	public static void show_help(CommandSender sender) {
		if (sender instanceof Player && !sender.isOp())
			sender.sendMessage(player_help());
		else
			sender.sendMessage(bukkit_help());
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Inventory inv = e.getInventory();
		if (!(e.getWhoClicked() instanceof Player))
			return;
		
		if (ShopManager.hasShop(inv.getTitle())) {
			Player p = (Player) e.getWhoClicked();
			e.setCancelled(true);
			
			ItemStack item = e.getCurrentItem();
			
			if(item == null || item.getType() == Material.AIR || !item.hasItemMeta())
				return;
			
			ArrayList<String> lores = new ArrayList<String>(item.getItemMeta().getLore());
			
			if (lores.size() != 2)
				return;
			
			// get Price
			long buyprice = 0;
			long sellprice = 0;
			
			for (String lore : lores) {
				String[] part = lore.split(" : ");
				
				if (part[0].contains("구")) {
					buyprice = Long.parseLong(part[1]);
				} else if (part[0].contains("판")) {
					sellprice = Long.parseLong(part[1]);
				}
			}
			
			// get Right or Left
			ClickType click = e.getClick();
			
			item.getItemMeta().setLore(new ArrayList<String>());
			long money = MoneyManager.getMoney(p.getName());
			
			if (click == ClickType.RIGHT) {
				if (p.getInventory().contains(item)) {
					p.getInventory().remove(item);
					MoneyManager.setMoney(p.getName(), money + sellprice);
				}
					
			} else if (click == ClickType.LEFT) {
				if (money < buyprice)
					p.sendMessage("[MagicBar] 돈이 부족합니당...");
				else {
					MoneyManager.setMoney(p.getName(), money - buyprice);
					p.getInventory().addItem(item);
				}
			}
		}
	}
	
	@EventHandler
	public void onMoving(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if(!LandManager.isAllow(p))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		if (LandManager.lands.containsKey(p.getName())) {
			MoneyManager.setMoney(p.getName(), 0);
			LandManager.lands.put(p.getName(), new ArrayList<Land>());
		}
	}
}
