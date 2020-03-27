package net.azisaba.lgw.rollback.listener;

import com.shampaggon.crackshot.CSUtility;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.rollback.RollbackWeapons;
import net.azisaba.lgw.rollback.data.RollbackData;
import net.azisaba.lgw.rollback.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@RequiredArgsConstructor
public class JoinListener implements Listener {

    private final RollbackWeapons plugin;

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDataContainer().loadRollbackData(uuid);

            RollbackData data = plugin.getDataContainer().getRollbackData(uuid);
            if (data != null && !data.isCompleted()) {
                p.sendMessage(Chat.f("&c&n補填アイテムが残っています！"));
                p.sendMessage(Chat.f("&e/ri &7で受け取りを完了してください"));
                p.sendMessage(Chat.f("&e/rr &7で内容を見ることができます"));
            }
        });


        if (!p.hasPlayedBefore()) {
            plugin.getNameCoinDataContainer().alreadyGave(p.getUniqueId());
            return;
        }
        if (plugin.getNameCoinDataContainer().alreadyGave(p.getUniqueId())) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!p.isOnline()) {
                return;
            }
            if (!hasEmptySlot(p)) {
                return;
            }

            ItemStack name = new CSUtility().generateWeapon("NAME");
            if (name == null) {
                return;
            }
            name.setAmount(2);
            p.getInventory().addItem(name);

            plugin.getNameCoinDataContainer().setGave(p.getUniqueId());

            p.sendMessage(Chat.f("&a補填用の&d名前変更コイン&aを&e2枚&a付与しました。"));
        }, 20);
    }

    private boolean hasEmptySlot(Player p) {
        return p.getInventory().firstEmpty() >= 0;
    }
}
