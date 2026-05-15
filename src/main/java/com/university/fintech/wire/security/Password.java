package com.university.fintech.wire.security;

public interface Password {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}