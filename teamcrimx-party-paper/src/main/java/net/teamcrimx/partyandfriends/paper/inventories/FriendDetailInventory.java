package net.teamcrimx.partyandfriends.paper.inventories;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import net.kyori.adventure.text.Component;
import net.teamcrimx.partyandfriends.api.NetworkPlayer;
import net.teamcrimx.partyandfriends.api.friends.FriendConstants;
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

        gui.setItem(2, 2, ItemBuilder.skull()
                .name(target.formattedName())
                .owner(Bukkit.getOfflinePlayer(target.uuid()))
                .asGuiItem());

        gui.setItem(2, 4, ItemBuilder.from(Material.BARRIER)
                        .name(Component.text("Freund entfernen"))
                .asGuiItem(event -> {
                    event.getWhoClicked().closeInventory();

                    this.paperPartyAndFriendsPlugin.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL,
                            FriendConstants.FRIEND_REMOVE_MESSAGE,
                            DataBuf.empty().writeUniqueId(owner.uuid()).writeString(target.name()));
                }));

        if(target.isOnline()) {
            gui.setItem(2, 6, ItemBuilder.from(Material.SPYGLASS)
                    .name(Component.text("Freund nachspringen"))
                    .asGuiItem(event -> {
                        event.getWhoClicked().closeInventory();

                        this.paperPartyAndFriendsPlugin.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL,
                                FriendConstants.FRIEND_JUMP_MESSAGE,
                                DataBuf.empty().writeUniqueId(owner.uuid()).writeString(target.name()));
                    }));
            gui.setItem(2, 7, ItemBuilder.from(Material.CAKE)
                    .name(Component.text("Freund in Party einladen"))
                    .asGuiItem(event -> {
                        event.getWhoClicked().closeInventory(); // TODO: party check

                        this.paperPartyAndFriendsPlugin.sendChannelMessageToNode(PartyConstants.PARTY_CHANNEL,
                                PartyConstants.PARTY_INVITE_MESSAGE,
                                DataBuf.empty().writeString(target.name()).writeUniqueId(owner.uuid()));

                    }));
        }

        gui.open(player);
    }
}
