import { NavLink } from "react-router-dom";
import {
    LayoutDashboard,
    Users,
    UserPlus,
    Tags,
    BarChart3,
    ChevronRight
} from "lucide-react";
import "../css/Sidebar.css";

const navItems = [
    { label: "Painel", to: "/dashboard", icon: <LayoutDashboard size={18} /> },
    { label: "Clientes", to: "/clients", icon: <Users size={18} /> },
    { label: "Adicionar Cliente", to: "/add-client", icon: <UserPlus size={18} /> },
    { label: "Categorias", to: "/categories", icon: <Tags size={18} /> },
    { label: "Relatórios", to: "/reports", icon: <BarChart3 size={18} /> },
];

const AppSidebar = () => {
    return (
        <aside className="sidebar">
            <div className="sidebar__header">
                <div className="sidebar__logo-icon" />
                <h2 className="sidebar__brand">Lizard</h2>
            </div>

            <nav className="sidebar__nav">
                {navItems.map((item) => (
                    <NavLink
                        key={item.to}
                        to={item.to}
                        className={({ isActive }) =>
                            `sidebar__link ${isActive ? "sidebar__link--active" : ""}`
                        }
                    >
                        <span className="sidebar__link-icon">{item.icon}</span>
                        <span className="sidebar__link-label">{item.label}</span>
                        <ChevronRight className="sidebar__link-arrow" size={14} />
                    </NavLink>
                ))}
            </nav>

            <div className="sidebar__footer">
                <div className="sidebar__user-badge">
                    <div className="user-avatar">SD</div>
                    <div className="user-info">
                        <span className="user-name">Premium Mode</span>
                        <span className="user-status">Online</span>
                    </div>
                </div>
            </div>
        </aside>
    );
};

export default AppSidebar;