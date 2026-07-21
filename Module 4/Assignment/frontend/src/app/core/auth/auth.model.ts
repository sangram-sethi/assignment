// Matches LoginRequestDTO
export interface LoginRequest {
  username: string;
  password: string;
}

// The authenticated identity the SPA tracks. The JWT itself is NOT here — it
// lives in an httpOnly cookie the browser manages, invisible to JavaScript.
export interface CurrentUser {
  username: string;
  roles: string[]; // e.g. ["ROLE_ADMIN"]
}

// Matches LoginResponseDTO. tokenType/accessToken exist for non-browser clients;
// the SPA only reads username + roles.
export interface LoginResponse extends CurrentUser {
  tokenType: string;
  accessToken: string;
  expiresInMs: number;
}
