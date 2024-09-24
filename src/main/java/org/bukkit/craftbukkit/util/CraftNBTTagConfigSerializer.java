package org.bukkit.craftbukkit.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class CraftNBTTagConfigSerializer {

    private static final Pattern ARRAY = Pattern.compile("^\\[.*]");
    private static final Pattern INTEGER = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)?i", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOUBLE = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", Pattern.CASE_INSENSITIVE);
    private static final net.minecraft.nbt.TagParser MOJANGSON_PARSER = new net.minecraft.nbt.TagParser(new StringReader(""));

    public static String serialize(@NotNull final net.minecraft.nbt.Tag base) {
        final net.minecraft.nbt.SnbtPrinterTagVisitor snbtVisitor = new net.minecraft.nbt.SnbtPrinterTagVisitor();
        return snbtVisitor.visit(base);
    }

    public static net.minecraft.nbt.Tag deserialize(final Object object) {
        // The new logic expects the top level object to be a single string, holding the entire nbt tag as SNBT.
        if (object instanceof final String snbtString) {
            try {
                return net.minecraft.nbt.TagParser.parseTag(snbtString);
            } catch (final CommandSyntaxException e) {
                throw new RuntimeException("Failed to deserialise nbt", e);
            }
        } else { // Legacy logic is passed to the internal legacy deserialization that attempts to read the old format that *unsuccessfully* attempted to read/write nbt to a full yml tree.
            return internalLegacyDeserialization(object);
        }
    }

    private static net.minecraft.nbt.Tag internalLegacyDeserialization(@NotNull final Object object) {
        if (object instanceof Map) {
            net.minecraft.nbt.CompoundTag compound = new net.minecraft.nbt.CompoundTag();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) object).entrySet()) {
                compound.put(entry.getKey(), internalLegacyDeserialization(entry.getValue()));
            }

            return compound;
        } else if (object instanceof List) {
            List<Object> list = (List<Object>) object;
            if (list.isEmpty()) {
                return new net.minecraft.nbt.ListTag(); // Default
            }

            net.minecraft.nbt.ListTag tagList = new net.minecraft.nbt.ListTag();
            for (Object tag : list) {
                tagList.add(internalLegacyDeserialization(tag));
            }

            return tagList;
        } else if (object instanceof String) {
            String string = (String) object;

            if (ARRAY.matcher(string).matches()) {
                try {
                    return new net.minecraft.nbt.TagParser(new StringReader(string)).readArrayTag();
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException("Could not deserialize found list ", e);
                }
            } else if (INTEGER.matcher(string).matches()) { //Read integers on our own
                return net.minecraft.nbt.IntTag.valueOf(Integer.parseInt(string.substring(0, string.length() - 1)));
            } else if (DOUBLE.matcher(string).matches()) {
                return net.minecraft.nbt.DoubleTag.valueOf(Double.parseDouble(string.substring(0, string.length() - 1)));
            } else {
                net.minecraft.nbt.Tag nbtBase = MOJANGSON_PARSER.type(string);

                if (nbtBase instanceof net.minecraft.nbt.IntTag) { // If this returns an integer, it did not use our method from above
                    return net.minecraft.nbt.StringTag.valueOf(nbtBase.getAsString()); // It then is a string that was falsely read as an int
                } else if (nbtBase instanceof net.minecraft.nbt.DoubleTag) {
                    return net.minecraft.nbt.StringTag.valueOf(String.valueOf(((net.minecraft.nbt.DoubleTag) nbtBase).getAsDouble())); // Doubles add "d" at the end
                } else {
                    return nbtBase;
                }
            }
        }

        throw new RuntimeException("Could not deserialize net.minecraft.nbt.Tag");
    }
}
