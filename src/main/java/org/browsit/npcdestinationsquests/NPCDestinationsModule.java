package org.browsit.npcdestinationsquests;

import me.pikamug.quests.BukkitQuestsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.AbstractMap;
import java.util.Map;

public class NPCDestinationsModule extends JavaPlugin {
    private static final BukkitQuestsPlugin quests = (BukkitQuestsPlugin) Bukkit.getServer().getPluginManager().getPlugin("Quests");
    private static final String moduleName = "NPCDestinations Quests Module";
    private static final Map.Entry<String, Short> moduleItem = new AbstractMap.SimpleEntry<>("GOLD_BLOCK", (short)0);

    public static BukkitQuestsPlugin getQuests() {
        return quests;
    }

    public static String getModuleName() {
        return moduleName;
    }

    public static Map.Entry<String, Short> getModuleItem() {
        return moduleItem;
    }

    @Override
    public void onEnable() {
        getLogger().severe(ChatColor.RED + "Move this jar to your " + File.separatorChar + "Quests" + File.separatorChar
                + "modules folder!");
        getServer().getPluginManager().disablePlugin(this);
        setEnabled(false);
    }

    @Override
    public void onDisable() {
    }
}
