package net.azisaba.lgw.rollback.command;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.rollback.RollbackWeapons;
import net.azisaba.lgw.rollback.data.RollbackData;
import net.azisaba.lgw.rollback.utils.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class RollbackItemCommand implements CommandExecutor {

    private final RollbackWeapons plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;

        if (!plugin.getDataContainer().hasData(p.getUniqueId())) {
            p.sendMessage(Chat.f("&cあなたはロールバック前に参加していません！"));
            return true;
        }

        RollbackData data = plugin.getDataContainer().getRollbackData(p.getUniqueId());
        if (data == null) {
            p.sendMessage(Chat.f("&cデータはロード中です。しばらくお待ちください。"));
            p.sendMessage(Chat.f("&cそれでもロードされない場合はリログしてください。"));
            return true;
        }

        if (data.isCompleted()) {
            p.sendMessage(Chat.f("&a既にすべてのロールバックアイテムを配布しました。"));
            return true;
        }

        try {
            data.returnItems(p);
        } catch (Exception e) {
            p.sendMessage(Chat.f("&cエラーが発生したためアイテムを付与できませんでした。"));
            p.sendMessage(Chat.f("&eデータは残っているため大丈夫です。"));
            e.printStackTrace();
        }
        return true;
    }
}
