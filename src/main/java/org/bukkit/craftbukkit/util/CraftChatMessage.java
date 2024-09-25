package org.bukkit.craftbukkit.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ethylenemc.EthyleneStatic;
import org.bukkit.ChatColor;

public final class CraftChatMessage {

    private static final Pattern LINK_PATTERN = Pattern.compile("((?:(?:https?):\\/\\/)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:[" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + " \\n]|$))))");
    private static final Map<Character, net.minecraft.ChatFormatting> formatMap;

    static {
        Builder<Character, net.minecraft.ChatFormatting> builder = ImmutableMap.builder();
        for (net.minecraft.ChatFormatting format : net.minecraft.ChatFormatting.values()) {
            builder.put(Character.toLowerCase(format.toString().charAt(1)), format);
        }
        formatMap = builder.build();
    }

    public static net.minecraft.ChatFormatting getColor(ChatColor color) {
        return formatMap.get(color.getChar());
    }

    public static ChatColor getColor(net.minecraft.ChatFormatting format) {
        return ChatColor.getByChar(format.code);
    }

    private static final class StringMessage {
        private static final Pattern INCREMENTAL_PATTERN = Pattern.compile("(" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + "[0-9a-fk-orx])|((?:(?:https?):\\/\\/)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:[" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + " \\n]|$))))|(\\n)", Pattern.CASE_INSENSITIVE);
        // Separate pattern with no group 3, new lines are part of previous string
        private static final Pattern INCREMENTAL_PATTERN_KEEP_NEWLINES = Pattern.compile("(" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + "[0-9a-fk-orx])|((?:(?:https?):\\/\\/)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:[" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + " ]|$))))", Pattern.CASE_INSENSITIVE);
        // ChatColor.b does not explicitly reset, its more of empty
        private static final net.minecraft.network.chat.Style RESET = net.minecraft.network.chat.Style.EMPTY.withBold(false).withItalic(false).withUnderlined(false).withStrikethrough(false).withObfuscated(false);

        private final List<net.minecraft.network.chat.Component> list = new ArrayList<net.minecraft.network.chat.Component>();
        private net.minecraft.network.chat.MutableComponent currentChatComponent = net.minecraft.network.chat.Component.empty();
        private net.minecraft.network.chat.Style modifier = net.minecraft.network.chat.Style.EMPTY;
        private final net.minecraft.network.chat.Component[] output;
        private int currentIndex;
        private StringBuilder hex;
        private final String message;

        private StringMessage(String message, boolean keepNewlines, boolean plain) {
            this.message = message;
            if (message == null) {
                output = new net.minecraft.network.chat.Component[]{currentChatComponent};
                return;
            }
            list.add(currentChatComponent);

            Matcher matcher = (keepNewlines ? INCREMENTAL_PATTERN_KEEP_NEWLINES : INCREMENTAL_PATTERN).matcher(message);
            String match = null;
            boolean needsAdd = false;
            while (matcher.find()) {
                int groupId = 0;
                while ((match = matcher.group(++groupId)) == null) {
                    // NOOP
                }
                int index = matcher.start(groupId);
                if (index > currentIndex) {
                    needsAdd = false;
                    appendNewComponent(index);
                }
                switch (groupId) {
                case 1:
                    char c = match.toLowerCase(Locale.ROOT).charAt(1);
                    net.minecraft.ChatFormatting format = formatMap.get(c);

                    if (c == 'x') {
                        hex = new StringBuilder("#");
                    } else if (hex != null) {
                        hex.append(c);

                        if (hex.length() == 7) {
                            modifier = RESET.withColor(net.minecraft.network.chat.TextColor.parseColor(hex.toString()).result().get());
                            hex = null;
                        }
                    } else if (format.isFormat() && format != net.minecraft.ChatFormatting.RESET) {
                        switch (format) {
                        case BOLD:
                            modifier = modifier.withBold(Boolean.TRUE);
                            break;
                        case ITALIC:
                            modifier = modifier.withItalic(Boolean.TRUE);
                            break;
                        case STRIKETHROUGH:
                            modifier = modifier.withStrikethrough(Boolean.TRUE);
                            break;
                        case UNDERLINE:
                            modifier = modifier.withUnderlined(Boolean.TRUE);
                            break;
                        case OBFUSCATED:
                            modifier = modifier.withObfuscated(Boolean.TRUE);
                            break;
                        default:
                            throw new AssertionError("Unexpected message format");
                        }
                    } else { // Color resets formatting
                        modifier = RESET.withColor(format);
                    }
                    needsAdd = true;
                    break;
                case 2:
                    if (plain) {
                        appendNewComponent(matcher.end(groupId));
                    } else {
                        if (!(match.startsWith("http://") || match.startsWith("https://"))) {
                            match = "http://" + match;
                        }
                        modifier = modifier.withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.OPEN_URL, match));
                        appendNewComponent(matcher.end(groupId));
                        modifier = modifier.withClickEvent((net.minecraft.network.chat.ClickEvent) null);
                    }
                    break;
                case 3:
                    if (needsAdd) {
                        appendNewComponent(index);
                    }
                    currentChatComponent = null;
                    break;
                }
                currentIndex = matcher.end(groupId);
            }

