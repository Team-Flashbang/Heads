package net.fbhosting.heads;

import lombok.Getter;
import net.fbhosting.heads.commands.Head;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public final class HeadsPlugin extends JavaPlugin implements Listener {

    @Getter
    private static HeadsPlugin instance;

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void onEnable() {
        instance = this;

        // validate dependencies
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            log.error("LuckPerms not found! You are missing a dependency. Please install LuckPerms.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // create default configuration
        saveDefaultConfig();

        // register commands
        getCommand("head").setPermission("head.spawn");
        List<String> aliases = this.getConfig().getStringList("aliases.head");
        getCommand("head").setAliases(aliases);
        getCommand("head").setExecutor(new Head());
    }

    @Override
    public void onDisable() {
        log.info("Plugin disabled.");
    }
}
