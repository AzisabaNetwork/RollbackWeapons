package net.azisaba.lgw.rollback;

import lombok.Getter;
import net.azisaba.lgw.rollback.command.RollbackItemCommand;
import net.azisaba.lgw.rollback.command.RollbackReportCommand;
import net.azisaba.lgw.rollback.data.NameCoinDataContainer;
import net.azisaba.lgw.rollback.data.RollbackDataContainer;
import net.azisaba.lgw.rollback.listener.JoinListener;
import net.azisaba.lgw.rollback.task.SaveTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class RollbackWeapons extends JavaPlugin {

    private RollbackDataContainer dataContainer;
    private NameCoinDataContainer nameCoinDataContainer;

    @Override
    public void onEnable() {
        dataContainer = new RollbackDataContainer();
        nameCoinDataContainer = new NameCoinDataContainer(this);
        nameCoinDataContainer.load();

        Bukkit.getPluginCommand("rollbackitem").setExecutor(new RollbackItemCommand(this));
        Bukkit.getPluginCommand("rollbackreport").setExecutor(new RollbackReportCommand(this));

        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);

        new SaveTask(this).runTaskTimerAsynchronously(this, 20 * 60, 20 * 60 * 5);

        for (Player p : Bukkit.getOnlinePlayers()) {
            dataContainer.loadRollbackData(p.getUniqueId());
        }

        Bukkit.getLogger().info(getName() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (dataContainer != null) {
            dataContainer.saveAllData();
        }
        if (nameCoinDataContainer != null) {
            nameCoinDataContainer.save();
        }

        Bukkit.getLogger().info(getName() + " disabled.");
    }
}
