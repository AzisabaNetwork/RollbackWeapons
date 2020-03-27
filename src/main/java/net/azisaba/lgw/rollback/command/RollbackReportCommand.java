package net.azisaba.lgw.rollback.command;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.rollback.CrackShotItems;
import net.azisaba.lgw.rollback.RollbackWeapons;
import net.azisaba.lgw.rollback.data.RollbackData;
import net.azisaba.lgw.rollback.utils.BookUtil;
import net.azisaba.lgw.rollback.utils.Chat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class RollbackReportCommand implements CommandExecutor {

    private final RollbackWeapons plugin;

    private HashMap<UUID, ItemStack> books = new HashMap<>();
    private HashMap<UUID, Long> messageCooldown = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player p = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("chat")) {
            if (messageCooldown.getOrDefault(p.getUniqueId(), 0L) > System.currentTimeMillis()) {
                return true;
            }
            sendReportMessage(p);
            messageCooldown.put(p.getUniqueId(), 10000L);
            return true;
        }

        if (books.containsKey(p.getUniqueId())) {
            BookUtil.openBook(books.get(p.getUniqueId()), p);
            return true;
        }

        ItemStack book = generateBook(p);
        if (book == null) {
            return true;
        }

        books.put(p.getUniqueId(), book);
        BookUtil.openBook(book, p);
        return true;
    }

    private String sendReportMessage(Player p) {
        if (!plugin.getDataContainer().hasData(p.getUniqueId())) {
            p.sendMessage(Chat.f("&cあなたはロールバック前にサーバーに参加していません！"));
            return null;
        }

        RollbackData data = plugin.getDataContainer().getRollbackData(p.getUniqueId());
        if (data == null) {
            p.sendMessage(Chat.f("&cデータはロード中です。しばらくお待ちください。"));
            p.sendMessage(Chat.f("&cそれでもロードされない場合はリログしてください。"));
            return null;
        }

        List<String> currentList = null;
        StringBuilder builder;

        builder = new StringBuilder();
        builder.append(Chat.f("&5[ 返ってくる武器一覧 ]")).append("\n");
        currentList = data.getRollbackWeapons();
        while (currentList != null) {
            currentList = appendWeaponNames(builder, currentList);
            p.sendMessage(builder.toString());
            builder = new StringBuilder();
        }

        builder.append(Chat.f("&5[ 使用した武器一覧 ]")).append("\n");
        currentList = data.getUsedWeapons();
        while (currentList != null) {
            currentList = appendWeaponNames(builder, currentList);
            p.sendMessage(builder.toString());
            builder = new StringBuilder();
        }

        builder.append(Chat.f("&5[ 使用した不明な武器一覧 ]")).append("\n");
        currentList = data.getUnknownUsedWeaponNames();
        while (currentList != null) {
            currentList = appendWeaponNames(builder, currentList);
            p.sendMessage(builder.toString());
            builder = new StringBuilder();
        }

        builder.append(Chat.f("&5[ インベントリの武器一覧 ]")).append("\n");
        currentList = data.getInventoryWeapons();
        while (currentList != null) {
            currentList = appendWeaponNames(builder, currentList);
            p.sendMessage(builder.toString());
            builder = new StringBuilder();
        }

        builder.append(Chat.f("&5[ エンチェスの武器一覧 ]")).append("\n");
        currentList = data.getEnderChestWeapons();
        while (currentList != null) {
            currentList = appendWeaponNames(builder, currentList);
            p.sendMessage(builder.toString());
            builder = new StringBuilder();
        }

        return builder.toString();
    }

    private ItemStack generateBook(Player p) {
        if (!plugin.getDataContainer().hasData(p.getUniqueId())) {
            p.sendMessage(Chat.f("&cあなたはロールバック前にサーバーに参加していません！"));
            return null;
        }

        RollbackData data = plugin.getDataContainer().getRollbackData(p.getUniqueId());
        if (data == null) {
            p.sendMessage(Chat.f("&cデータはロード中です。しばらくお待ちください。"));
            p.sendMessage(Chat.f("&cそれでもロードされない場合はリログしてください。"));
            return null;
        }

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setAuthor(Chat.f("&c運営"));
        meta.setTitle(Chat.f("&e補填レポート"));

        int rollbackWeaponsPage, usedWeaponsPage, unknownUsedWeaponPage, inventoryWeaponPage, enderChestWeaponPage;

        meta.addPage("");

        rollbackWeaponsPage = meta.getPageCount() + 1;
        meta.addPage(Strings.repeat("\n", 6) + Chat.f("  &5[ 返ってくる武器一覧 ]"));
        writeWeaponNames(meta, data.getRollbackWeapons());

        usedWeaponsPage = meta.getPageCount() + 1;
        meta.addPage(Strings.repeat("\n", 6) + Chat.f("   &5[ 使用した武器一覧 ]"));
        writeWeaponNames(meta, data.getUsedWeapons());

        unknownUsedWeaponPage = meta.getPageCount() + 1;
        meta.addPage(Strings.repeat("\n", 6) + Chat.f("&5[ 使用した不明な武器一覧 ]"));
        writeWeaponNames(meta, data.getUnknownUsedWeaponNames());

        inventoryWeaponPage = meta.getPageCount() + 1;
        meta.addPage(Strings.repeat("\n", 6) + Chat.f("&5[ インベントリの武器一覧 ]"));
        writeWeaponNames(meta, data.getInventoryWeapons());

        enderChestWeaponPage = meta.getPageCount() + 1;
        meta.addPage(Strings.repeat("\n", 6) + Chat.f(" &5[ エンチェスの武器一覧 ]"));
        writeWeaponNames(meta, data.getEnderChestWeapons());

        meta.setPage(1, Strings.repeat(" ", 10) + Chat.f("[目次]") + "\n" +
                "\n" +
                Chat.f("&5返ってくる武器&7: &0{0}～", rollbackWeaponsPage) + "\n" +
                Chat.f("&5使用した武器&7: &0{0}～", usedWeaponsPage) + "\n" +
                Chat.f("&5使用した不明な武器&7: &0{0}～", unknownUsedWeaponPage) + "\n" +
                Chat.f("&5インベントリの武器&7: &0{0}～", inventoryWeaponPage) + "\n" +
                Chat.f("&5エンチェスの武器&7: &0{0}～", enderChestWeaponPage) + "\n" +
                "\n" +
                Chat.f("&4&n不明な&c武器の&c補填が必要な&c場合はスクショして報告してください。") + "\n\n" +
                Chat.f("&050ページを超えている場合は &d/rr chat"));

        book.setItemMeta(meta);
        return book;
    }

    public void writeWeaponNames(BookMeta meta, List<String> weapons) {
        StringBuilder currentModifyingPage = new StringBuilder();
        int lines = 0;

        for (String id : weapons) {
            String name = CrackShotItems.getItemDisplayName(id);
            currentModifyingPage.append(ChatColor.GRAY).append("● ").append(name).append("\n");
            lines++;

            if (lines >= 7) {
                meta.addPage(currentModifyingPage.toString());
                currentModifyingPage = new StringBuilder();
                lines = 0;
            }
        }
        currentModifyingPage.append("\n").append(Strings.repeat(" ", 20)).append("以上");

        if (!currentModifyingPage.toString().equals("")) {
            meta.addPage(currentModifyingPage.toString());
        }
    }

    public List<String> appendWeaponNames(StringBuilder builder, List<String> weapons) {
        int finished = 0;
        for (String id : weapons) {
            String name = CrackShotItems.getItemDisplayName(id);
            builder.append(ChatColor.GRAY).append("● ").append(name).append("  ");
            finished++;

            if (finished > 100) {
                return weapons.subList(finished, weapons.size());
            }
        }
        builder.append("\n").append(ChatColor.RESET).append("以上").append("\n").append("\n");
        return null;
    }
}
