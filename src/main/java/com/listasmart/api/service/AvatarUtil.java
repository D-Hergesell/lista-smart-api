package com.listasmart.api.service;

/** Iniciais para o avatar (espelha SessionManager.getInitials do app). */
public final class AvatarUtil {

    private AvatarUtil() {}

    public static String initials(String name) {
        if (name == null || name.trim().isEmpty()) return "VC";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            String p = parts[0];
            return p.substring(0, Math.min(2, p.length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
