package net.teamcrimx.partyandfriends.paper.inventories;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.builder.item.SkullBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import eu.cloudnetservice.modules.bridge.player.CloudOfflinePlayer;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.teamcrimx.partyandfriends.api.chat.ChatUtils;
import net.teamcrimx.partyandfriends.api.friends.SimpleFriend;
import net.teamcrimx.partyandfriends.paper.PaperPartyAndFriendsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class FriendInventory {

    private static PaperPartyAndFriendsPlugin paperPartyAndFriendsPlugin;

    public FriendInventory(PaperPartyAndFriendsPlugin paperPartyAndFriendsPlugin) {
        FriendInventory.paperPartyAndFriendsPlugin = paperPartyAndFriendsPlugin;
    }

    private static PaginatedGui friendInventory;


    public static void paginatedGui(Player player) {
        SimpleFriend simpleFriend = paperPartyAndFriendsPlugin.simpleFriendMap().get(player.getUniqueId());

        if(simpleFriend == null) {
            player.sendMessage(Component.text("Dein Profil konnte nicht geladen werden!"));
            return;
        }

        friendInventory = Gui.paginated()
                .title(Component.text("● Deine Freunde"))
                .rows(6)
                .pageSize(36)
                .create();

        friendInventory.disableAllInteractions();

        friendInventory.getFiller().fillTop(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                .name(Component.empty()).asGuiItem());
        friendInventory.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                .name(Component.empty()).asGuiItem());


        friendInventory.setItem(6, 8, dev.triumphteam.gui.builder.item.ItemBuilder.skull()
                .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==")
                .name(Component.textOfChildren(Component.text("● ", NamedTextColor.DARK_GRAY),
                        Component.text("Vorherige Seite", NamedTextColor.GRAY)))
                .asGuiItem(event -> {
                    friendInventory.previous();
                }));

        friendInventory.setItem(6, 9, dev.triumphteam.gui.builder.item.ItemBuilder.skull()
                .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19")
                .name(Component.textOfChildren(Component.text("● ", NamedTextColor.DARK_GRAY),
                        Component.text("Nächste Seite", NamedTextColor.GRAY)))
                .asGuiItem(event -> {
                    friendInventory.next();
                }));

        Bukkit.getScheduler().runTaskAsynchronously(paperPartyAndFriendsPlugin.plugin(), () -> {
            List<GuiItem> guiItems = simpleFriend.friends().stream()
                    .sorted((o1, o2) -> Boolean.compare(paperPartyAndFriendsPlugin.playerManager().onlinePlayer(o1) != null,
                            paperPartyAndFriendsPlugin.playerManager().onlinePlayer(o2) != null))
                    .map(uuid -> {
                        SkullBuilder itemBuilder = ItemBuilder.skull();

                        Component lore2 = paperPartyAndFriendsPlugin.miniMessage()
                                .deserialize("<#FBBF2B>Klicke für weitere Optionen").decoration(TextDecoration.ITALIC, false);

                        CloudPlayer cloudPlayer = paperPartyAndFriendsPlugin.playerManager().onlinePlayer(uuid);
                        if(cloudPlayer != null) {
                            itemBuilder.name(ChatUtils.getDisplayColor(cloudPlayer.uniqueId()).append(Component.text(cloudPlayer.name())));
                            itemBuilder.owner(Bukkit.getOfflinePlayer(cloudPlayer.uniqueId()));

                            String onlineServer = cloudPlayer.connectedService().serverName();
                            Component lore1 = paperPartyAndFriendsPlugin.miniMessage()
                                    .deserialize("<gray>Online auf <#58A5FA>" + onlineServer);

                            itemBuilder.lore(lore1, Component.empty(), lore2.decoration(TextDecoration.ITALIC, false));
                        } else {
                            CloudOfflinePlayer cloudOfflinePlayer = paperPartyAndFriendsPlugin.playerManager().offlinePlayer(uuid);

                            itemBuilder.name(ChatUtils.getDisplayColor(cloudOfflinePlayer.uniqueId()).append(Component.text(cloudOfflinePlayer.name())));
                            itemBuilder.owner(Bukkit.getOfflinePlayer(cloudOfflinePlayer.uniqueId()));

                            itemBuilder.lore(Component.empty(), lore2.decoration(TextDecoration.ITALIC, false));
                        }

                        return itemBuilder.asGuiItem();

                    }).toList();

            guiItems.forEach(guiItem -> friendInventory.addItem(guiItem));
            friendInventory.update();
        });

        friendInventory.open(player);
    }
}
