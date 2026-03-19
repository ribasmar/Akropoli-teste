import { ReactNode } from "react";

interface DashboardCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  statusColor?: "growing" | "at-risk" | "stable" | "default";
}

const colorMap = {
  growing: "border-l-status-growing",
  "at-risk": "border-l-status-at-risk",
  stable: "border-l-status-stable",
  default: "border-l-border",
};

const DashboardCard = ({ title, value, subtitle, statusColor = "default" }: DashboardCardProps) => {
  return (
    <div className={`bg-card rounded-lg border border-border border-l-4 ${colorMap[statusColor]} p-6`}>
      <p className="text-sm text-muted-foreground font-heading font-medium uppercase tracking-wide">{title}</p>
      <p className="text-3xl font-heading font-semibold text-foreground mt-2">{value}</p>
      {subtitle && <p className="text-sm text-muted-foreground mt-1">{subtitle}</p>}
    </div>
  );
};

export default DashboardCard;
