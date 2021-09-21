package com.imjustdoom.justpermissions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MessageUtil {

    public static String translate(String msg) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(Component.text(msg));
    }
}