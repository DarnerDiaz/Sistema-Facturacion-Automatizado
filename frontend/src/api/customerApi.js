import apiClient from "./client";

export async function getCustomers() {
  const { data } = await apiClient.get("/customers");
  return data;
}

export async function createCustomer(payload) {
  const { data } = await apiClient.post("/customers", payload);
  return data;
}
