package net.teamcrimx.partyandfriends.api.constants;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class ChatConstants {

    public static Component partyPrefix = Component.text("Party", TextColor.fromHexString("#C800C8"))
            .append(Component.text(" ● ", NamedTextColor.DARK_GRAY))
            .append(Component.empty().color(NamedTextColor.GRAY));
    private final static Component a = partyPrefix.append(Component.text("Liste der Untercommands", NamedTextColor.GRAY));
    private final static Component b = partyPrefix.append(Component.text("/party create", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Erstelle eine Party", NamedTextColor.GRAY));
    private final static Component c = partyPrefix.append(Component.text("/party join <Name>", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Trete eine Party eines Spielers bei", NamedTextColor.GRAY));
    private final static Component d = partyPrefix.append(Component.text("/party invite <Name>", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Lade einen Spieler zu deiner Party ein", NamedTextColor.GRAY));
    private final static Component e = partyPrefix.append(Component.text("/party leave", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Verlasse die Party", NamedTextColor.GRAY));
    private final static Component f = partyPrefix.append(Component.text("/party kick <Name>", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Kicke einen Spieler aus deine Party", NamedTextColor.GRAY));
    private final static Component g = partyPrefix.append(Component.text("/party close", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Beende die Party", NamedTextColor.GRAY));
    private final static Component h = partyPrefix.append(Component.text("/party promote <Name>", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Befördere einen Spieler zum neuen Leiter der Party", NamedTextColor.GRAY));
    private final static Component i = partyPrefix.append(Component.text("/party list", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Zeige Details über deine Party", NamedTextColor.GRAY));
    private final static Component j = partyPrefix.append(Component.text("/pc", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Schreibe im Partychat", NamedTextColor.GRAY));
    public static final Component partyHelpMessage = Component.empty().append(a).append(Component.newline())
            .append(b).append(Component.newline()).append(c).append(Component.newline()).append(d)
            .append(Component.newline()).append(e).append(Component.newline()).append(f).append(Component.newline())
            .append(g).append(Component.newline()).append(h).append(Component.newline()).append(j)
            .append(Component.newline()).append(i);
    public static Component friendPrefix = Component.text("Friends", TextColor.fromHexString("#73E2A7"))
            .append(Component.text(" ● ", NamedTextColor.DARK_GRAY))
            .append(Component.empty().color(NamedTextColor.GRAY));
    private final static Component a1 = friendPrefix.append(Component.text("Liste der Untercommands", NamedTextColor.GRAY));
    private final static Component b1 = friendPrefix.append(Component.text("/friend add <Name>", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Sende eine Freundschaftsanfrage", NamedTextColor.GRAY));
    private final static Component c1 = friendPrefix.append(Component.text("/friend accept <Name>", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Akzeptiere eine Freundschaftsanfrage", NamedTextColor.GRAY));
    private final static Component d1 = friendPrefix.append(Component.text("/friend deny <Name>", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Lehne eine Freundschaftsanfrage ab", NamedTextColor.GRAY));
    private final static Component e1 = friendPrefix.append(Component.text("/friend remove <Name>", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Entferne einen Freund aus deiner Freundesliste", NamedTextColor.GRAY));
    private final static Component f1 = friendPrefix.append(Component.text("/friend acceptAll", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Akzeptiere alle Freundschaftsanfragen", NamedTextColor.GRAY));
    private final static Component g1 = friendPrefix.append(Component.text("/friend denyAll", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Lehne alle Freundschaftsanfragen ab", NamedTextColor.GRAY));
    private final static Component h1 = friendPrefix.append(Component.text("/friend removeAll", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Entferne alle deine Freunde aus deiner Freundesliste", NamedTextColor.GRAY));
    private final static Component i1 = friendPrefix.append(Component.text("/friend listRequests", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Zeige offene Freundschaftsanfragen an", NamedTextColor.GRAY));
    private final static Component j1 = friendPrefix.append(Component.text("/friend list", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Zeige dir eine Liste deiner Freunde, die aktuell online sind", NamedTextColor.GRAY));
    private final static Component k1 = friendPrefix.append(Component.text("/friend jump <Name>", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Springe auf den Server deines Freunds", NamedTextColor.GRAY));
    private final static Component l1 = friendPrefix.append(Component.text("/msg <Name>", NamedTextColor.GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(Component.text("Sende dem Spieler eine Privatnachricht", NamedTextColor.GRAY));

    public static final Component friendHelpMessage = Component.empty().append(a1).append(Component.newline())
            .append(b1).append(Component.newline()).append(c1).append(Component.newline()).append(d1)
            .append(Component.newline()).append(e1).append(Component.newline()).append(f1).append(Component.newline())
            .append(g1).append(Component.newline()).append(h1).append(Component.newline()).append(i1)
            .append(Component.newline()).append(j1).append(Component.newline()).append(k1).append(Component.newline()).append(l1);

}
