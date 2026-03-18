import { useEffect, useMemo, useState } from "react";
import AppShell from "../components/AppShell";
import { getCustomers } from "../api/customerApi";
import { createInvoice, validateInvoice } from "../api/invoiceApi";

const TAX_PERCENTAGE = 18;

export default function InvoiceCreatePage() {
  const [customers, setCustomers] = useState([]);
  const [form, setForm] = useState({
    customerId: "",
    issueDate: new Date().toISOString().slice(0, 10),
    dueDate: "",
    notes: "",
    items: [{ description: "", quantity: 1, unitPrice: 0, taxPercentage: TAX_PERCENTAGE }],
  });
  const [validationErrors, setValidationErrors] = useState([]);
  const [submitMessage, setSubmitMessage] = useState("");

  useEffect(() => {
    getCustomers().then(setCustomers).catch(() => setCustomers([]));
  }, []);

  const totals = useMemo(() => {
    const subtotal = form.items.reduce(
      (acc, item) => acc + Number(item.quantity || 0) * Number(item.unitPrice || 0),
      0
    );
    const tax = form.items.reduce((acc, item) => {
      const base = Number(item.quantity || 0) * Number(item.unitPrice || 0);
      return acc + (base * Number(item.taxPercentage || 0)) / 100;
    }, 0);
    return {
      subtotal: subtotal.toFixed(2),
      tax: tax.toFixed(2),
      total: (subtotal + tax).toFixed(2),
    };
  }, [form.items]);

  function updateItem(index, field, value) {
    const updated = [...form.items];
    updated[index] = { ...updated[index], [field]: value };
    setForm({ ...form, items: updated });
  }

  async function handleValidate() {
    try {
      const payload = {
        ...form,
        items: form.items.map((item) => ({
          description: item.description,
          quantity: Number(item.quantity),
          unitPrice: Number(item.unitPrice),
          taxPercentage: Number(item.taxPercentage),
        })),
      };
      const result = await validateInvoice(payload);
      setValidationErrors(result.errors || []);
    } catch (err) {
      setValidationErrors([err?.response?.data?.message || "Error de validación"]);
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitMessage("");
    const payload = {
      ...form,
      items: form.items.map((item) => ({
        description: item.description,
        quantity: Number(item.quantity),
        unitPrice: Number(item.unitPrice),
        taxPercentage: Number(item.taxPercentage),
      })),
    };

    try {
      const saved = await createInvoice(payload);
      setSubmitMessage(`Factura ${saved.invoiceNumber} creada en DRAFT`);
    } catch (err) {
      setSubmitMessage(err?.response?.data?.message || "No se pudo crear factura");
    }
  }

  return (
    <AppShell title="Nueva Factura">
      <form className="panel" onSubmit={handleSubmit}>
        <div className="invoice-grid">
          <select
            value={form.customerId}
            onChange={(e) => setForm({ ...form, customerId: e.target.value })}
            required
          >
            <option value="">Selecciona cliente</option>
            {customers.map((customer) => (
              <option key={customer.id} value={customer.id}>
                {customer.name}
              </option>
            ))}
          </select>
          <input type="date" value={form.issueDate} onChange={(e) => setForm({ ...form, issueDate: e.target.value })} required />
          <input type="date" value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
        </div>

        {form.items.map((item, index) => (
          <div className="item-row" key={index}>
            <input
              placeholder="Descripción"
              value={item.description}
              onChange={(e) => updateItem(index, "description", e.target.value)}
              required
            />
            <input
              type="number"
              min="0.01"
              step="0.01"
              value={item.quantity}
              onChange={(e) => updateItem(index, "quantity", e.target.value)}
              required
            />
            <input
              type="number"
              min="0.01"
              step="0.01"
              value={item.unitPrice}
              onChange={(e) => updateItem(index, "unitPrice", e.target.value)}
              required
            />
            <input
              type="number"
              min="0"
              step="0.01"
              value={item.taxPercentage}
              onChange={(e) => updateItem(index, "taxPercentage", e.target.value)}
              required
            />
          </div>
        ))}

        <button
          type="button"
          onClick={() =>
            setForm({
              ...form,
              items: [...form.items, { description: "", quantity: 1, unitPrice: 0, taxPercentage: TAX_PERCENTAGE }],
            })
          }
        >
          Agregar item
        </button>

        <textarea
          placeholder="Notas"
          value={form.notes}
          onChange={(e) => setForm({ ...form, notes: e.target.value })}
        />

        <div className="totals-box">
          <div>Subtotal: PEN {totals.subtotal}</div>
          <div>IGV: PEN {totals.tax}</div>
          <strong>Total: PEN {totals.total}</strong>
        </div>

        <div className="button-row">
          <button type="button" onClick={handleValidate}>Validar</button>
          <button type="submit">Guardar DRAFT</button>
        </div>

        {validationErrors.length > 0 && (
          <ul className="error-list">
            {validationErrors.map((error, index) => (
              <li key={index}>{error}</li>
            ))}
          </ul>
        )}

        {submitMessage && <p>{submitMessage}</p>}
      </form>
    </AppShell>
  );
}
