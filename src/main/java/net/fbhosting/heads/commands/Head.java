package net.fbhosting.heads.commands;

import lombok.extern.slf4j.Slf4j;
import net.fbhosting.heads.utils.TimeoutManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Head implements CommandExecutor, TabCompleter {

    private final TimeoutManager timeoutManager;
    private final LuckPerms luckPerms;
    private final JavaPlugin plugin;

    public Head(JavaPlugin plugin) {
        this.timeoutManager = new TimeoutManager(plugin.getConfig().getInt("general.timeout"));
        this.plugin = plugin;
        luckPerms = LuckPermsProvider.get();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args) {

        // only allow commands from players
        if (!(sender instanceof Player player)) return false;

        // permission check
        if (!player.hasPermission(Objects.requireNonNull(command.getPermission()))) {
            player.sendMessage("Du hast nicht die erforderlichen Berechtigungen.");
            return true;
        }

        // check if player is in timeout
        if (!player.hasPermission("heads.bypass") && this.timeoutManager.isBlocked(player)) {
            player.sendMessage("Du musst " + this.timeoutManager.getRemainingTime(player) + " sekunden warten, bevor du diesen Befehl erneut verwenden kannst.");
            return true;
        }
        this.timeoutManager.block(player);

        //get head
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                User user = luckPerms.getUserManager().getUser(target.getUniqueId());
                if (user == null) {
                    sender.sendMessage(ChatColor.RED + "LuckPerms konnte den angeforderten Spieler nicht finden.");
                    return true;
                }
                if (user.getCachedData().getPermissionData().checkPermission("moderator").asBoolean()) {
                    sender.sendMessage(ChatColor.RED + "Dieser Spieler ist ein Moderator oder höherrangig.");
                    return true;
                }
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
                meta.setOwningPlayer(target);
                playerHead.setItemMeta(meta);
                ((Player) sender).getInventory().addItem(playerHead);
                sender.sendMessage(ChatColor.GREEN + "Du hast den Kopf von " + target.getName() + " erhalten.");
            } else {
                sender.sendMessage(ChatColor.RED + "Dieser Spieler ist nicht online.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Verwendung: /Kopf <spieler>");
        }
        return true;
    }

    //Tab Completer
    public List<String> onTabComplete(CommandSender sender, Command command, String commandlabel, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(prefix)) {
                    completions.add(player.getName());
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
}