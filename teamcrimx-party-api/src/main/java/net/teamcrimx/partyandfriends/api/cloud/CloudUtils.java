package net.teamcrimx.partyandfriends.api.cloud;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionManagement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

public class CloudUtils {

    private static final PermissionManagement permissionManagement = InjectionLayer.ext().instance(PermissionManagement.class);

    public static Component getDisplayColor(UUID uuid) {
        final PermissionManagement permissionManagement = InjectionLayer.ext().instance(PermissionManagement.class);
        var permissionUser = permissionManagement.user(uuid);

        if(permissionUser == null) {
            return Component.empty();
        }

        var group = permissionManagement.highestPermissionGroup(permissionUser);

        if(group == null) {
            return Component.empty();
        }

        var color = group.color();

        if(color.startsWith("#")) {
            return Component.empty().color(TextColor.fromHexString(color));
        } else {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(color);
        }
    }

    public static Component getRank(UUID uuid) {
        var permissionUser = permissionManagement.user(uuid);

        Component toReturn = Component.text("Kein Rang", NamedTextColor.GRAY);

        if(permissionUser == null) {
            return toReturn;
        }

        var group = permissionManagement.highestPermissionGroup(permissionUser);

        if(group == null) {
            return toReturn;
        }

        var color = group.color();

        if(color.startsWith("#")) {
            toReturn = Component.empty().color(TextColor.fromHexString(color));
        } else {
            toReturn = LegacyComponentSerializer.legacyAmpersand().deserialize(color);
        }

        return toReturn.append(Component.text(group.name()));
    }

}
