package net.fbhosting.heads;

import net.fbhosting.heads.commands.Head;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Heads extends JavaPlugin implements Listener{

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("Luckperms") == null) {
            log.error("LuckPerms not found! You are missing a dependency. Please install LuckPerms.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // load default configuration
        saveDefaultConfig();

        // register commands
        getCommand("head").setPermission("head.spawn");
        getCommand("head").setExecutor(new Head(this));
    }

    @Override
    public void onDisable() {
        log.info("Plugin disabled.");
    }
}
