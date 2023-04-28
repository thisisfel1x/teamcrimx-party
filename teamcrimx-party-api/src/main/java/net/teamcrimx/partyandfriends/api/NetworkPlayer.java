package net.teamcrimx.partyandfriends.api;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionManagement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.teamcrimx.partyandfriends.api.chat.ChatUtils;

import java.util.UUID;

public class NetworkPlayer {

    private final UUID uuid;
    private final String name;
    private Component chatColor;
    private Component formattedName;

    public NetworkPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        this.chatColor = ChatUtils.getDisplayColor(this.uuid);
        this.formattedName = Component.text(name, this.chatColor.color());

    }

    public Component chatColor() {
        return chatColor;
    }

    public Component formattedName() {
        return formattedName;
    }
}
