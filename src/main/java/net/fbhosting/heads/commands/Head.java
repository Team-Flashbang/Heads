package net.fbhosting.heads.commands;

import lombok.extern.slf4j.Slf4j;
import net.fbhosting.heads.HeadsPlugin;
import net.fbhosting.heads.utils.TimeoutManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Head implements CommandExecutor, TabCompleter {

    private final HeadsPlugin plugin = HeadsPlugin.getInstance();
    private final FileConfiguration config = this.plugin.getConfig();
    private final TimeoutManager timeoutManager = new TimeoutManager(this.plugin.getConfig().getInt("general.timeout"));
    private final LuckPerms luckPerms = LuckPermsProvider.get();

    /**
     * Emitted once command is called.
     * @param sender Command executor.
     * @param command Command instance.
     * @param commandLabel Command name.
     * @param args Arguments.
     * @return true, execution successfully / false, otherwise
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args) {
        // only allow commands from players
        if (!(sender instanceof Player player)) return false;


        // show usage if missing arguments
        if (args.length < 1) return false;

        // permission check
        if (!player.hasPermission(Objects.requireNonNull(command.getPermission()))) {
            player.sendMessage(this.config.getString("locale.missingPermission", "You are lacking the required permission to run this command."));
            return true;
        }

        // check if player is in timeout
        if (!player.hasPermission("heads.bypass") && this.timeoutManager.isBlocked(player)) {
            String timeoutMessage = this.config.getString("locale.timeout", "You need to wait {seconds} seconds before using this command again.");
            player.sendMessage(timeoutMessage.replace("{seconds}", Long.toString(this.timeoutManager.getRemainingTime(player))));
            return true;
        }

        // get player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(this.config.getString("locale.playerNotFound", "Failed to find player. Please try something else."));
            return true;
        }

        boolean isUserPermitted;

        // check if target user is an allowed target
        User user = luckPerms.getUserManager().getUser(target.getUniqueId());
        if (user != null) {
            boolean hasGroupPermission = player.hasPermission("heads.group." + user.getPrimaryGroup());
            boolean hasWildcardPermission = player.hasPermission("heads.group.*");
            isUserPermitted = hasGroupPermission | hasWildcardPermission;
        } else {
            // unknown users are always allowed
            isUserPermitted = true;
        }

        // is the target player ok?
        if (!isUserPermitted) {
            player.sendMessage(this.config.getString("locale.notPermitted", "You are not allowed to get the head of this player."));
            return true;
        }

        // get head
        this.timeoutManager.block(player); // timeout
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(target);
        playerHead.setItemMeta(skullMeta);

        // give head
        if (player.getInventory().firstEmpty() == -1) {
            // inventory full
            player.getWorld().dropItem(player.getLocation().add(0, 1, 0), playerHead);
        } else {
            // add into inventory
            player.getInventory().addItem(playerHead);
        }

        String resultText = this.config.getString("locale.success", "Congrats, you got the head of {player}.");
        player.sendMessage(resultText.replace("{player}", target.getName()));
        return true;
    }

    /**
     * Handles the TabCompleter.
     * @param sender Command executor.
     * @param command Command instance.
     * @param commandLabel Command name.
     * @param args Arguments.
     * @return A list of possible values for the current argument.
     */
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, String[] args) {
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