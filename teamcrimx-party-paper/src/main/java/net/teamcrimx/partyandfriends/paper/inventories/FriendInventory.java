package net.teamcrimx.partyandfriends.paper.inventories;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.builder.item.SkullBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.teamcrimx.partyandfriends.api.NetworkPlayer;
import net.teamcrimx.partyandfriends.api.friends.SimpleFriend;
import net.teamcrimx.partyandfriends.paper.PaperPartyAndFriendsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class FriendInventory {

    private static final String headTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM1YmUzZWY3MzU4ZmY4NjNlMzA2MDg0OGRiYTM2ODdhZTMzYWI5NTg4NzQwZmQ3YjE4OWRjYTZmNzNlOWZjMiJ9fX0=";
    private final PaperPartyAndFriendsPlugin paperPartyAndFriendsPlugin;
    private PaginatedGui friendInventory;

    public FriendInventory(PaperPartyAndFriendsPlugin paperPartyAndFriendsPlugin) {
        this.paperPartyAndFriendsPlugin = paperPartyAndFriendsPlugin;
    }

    public void getFriendOverviewGui(Player player) {
        SimpleFriend simpleFriend = paperPartyAndFriendsPlugin.simpleFriendMap().get(player.getUniqueId());

        if (simpleFriend == null) {
            player.sendMessage(Component.text("Dein Profil konnte nicht geladen werden!"));
            return;
        }

        this.friendInventory = Gui.paginated()
                .title(Component.text("● Deine Freunde"))
                .rows(6)
                .pageSize(36)
                .create();

        this.friendInventory.disableAllInteractions();

        this.friendInventory.getFiller().fillTop(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                .name(Component.empty()).asGuiItem());
        this.friendInventory.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                .name(Component.empty()).asGuiItem());


        this.friendInventory.setItem(6, 8, dev.triumphteam.gui.builder.item.ItemBuilder.skull()
                .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==")
                .name(Component.textOfChildren(Component.text("● ", NamedTextColor.DARK_GRAY),
                        Component.text("Vorherige Seite", NamedTextColor.GRAY)))
                .asGuiItem(event -> {
                    this.friendInventory.previous();
                }));

        this.friendInventory.setItem(6, 9, dev.triumphteam.gui.builder.item.ItemBuilder.skull()
                .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19")
                .name(Component.textOfChildren(Component.text("● ", NamedTextColor.DARK_GRAY),
                        Component.text("Nächste Seite", NamedTextColor.GRAY)))
                .asGuiItem(event -> {
                    this.friendInventory.next();
                }));

        Bukkit.getScheduler().runTaskAsynchronously(paperPartyAndFriendsPlugin.plugin(), () -> {
            List<GuiItem> guiItems = simpleFriend.friends().stream()
                    .sorted((o1, o2) -> Boolean.compare(paperPartyAndFriendsPlugin.playerManager().onlinePlayer(o1) == null,
                            paperPartyAndFriendsPlugin.playerManager().onlinePlayer(o2) == null))
                    .map(uuid -> {
                        NetworkPlayer networkPlayer = new NetworkPlayer(uuid);
                        SkullBuilder itemBuilder = ItemBuilder.skull();

                        Component lore3 = paperPartyAndFriendsPlugin.miniMessage()
                                .deserialize("<#FBBF2B>Klicke für weitere Optionen").decoration(TextDecoration.ITALIC, false);

                        itemBuilder.name(networkPlayer.formattedName().decoration(TextDecoration.ITALIC, false));

                        if (networkPlayer.isOnline()) {
                            itemBuilder.owner(Bukkit.getOfflinePlayer(networkPlayer.uuid()));

                            String onlineServer = networkPlayer.connectedServer();
                            Component lore1 = paperPartyAndFriendsPlugin.miniMessage()
                                    .deserialize("<gray>Online auf <#58A5FA>" + onlineServer)
                                    .decoration(TextDecoration.ITALIC, false);

                            Component lore2 = Component.text("Rang: ", NamedTextColor.GRAY).append(networkPlayer.rank())
                                    .decoration(TextDecoration.ITALIC, false);

                            itemBuilder.lore(lore1, lore2, Component.empty(), lore3.decoration(TextDecoration.ITALIC, false));
                        } else {
                            itemBuilder.texture(headTexture);

                            itemBuilder.lore(Component.empty(), lore3.decoration(TextDecoration.ITALIC, false));
                        }
                        return itemBuilder.asGuiItem(event -> {
                            this.paperPartyAndFriendsPlugin.friendDetailInventory().openDetailInventory(player, simpleFriend, networkPlayer);
                        });

                    }).toList();

            guiItems.forEach(guiItem -> this.friendInventory.addItem(guiItem));
            this.friendInventory.update();
        });

        this.friendInventory.open(player);
    }
}
