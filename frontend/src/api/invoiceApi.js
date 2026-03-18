import apiClient from "./client";

export async function getInvoices() {
  const { data } = await apiClient.get("/invoices");
  return data;
}

export async function createInvoice(payload) {
  const { data } = await apiClient.post("/invoices", payload);
  return data;
}

export async function validateInvoice(payload) {
  const { data } = await apiClient.post("/invoices/validate", payload);
  return data;
}

export async function emitInvoice(invoiceId, reason) {
  const { data } = await apiClient.post(`/invoices/${invoiceId}/emit`, { reason });
  return data;
}

export async function sendInvoiceEmail(invoiceId) {
  const { data } = await apiClient.post(`/invoices/${invoiceId}/send-email`);
  return data;
}
