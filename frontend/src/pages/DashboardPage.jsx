import AppShell from "../components/AppShell";

export default function DashboardPage() {
  return (
    <AppShell title="Dashboard">
      <div className="card-grid">
        <article className="card">
          <h3>Emision rápida</h3>
          <p>Crea borradores y emite facturas con un clic.</p>
        </article>
        <article className="card">
          <h3>IGV 18%</h3>
          <p>Cálculo automático de subtotal, impuesto y total.</p>
        </article>
        <article className="card">
          <h3>Historial</h3>
          <p>Seguimiento de estado y envíos por correo.</p>
        </article>
      </div>
    </AppShell>
  );
}
