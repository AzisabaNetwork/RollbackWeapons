package net.azisaba.lgw.rollback.task;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.rollback.RollbackWeapons;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class SaveTask extends BukkitRunnable {

    private final RollbackWeapons plugin;

    @Override
    public void run() {
        plugin.getDataContainer().saveAndUnloadOfflineData();
    }
}
