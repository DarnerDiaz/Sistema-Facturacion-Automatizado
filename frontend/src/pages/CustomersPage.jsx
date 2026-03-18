import { useEffect, useState } from "react";
import AppShell from "../components/AppShell";
import { createCustomer, getCustomers } from "../api/customerApi";

export default function CustomersPage() {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    name: "",
    taxId: "",
    email: "",
    address: "",
    phone: "",
  });

  async function loadCustomers() {
    setLoading(true);
    try {
      setCustomers(await getCustomers());
      setError("");
    } catch (err) {
      setError(err?.response?.data?.message || "No se pudo cargar clientes");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadCustomers();
  }, []);

  async function handleSubmit(event) {
    event.preventDefault();
    try {
      await createCustomer(form);
      setForm({ name: "", taxId: "", email: "", address: "", phone: "" });
      await loadCustomers();
    } catch (err) {
      setError(err?.response?.data?.message || "No se pudo crear cliente");
    }
  }

  return (
    <AppShell title="Clientes">
      <div className="two-columns">
        <form className="panel" onSubmit={handleSubmit}>
          <h3>Nuevo cliente</h3>
          <input placeholder="Nombre" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <input placeholder="RUC/DNI" value={form.taxId} onChange={(e) => setForm({ ...form, taxId: e.target.value })} required />
          <input placeholder="Correo" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} type="email" />
          <input placeholder="Dirección" value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} />
          <input placeholder="Teléfono" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          <button type="submit">Guardar cliente</button>
          {error && <div className="error-box">{error}</div>}
        </form>

        <section className="panel">
          <h3>Listado</h3>
          {loading ? (
            <p>Cargando...</p>
          ) : (
            <ul className="simple-list">
              {customers.map((customer) => (
                <li key={customer.id}>
                  <strong>{customer.name}</strong>
                  <span>{customer.taxId}</span>
                  <span>{customer.email || "Sin correo"}</span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </AppShell>
  );
}
