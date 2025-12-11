import { Suspense } from "react";

export default function SearchLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <Suspense fallback={<p className="text-center text-gray-400">Loading...</p>}>
      {children}
    </Suspense>
  );
}