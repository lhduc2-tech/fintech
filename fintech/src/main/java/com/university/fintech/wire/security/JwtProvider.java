package com.university.fintech.wire.security;


import com.university.fintech.domain.dto.UserDTO;

public interface JwtProvider {

    String generateToken(UserDTO userDTO);
}