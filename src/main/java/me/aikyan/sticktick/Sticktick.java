package me.aikyan.sticktick;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

class StickstickStr {
    String name; // display name of Player
    long timeHeld; // Timer while holding a stick
    long totalTimeHeld; // Total time in ms holding a stick
}

public final class Sticktick extends JavaPlugin implements Listener {
    int nb_players;
    List<StickstickStr> ssdata;

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("The plugin Stickstick has started !");
        getServer().getPluginManager().registerEvents(this, this);
        nb_players = 0;
        ssdata = new ArrayList<>();;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("The plugin Stickstick has stopped !");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { // /sstime function callable by Player, response is time spent with a stick
        if (command.getName().equalsIgnoreCase("sstime") && sender instanceof Player) {
            Player p = (Player) sender;
            for (StickstickStr data : ssdata) {
                if (p.getDisplayName().equals(data.name)) {
                    if (data.timeHeld != 0)
                        data.totalTimeHeld += System.currentTimeMillis() - data.timeHeld;
                    data.timeHeld = 0;
                    p.sendMessage("[Stickstick] Time spent with a stick: " + data.totalTimeHeld + "ms");
                    if (p.getInventory().getItem(p.getInventory().getHeldItemSlot()).getType() == Material.STICK)
                        data.timeHeld = System.currentTimeMillis();
                    return true;
                }
            }
        }
        return super.onCommand(sender, command, label, args);
    }

    @EventHandler
    public void onPlayerConnection(PlayerJoinEvent event) { // Setup records of time spent with a stick on player connection
        StickstickStr newData = new StickstickStr();

        newData.timeHeld = 0;
        newData.totalTimeHeld = 0;
        newData.name = event.getPlayer().getDisplayName();
        ssdata.add(newData);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) { // Delete all records of time spent with a stick on player disconnection
        nb_players -= 1;
        ssdata.removeIf(x -> x.name.contentEquals(event.getPlayer().getDisplayName()));
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) { // Time spent with stick logic here
        Player p = event.getPlayer();

        for (StickstickStr data : ssdata) {
            if (p.getDisplayName().equals(data.name)) { // get right data from user
                if (data.timeHeld != 0) {
                    data.totalTimeHeld += System.currentTimeMillis() - data.timeHeld; // Update total time held
                    data.timeHeld = 0;
                }
                if (p.getInventory().getItem(event.getNewSlot()).getType() == Material.STICK) { // if new item in hand is a stick, start a new timer
                    data.timeHeld = System.currentTimeMillis();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent event) { // check if player changed holding item slot
        Player p = (Player) event.getPlayer();
        int slot = 0;

        System.out.println("[Stickstick] " + p.getDisplayName() + " closed its inventory");
        slot = p.getInventory().getHeldItemSlot();
        for (StickstickStr data : ssdata) {
            if (data.name.equals(p.getDisplayName())) {
                if (data.timeHeld != 0) {
                    data.totalTimeHeld += System.currentTimeMillis() - data.timeHeld; // Update total time held
                    data.timeHeld = 0;
                }
                if (p.getInventory().getItem(slot).getType() == Material.STICK) { // new held item is a stick
                    data.timeHeld = System.currentTimeMillis();
                }
            }
        }
    }
}
