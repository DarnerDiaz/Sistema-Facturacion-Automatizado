import { useEffect, useState } from "react";
import AppShell from "../components/AppShell";
import { emitInvoice, getInvoices, sendInvoiceEmail } from "../api/invoiceApi";

export default function InvoicesPage() {
  const [invoices, setInvoices] = useState([]);
  const [error, setError] = useState("");

  async function loadInvoices() {
    try {
      const data = await getInvoices();
      setInvoices(data);
      setError("");
    } catch (err) {
      setError(err?.response?.data?.message || "No se pudo cargar facturas");
    }
  }

  useEffect(() => {
    loadInvoices();
  }, []);

  async function handleEmit(id) {
    await emitInvoice(id, "Emision desde UI");
    await loadInvoices();
  }

  async function handleSend(id) {
    await sendInvoiceEmail(id);
    await loadInvoices();
  }

  return (
    <AppShell title="Facturas">
      {error && <div className="error-box">{error}</div>}
      <div className="panel">
        <table className="table">
          <thead>
            <tr>
              <th>Número</th>
              <th>Cliente</th>
              <th>Total</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {invoices.map((invoice) => (
              <tr key={invoice.id}>
                <td>{invoice.invoiceNumber}</td>
                <td>{invoice.customerName}</td>
                <td>PEN {invoice.total}</td>
                <td>{invoice.status}</td>
                <td className="actions">
                  {invoice.status === "DRAFT" && (
                    <button type="button" onClick={() => handleEmit(invoice.id)}>
                      Emitir
                    </button>
                  )}
                  {(invoice.status === "ISSUED" || invoice.status === "SENT") && (
                    <button type="button" onClick={() => handleSend(invoice.id)}>
                      Enviar correo
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </AppShell>
  );
}
