import { useEffect } from "react";

const usePageTitle = (title) => {
  useEffect(() => {
    document.title = title ? `${title} – Nzyme` : "Nzyme";

    return () => {
      document.title = "Nzyme"; // reset when component unmounts
    };
  }, [title]);
};

export default usePageTitle;