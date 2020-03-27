package net.azisaba.lgw.rollback;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.CSUtility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class CrackShotItems {

    public static ItemStack getItem(String id) {
        if (id.equals("SG19") || id.equals("M320")) {
            return new ItemStack(Material.EMERALD_BLOCK, 40);
        }

        return new CSUtility().generateWeapon(id);
    }

    private static HashMap<String, String> displayNameCache = new HashMap<>();
    private static CSDirector crackshot;

    public static String getItemDisplayName(String id) {
        if (displayNameCache.containsKey(id)) {
            return displayNameCache.get(id);
        }

        if (crackshot == null) {
            crackshot = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");
        }

        if (crackshot.getString(id + ".Item_Information.Item_Name") != null) {
            return crackshot.getString(id + ".Item_Information.Item_Name");
        }

        CSUtility util = new CSUtility();
        if (util.generateWeapon(id) == null) {
            return id;
        }
        String name = util.generateWeapon(id).getItemMeta().getDisplayName();

        if (name == null) {
            return id;
        }
        displayNameCache.put(id, name);
        return name;
    }
}
