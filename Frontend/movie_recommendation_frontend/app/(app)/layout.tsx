import { Navbar } from "@/components/Navbar";
import AuthGuard from "@/components/AuthGuard";

export default function AppLayout({
        children,
      }: {
        children: React.ReactNode;
      }) {
        return (
          
          <div className="min-h-screen bg-linear-to-br from-slate-50 to-slate-100 dark:from-slate-900 dark:to-slate-800">
            <Navbar />	
            <AuthGuard>{children}</AuthGuard>
          </div>
        );
      }