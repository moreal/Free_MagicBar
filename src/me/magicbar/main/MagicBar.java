package me.magicbar.main;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

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

	final String money_path = "./plugins/MagicBar/Money.dat";

	public void onEnable() {
		Bukkit.getLogger().info("[MagicBar] Plugin is on!!");

		getServer().getPluginManager().registerEvents(this, this);

		if (MoneyManager.checkMoneyFile(money_path))
			MoneyManager.loadMoney(money_path);
		else
			MoneyManager.createMoneyFile(money_path);

		ShopManager.loadShops("./plugins/MagicBar/Shops/");
		LandManager.loadLands("./plugins/MagicBar/Lands/");
	}

	public void onDisable() {
		Bukkit.getLogger().info("[MagicBar] Plugin is off!!");

		if (MoneyManager.length() > 0)
			MoneyManager.saveMoney(money_path);

		LandManager.saveLands("./plugins/MagicBar/Lands/");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (command.equals("mb")) {
			if (args.length == 0) { // 추가 명령어 없을 경우
				show_help(sender);
			} else if (args[0].equals("money")) {
				if (args.length == 1) {
					sender.sendMessage("[MagicBar] " + sender.getName() + " has "
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
			} else if (args[0].equals("land")) {
				if (args.length == 1)
					sender.sendMessage(LandManager.player_land(sender.getName()));
				else if (args.length == 4) {
					if (Bukkit.getServer().getPlayer(args[3]) != null) {
						if (args[1].equals("give")) {
							try {
								int code = Integer.parseInt(args[2]);
								if (0 <= code && code < LandManager.lands.get(sender.getName()).size()) {
									Land tmp = LandManager.lands.get(sender.getName()).get(code);
									LandManager.lands.get(sender.getName()).remove(code);
									LandManager.lands.get(args[3]).add(tmp);
								} else
									show_help(sender);
							} catch (NumberFormatException e) {
								show_help(sender);
								return true;
							}
						} else if (args[1].equals("allow")) {
							try {
								int code = Integer.parseInt(args[2]);
								if (0 <= code && code < LandManager.lands.get(sender.getName()).size()) {
									LandManager.lands.get(sender.getName()).get(code).allow(args[3]);
								} else
									show_help(sender);
							} catch (NumberFormatException e) {
								show_help(sender);
								return true;
							}
						} else if (args[1].equals("ban")) {
							try {
								int code = Integer.parseInt(args[2]);
								if (0 <= code && code < LandManager.lands.get(sender.getName()).size()) {
									LandManager.lands.get(sender.getName()).get(code).ban(args[3]);
								} else
									show_help(sender);
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
				} else if (args.length == 3) {
					if (args[1].equals("make")) {
						try {
							int scale = Integer.parseInt(args[2]);
							Land land = new Land(sender.getName(), ((Player) sender).getLocation(), scale);
							LandManager.lands.get(sender.getName()).add(land);
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
			} else if (args[0].equals("shop")) {
				if (args.length == 2 && sender.isOp()) {
					if (args[1].equals("shuffle")) {
						ShopManager.shuffle(); // Shuffle and Match
					} else if (args[1].equals("list")) {
						sender.sendMessage("[MagicBar] 상점 리스트");
						if (ShopManager.shops.size() == 0) {
							sender.sendMessage("[MagicBar] 하나도 없습니다");
							return true;
						}

						for (Shop s : ShopManager.shops) {
							sender.sendMessage(s.inv.getTitle());
						}
					} else {
						if (Bukkit.getPlayer(args[1]) != null) {
							ShopManager.showShop(sender, args[1]);
						}
					}
				} else if (args.length == 1)
					ShopManager.showShop(sender);
				else
					show_help(sender);

			} else {
				show_help(sender);
			}
		}

		return true;
	}

	public static String player_help() {
		return ChatColor.WHITE + "======= MagicBar Commands ========\n" + ChatColor.RESET + "/mb money : 돈을 보여줍니다.\n"
				+ "/mb money send <Money> <Player>: 돈을 보냅니다.\n" + "/mb land : 소유하고 있는 땅을 보여줍니다\n"
				+ "/mb land give <Number> <Player> : 땅을 줍니다\n"
				+ "/mb land allow <Number> <Player> : 플레이어가 해당 땅에 들어올 수 있도록 합니다\n" 
				+ "/mb land ban <Number> <Player> : 플레이어가 해당 땅에 들어올 수 없도록 합니다\n"
				+ "/mb shop : 상점을 볼 수 있습니다";
	}

	public static String bukkit_help() {
		return player_help() + "\n/mb shop list : 상점들의 목록을 볼 수 있습니다\n" + "/mb shop <Name> : 해당 상점을 볼 수 있습니다\n"
				+ "/mb shop shuffle : 직업(상점) 을 섞습니다\n" + "/mb land make <ScaleNumber>";
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
			Inventory player_inv = p.getInventory();
			
			e.setCancelled(true);

			ItemStack item = e.getCurrentItem();

			if (item == null || item.getType() == Material.AIR || !item.hasItemMeta())
				return;

			int item_code = item.getTypeId();

			ArrayList<String> lores = new ArrayList<String>(item.getItemMeta().getLore());
			
			long buyprice = -1;
			long sellprice = -1;
			
			for (String s : lores) {
				// get Price
				if (s.contains("구"))
					buyprice = Long.parseLong(ChatColor.stripColor(s.split(" : ")[1]));
				if (s.contains("판"))
					sellprice = Long.parseLong(ChatColor.stripColor(s.split(" : ")[1]));
			}

			byte meta = item.getData().getData();

			// get Right or Left
			ClickType click = e.getClick();

			long money = MoneyManager.getMoney(p.getName());

			ItemStack check_item = new ItemStack(item_code, 1, meta);

			if (click == ClickType.RIGHT) {
				if (sellprice == -1) {
					p.sendMessage("[MagicBar] 판매 불가능한 상품입니다");
					return;
				}
				if (player_inv.contains(item_code)) {
					for (ItemStack player_item : player_inv.getContents())
						if (player_item != null && !player_item.hasItemMeta() && player_item.getTypeId() == item_code && player_item.getData().getData() == meta) {
							if(player_item.getAmount() == 1)
								player_inv.removeItem(new ItemStack[] {player_item});
							else
								player_item.setAmount(player_item.getAmount() - 1);
							MoneyManager.setMoney(p.getName(), money + sellprice);
							p.sendMessage(String.format("[MagicBar] 아이템을 판매하였습니다. 현재 소유 돈 : %d", MoneyManager.getMoney(p.getName())));
							break;
						}
				}
			} else if (click == ClickType.LEFT) {
				if (buyprice == -1) {
					p.sendMessage("[MagicBar] 구매 불가능한 상품입니다");
					return;
				}
				if (money < buyprice)
					p.sendMessage("[MagicBar] 돈이 부족합니당...");
				else {
					MoneyManager.setMoney(p.getName(), money - buyprice);
					p.getInventory().addItem(check_item);
					p.sendMessage(String.format("[MagicBar] 아이템을 구매하였습니다. 현재 소유 돈 : %d", MoneyManager.getMoney(p.getName())));
				}
			} else if (click == ClickType.SHIFT_RIGHT) {
				if (sellprice == -1) {
					p.sendMessage("[MagicBar] 판매 불가능한 상품입니다");
					return;
				}
				if (player_inv.contains(item_code)) {
					ItemStack[] items = player_inv.getContents();
					ItemStack player_item = null;
					
					for (int i = 0; i < 36; ++i) {
						if (items[i] == null)
							continue;
						
						player_item = items[i];
						
						if (!player_item.hasItemMeta() && player_item.getTypeId() == item_code && player_item.getData().getData() == meta) {
							MoneyManager.setMoney(p.getName(), money + sellprice * player_item.getAmount());
							player_inv.removeItem(new ItemStack[] {player_item});
						
							money = MoneyManager.getMoney(p.getName());
						}
					}
					
					p.sendMessage(String.format("[MagicBar] 아이템을 판매하였습니다. 현재 소유 돈 : %d", MoneyManager.getMoney(p.getName())));
				}
			} else if (click == ClickType.SHIFT_LEFT) {
				if (buyprice == -1) {
					p.sendMessage("[MagicBar] 구매 불가능한 상품입니다");
					return;
				}
				if (money < buyprice * 64)
					p.sendMessage("[MagicBar] 돈이 부족합니당...");
				else if (true) {
					
					for(ItemStack item_t : player_inv.getContents())
						if(item_t == null) {
							MoneyManager.setMoney(p.getName(), money - buyprice * 64);
							check_item.setAmount(64);
							p.getInventory().addItem(check_item);
							p.sendMessage(String.format("[MagicBar] 아이템을 구매하였습니다. 현재 소유 돈 : %d", MoneyManager.getMoney(p.getName())));
							return;
						}
						
						p.sendMessage("[MagicBar] 인벤토리에 빈공간이 없습니다");
				}
			}
		}
	}

	@EventHandler
	public void onMoving(PlayerMoveEvent e) {
		Player p = e.getPlayer();

		if (!LandManager.isAllow(p))
			p.getLocation().add(-1, 0, -1);
	}
	
	@EventHandler
	public void onBreakBlock(BlockBreakEvent e) {
		if (!LandManager.isAllow(e.getPlayer().getName(), e.getBlock().getLocation())) e.setCancelled(true);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		if (!LandManager.lands.containsKey(p.getName())) {
			MoneyManager.setMoney(p.getName(), 0);
			getLogger().info("[MagicBar] New Player, so set Money : " + MoneyManager.getMoney(p.getName()));
			LandManager.lands.put(p.getName(), new ArrayList<Land>());
		}
	}
}
