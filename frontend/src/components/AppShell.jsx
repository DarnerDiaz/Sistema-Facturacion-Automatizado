import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function AppShell({ title, children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const navItems = [
    { to: "/", label: "Dashboard" },
    { to: "/customers", label: "Clientes" },
    { to: "/invoices", label: "Facturas" },
    { to: "/invoices/new", label: "Nueva Factura" },
  ];

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h1>FacturaFlow</h1>
        <nav>
          {navItems.map((item) => (
            <Link
              key={item.to}
              to={item.to}
              className={location.pathname === item.to ? "active" : ""}
            >
              {item.label}
            </Link>
          ))}
        </nav>
        <button type="button" onClick={handleLogout} className="logout-btn">
          Cerrar sesión
        </button>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <h2>{title}</h2>
          <p>{user?.fullName || "Usuario"}</p>
        </header>
        <section>{children}</section>
      </main>
    </div>
  );
}
