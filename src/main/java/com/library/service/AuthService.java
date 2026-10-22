package com.library.service;

import com.library.dto.request.LoginRequestDTO;
import com.library.dto.response.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO login(LoginRequestDTO request);

    /** Revokes the given JWT so it can no longer be used, even before it expires. */
    void logout(String token);
}
