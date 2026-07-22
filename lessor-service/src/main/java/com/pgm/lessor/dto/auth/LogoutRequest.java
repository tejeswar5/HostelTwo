package com.pgm.lessor.dto.auth;

/** refreshToken is optional - if the client has one, this also revokes it; the current
 * access token (read from the request's own Authorization header) is always revoked. */
public record LogoutRequest(String refreshToken) {
}