            if (currentIndex < message.length() || needsAdd) {
                appendNewComponent(message.length());
            }

            output = list.toArray(new net.minecraft.network.chat.Component[list.size()]);
        }

        private void appendNewComponent(int index) {
            net.minecraft.network.chat.Component addition = net.minecraft.network.chat.Component.literal(message.substring(currentIndex, index)).setStyle(modifier);
            currentIndex = index;
            if (currentChatComponent == null) {
                currentChatComponent = net.minecraft.network.chat.Component.empty();
                list.add(currentChatComponent);
            }
            currentChatComponent.append(addition);
        }

        private net.minecraft.network.chat.Component[] getOutput() {
            return output;
        }
    }

    public static Optional<net.minecraft.network.chat.Component> fromStringOrOptional(String message) {
        return Optional.ofNullable(fromStringOrNull(message));
    }

    public static Optional<net.minecraft.network.chat.Component> fromStringOrOptional(String message, boolean keepNewlines) {
        return Optional.ofNullable(fromStringOrNull(message, keepNewlines));
    }

    public static net.minecraft.network.chat.Component fromStringOrNull(String message) {
        return fromStringOrNull(message, false);
    }

    public static net.minecraft.network.chat.Component fromStringOrNull(String message, boolean keepNewlines) {
        return (message == null || message.isEmpty()) ? null : fromString(message, keepNewlines)[0];
    }

    public static net.minecraft.network.chat.Component fromStringOrEmpty(String message) {
        return fromStringOrEmpty(message, false);
    }

    public static net.minecraft.network.chat.Component fromStringOrEmpty(String message, boolean keepNewlines) {
        return fromString(message, keepNewlines)[0];
    }

    public static net.minecraft.network.chat.Component[] fromString(String message) {
        return fromString(message, false);
    }

    public static net.minecraft.network.chat.Component[] fromString(String message, boolean keepNewlines) {
        return fromString(message, keepNewlines, false);
    }

    public static net.minecraft.network.chat.Component[] fromString(String message, boolean keepNewlines, boolean plain) {
        return new StringMessage(message, keepNewlines, plain).getOutput();
    }

    public static String toJSON(net.minecraft.network.chat.Component component) {
        return net.minecraft.network.chat.Component.Serializer.toJson(component, EthyleneStatic.getDefaultRegistryAccess());
    }

    public static String toJSONOrNull(net.minecraft.network.chat.Component component) {
        if (component == null) return null;
        return toJSON(component);
    }

    public static net.minecraft.network.chat.Component fromJSON(String jsonMessage) throws JsonParseException {
        // Note: This also parses plain Strings to text components.
        // Note: An empty message (empty, or only consisting of whitespace) results in null rather than a parse exception.
        return net.minecraft.network.chat.Component.Serializer.fromJson(jsonMessage, EthyleneStatic.getDefaultRegistryAccess());
    }

    public static net.minecraft.network.chat.Component fromJSONOrNull(String jsonMessage) {
        if (jsonMessage == null) return null;
        try {
            return fromJSON(jsonMessage); // Can return null
        } catch (JsonParseException ex) {
            return null;
        }
    }

    public static net.minecraft.network.chat.Component fromJSONOrString(String message) {
        return fromJSONOrString(message, false);
    }

    public static net.minecraft.network.chat.Component fromJSONOrString(String message, boolean keepNewlines) {
        return fromJSONOrString(message, false, keepNewlines);
    }

    public static net.minecraft.network.chat.Component fromJSONOrString(String message, boolean nullable, boolean keepNewlines) {
        return fromJSONOrString(message, nullable, keepNewlines, Integer.MAX_VALUE, false);
    }

    public static net.minecraft.network.chat.Component fromJSONOrString(String message, boolean nullable, boolean keepNewlines, int maxLength, boolean checkJsonContentLength) {
        if (message == null) message = "";
        if (nullable && message.isEmpty()) return null;
        net.minecraft.network.chat.Component component = fromJSONOrNull(message);
        if (component != null) {
            if (checkJsonContentLength) {
                String content = fromComponent(component);
                String trimmedContent = trimMessage(content, maxLength);
                if (content != trimmedContent) { // Identity comparison is fine here
                    // Note: The resulting text has all non-plain text features stripped.
                    return fromString(trimmedContent, keepNewlines)[0];
                }
            }
            return component;
        } else {
            message = trimMessage(message, maxLength);
            return fromString(message, keepNewlines)[0];
        }
    }

    public static String trimMessage(String message, int maxLength) {
        if (message != null && message.length() > maxLength) {
            return message.substring(0, maxLength);
        } else {
            return message;
        }
    }

    public static String fromComponent(net.minecraft.network.chat.Component component) {
        if (component == null) return "";
        StringBuilder out = new StringBuilder();

        boolean hadFormat = false;
        for (net.minecraft.network.chat.Component c : component) {
            net.minecraft.network.chat.Style modi = c.getStyle();
            net.minecraft.network.chat.TextColor color = modi.getColor();
            if (c.getContents() != net.minecraft.network.chat.contents.PlainTextContents.EMPTY || color != null) {
                if (color != null) {
                    if (color.format != null) {
                        out.append(color.format);
                    } else {
                        out.append(ChatColor.COLOR_CHAR).append("x");
                        for (char magic : color.serialize().substring(1).toCharArray()) {
                            out.append(ChatColor.COLOR_CHAR).append(magic);
                        }
                    }
                    hadFormat = true;
                } else if (hadFormat) {
                    out.append(ChatColor.RESET);
                    hadFormat = false;
                }
            }
            if (modi.isBold()) {
                out.append(net.minecraft.ChatFormatting.BOLD);
                hadFormat = true;
            }
            if (modi.isItalic()) {
                out.append(net.minecraft.ChatFormatting.ITALIC);
                hadFormat = true;
            }
            if (modi.isUnderlined()) {
                out.append(net.minecraft.ChatFormatting.UNDERLINE);
                hadFormat = true;
            }
            if (modi.isStrikethrough()) {
                out.append(net.minecraft.ChatFormatting.STRIKETHROUGH);
                hadFormat = true;
            }
            if (modi.isObfuscated()) {
                out.append(net.minecraft.ChatFormatting.OBFUSCATED);
                hadFormat = true;
            }
            c.getContents().visit((x) -> {
                out.append(x);
                return Optional.empty();
            });
        }
        return out.toString();
    }

    public static net.minecraft.network.chat.Component fixComponent(net.minecraft.network.chat.MutableComponent component) {
        Matcher matcher = LINK_PATTERN.matcher("");
        return fixComponent(component, matcher);
    }

    private static net.minecraft.network.chat.Component fixComponent(net.minecraft.network.chat.MutableComponent component, Matcher matcher) {
        if (component.getContents() instanceof net.minecraft.network.chat.contents.PlainTextContents) {
            net.minecraft.network.chat.contents.PlainTextContents text = ((net.minecraft.network.chat.contents.PlainTextContents) component.getContents());
            String msg = text.text();
            if (matcher.reset(msg).find()) {
                matcher.reset();

                net.minecraft.network.chat.Style modifier = component.getStyle();
                List<net.minecraft.network.chat.Component> extras = new ArrayList<net.minecraft.network.chat.Component>();
                List<net.minecraft.network.chat.Component> extrasOld = new ArrayList<net.minecraft.network.chat.Component>(component.getSiblings());
                component = net.minecraft.network.chat.Component.empty();

                int pos = 0;
                while (matcher.find()) {
                    String match = matcher.group();

                    if (!(match.startsWith("http://") || match.startsWith("https://"))) {
                        match = "http://" + match;
                    }

                    net.minecraft.network.chat.MutableComponent prev = net.minecraft.network.chat.Component.literal(msg.substring(pos, matcher.start()));
                    prev.setStyle(modifier);
                    extras.add(prev);

                    net.minecraft.network.chat.MutableComponent link = net.minecraft.network.chat.Component.literal(matcher.group());
                    net.minecraft.network.chat.Style linkModi = modifier.withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.OPEN_URL, match));
                    link.setStyle(linkModi);
                    extras.add(link);

                    pos = matcher.end();
                }

                net.minecraft.network.chat.MutableComponent prev = net.minecraft.network.chat.Component.literal(msg.substring(pos));
                prev.setStyle(modifier);
                extras.add(prev);
                extras.addAll(extrasOld);

                for (net.minecraft.network.chat.Component c : extras) {
                    component.append(c);
                }
            }
        }

        List<net.minecraft.network.chat.Component> extras = component.getSiblings();
        for (int i = 0; i < extras.size(); i++) {
            net.minecraft.network.chat.Component comp = extras.get(i);
            if (comp.getStyle() != null && comp.getStyle().getClickEvent() == null) {
                extras.set(i, fixComponent(comp.copy(), matcher));
            }
        }

        if (component.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents) {
            Object[] subs = ((net.minecraft.network.chat.contents.TranslatableContents) component.getContents()).getArgs();
            for (int i = 0; i < subs.length; i++) {
                Object comp = subs[i];
                if (comp instanceof net.minecraft.network.chat.Component) {
                    net.minecraft.network.chat.Component c = (net.minecraft.network.chat.Component) comp;
                    if (c.getStyle() != null && c.getStyle().getClickEvent() == null) {
                        subs[i] = fixComponent(c.copy(), matcher);
                    }
                } else if (comp instanceof String && matcher.reset((String) comp).find()) {
                    subs[i] = fixComponent(net.minecraft.network.chat.Component.literal((String) comp), matcher);
                }
            }
        }

        return component;
    }

    private CraftChatMessage() {
    }
}
