package net.teamcrimx.partyandfriends.paper.inventories;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import net.teamcrimx.partyandfriends.api.NetworkPlayer;
import net.teamcrimx.partyandfriends.api.friends.SimpleFriend;
import net.teamcrimx.partyandfriends.api.party.PartyConstants;
import net.teamcrimx.partyandfriends.paper.PaperPartyAndFriendsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FriendDetailInventory {

    private final PaperPartyAndFriendsPlugin paperPartyAndFriendsPlugin;

    public FriendDetailInventory(PaperPartyAndFriendsPlugin paperPartyAndFriendsPlugin) {
        this.paperPartyAndFriendsPlugin = paperPartyAndFriendsPlugin;
    }

    public void openDetailInventory(Player player, SimpleFriend owner, NetworkPlayer target) {
        Gui gui = Gui.gui()
                .title(Component.text("● Details"))
                .rows(3)
                .create();

        gui.disableAllInteractions();
        gui.getFiller().fillTop(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());
        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());

        gui.setItem(1, 1, ItemBuilder.skull()
                .name(target.formattedName())
                .owner(Bukkit.getOfflinePlayer(target.uuid()))
                .asGuiItem());

        gui.setItem(1, 3, ItemBuilder.from(Material.BARRIER)
                        .name(Component.text("Freund entfernen"))
                .asGuiItem(event -> {
                    event.getWhoClicked().closeInventory();
                    ((Player) event.getWhoClicked()).performCommand("/friend remove " + target.name());
                }));

        if(owner.isOnline()) {
            gui.setItem(1, 4, ItemBuilder.from(Material.SPYGLASS)
                            .name(Component.text("Freund nachspringen"))
                    .asGuiItem(event -> {
                        event.getWhoClicked().closeInventory();
                        ((Player) event.getWhoClicked()).performCommand("/friend jump " + target.name());
                    }));
            gui.setItem(1, 5, ItemBuilder.from(Material.CAKE)
                    .name(Component.text("Freund in Party einladen"))
                    .asGuiItem(event -> {
                        event.getWhoClicked().closeInventory(); // TODO: party check
                        ((Player) event.getWhoClicked()).performCommand("/party invite " + target.name());
                    }));
        }

        gui.open(player);
    }

}
