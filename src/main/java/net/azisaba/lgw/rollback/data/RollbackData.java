package net.azisaba.lgw.rollback.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.rollback.CrackShotItems;
import net.azisaba.lgw.rollback.utils.Chat;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class RollbackData {

    private final UUID uuid;

    private boolean completed = false;

    private List<String> usedWeapons, unknownUsedWeaponNames, inventoryWeapons, enderChestWeapons, rollbackWeapons, alreadyGaveRollbackWeapons;

    public void load() {
        File file = new File(".").toPath().resolve("plugins/RollbackWeapons/PlayerData/" + uuid.toString() + ".yml").toFile();
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

        usedWeapons = conf.getStringList("Weapons");
        unknownUsedWeaponNames = conf.getStringList("UnknownWeapons");
        inventoryWeapons = conf.getStringList("InventoryWeapons");
        enderChestWeapons = conf.getStringList("EnderChestItems");
        rollbackWeapons = conf.getStringList("CompleteWeapons");
        alreadyGaveRollbackWeapons = conf.getStringList("AlreadyGaveRollbackWeapons");

        completed = alreadyGaveRollbackWeapons.size() >= rollbackWeapons.size();
    }

    protected boolean save() {
        File file = new File(".").toPath().resolve("plugins/RollbackWeapons/PlayerData/" + uuid.toString() + ".yml").toFile();
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

        conf.set("AlreadyGaveRollbackWeapons", alreadyGaveRollbackWeapons);

        try {
            conf.save(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void returnItems(Player p) {
        if (p.getUniqueId() != uuid) {
            throw new IllegalArgumentException("Invalid uuid player.");
        }

        for (int i = 0; i < 36; i++) {
            ItemStack item = p.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                String nextId = getNextWeapon();
                if (nextId == null) {
                    completed = true;
                    p.sendMessage(Chat.f("&a全てのロールバックアイテムを受け取りました。"));
                    return;
                }

                ItemStack rollbackItem = CrackShotItems.getItem(nextId);
                if (rollbackItem == null) {
                    p.sendMessage(Chat.f("&b{0} &eの補填ができませんでした。報告してください。", nextId));
                    continue;
                }

                p.getInventory().addItem(rollbackItem);
                p.sendMessage(Chat.f("{0} &cを受け取りました。", CrackShotItems.getItemDisplayName(nextId)));
                alreadyGaveRollbackWeapons.add(nextId);
            }
        }

        int complete = alreadyGaveRollbackWeapons.size();
        int total = rollbackWeapons.size();

        if (complete >= total) {
            completed = true;
            p.sendMessage(Chat.f("&a全てのロールバックアイテムを受け取りました。"));
        } else {
            p.sendMessage(Chat.f("&a残り &e{0}個 &aのアイテムが残っています。インベントリを空け、再度&c/rollbackitems&aを実行して受け取ってください。", total - complete));
        }
    }

    private String getNextWeapon() {
        for (String id : rollbackWeapons) {
            if (!alreadyGaveRollbackWeapons.contains(id)) {
                return id;
            }
        }

        return null;
    }
}
