import { Outlet } from "react-router-dom";
import AppSidebar from "@/components/AppSidebar";

const AppLayout = () => {
  return (
    <div className="flex min-h-screen w-full">
      <AppSidebar />
      <main className="flex-1 min-h-screen overflow-auto">
        <Outlet />
      </main>
    </div>
  );
};

export default AppLayout;
