package net.teamcrimx.partyandfriends.paper.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.teamcrimx.partyandfriends.api.friends.SimpleFriend;
import net.teamcrimx.partyandfriends.paper.PaperPartyAndFriendsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.concurrent.ExecutionException;

public class PlayerJoinListener implements Listener {

    private final PaperPartyAndFriendsPlugin paperPartyAndFriendsPlugin;

    public PlayerJoinListener(PaperPartyAndFriendsPlugin plugin) {
        this.paperPartyAndFriendsPlugin = plugin;
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(this.paperPartyAndFriendsPlugin.plugin(), () -> {
            try {
                this.paperPartyAndFriendsPlugin.simpleFriendMap().put(player.getUniqueId(),
                        SimpleFriend.getSimpleFriendByUUID(player.getUniqueId()).get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, 20L);

        event.joinMessage(null);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, meta -> {
            meta.displayName(Component.text("Profil", TextColor.fromHexString("#4940CC")));
            meta.setPlayerProfile(player.getPlayerProfile());
        });

        player.getInventory().setItem(8, head);

    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        event.quitMessage(null);
    }

}
