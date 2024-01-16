package net.wandermc.enhancements.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

public class Settings {
  public static final String NAMESPACE = "socketenhancements";
  // Lore is italic by default, explicitly setting it to "false" overrides this.
  public static final TextComponent EMPTY_SOCKET_MESSAGE = Component.text("<Empty Socket>").style(Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)));
}
