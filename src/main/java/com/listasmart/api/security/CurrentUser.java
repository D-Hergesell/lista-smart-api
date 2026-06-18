package com.listasmart.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Atalho para obter o id do usuario autenticado a partir do JWT. */
public final class CurrentUser {

    private CurrentUser() {}

    public static Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            return null;
        }
        return userId;
    }
}
