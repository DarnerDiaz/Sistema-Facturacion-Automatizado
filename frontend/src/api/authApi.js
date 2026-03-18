import apiClient from "./client";

export async function login(payload) {
  const { data } = await apiClient.post("/auth/login", payload);
  return data;
}

export async function register(payload) {
  const { data } = await apiClient.post("/auth/register", payload);
  return data;
}

export async function refreshToken(refreshTokenValue) {
  const { data } = await apiClient.post("/auth/refresh", {
    refreshToken: refreshTokenValue,
  });
  return data;
}
