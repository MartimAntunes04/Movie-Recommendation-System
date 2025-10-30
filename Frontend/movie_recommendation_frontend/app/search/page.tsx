import { Suspense } from "react";
import SearchContent from "./SearchContent";


export default function Search(){

  return (
    <Suspense fallback={<p className="text-center text-gray-400">Loading...</p>}>
      <SearchContent />
    </Suspense>
  );
}