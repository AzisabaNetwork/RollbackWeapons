package net.azisaba.lgw.rollback.data;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.rollback.RollbackWeapons;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class NameCoinDataContainer {

    private final RollbackWeapons plugin;

    private List<UUID> alreadyGave = new ArrayList<>();

    public boolean alreadyGave(UUID uuid) {
        return alreadyGave.contains(uuid);
    }

    public void setGave(UUID uuid) {
        alreadyGave.add(uuid);
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "NameCoins.yml");
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        alreadyGave = conf.getStringList("AlreadyGave").stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }

    public void save() {
        YamlConfiguration conf = new YamlConfiguration();

        conf.set("AlreadyGave", alreadyGave.stream()
                .map(UUID::toString)
                .collect(Collectors.toList()));

        try {
            conf.save(new File(plugin.getDataFolder(), "NameCoins.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
