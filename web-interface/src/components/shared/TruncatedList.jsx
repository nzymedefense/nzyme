import React, { useState } from "react";

function TruncatedList({ items, limit = 5 }) {
  const [showAll, setShowAll] = useState(false);

  const visibleItems = showAll ? items : items.slice(0, limit);
  const hasMore = items.length > limit;

  return (
    <div>
      <ul className="mb-0">
        {visibleItems.map((item, i) => (
          <li key={i}>{item}</li>
        ))}
      </ul>
      {hasMore && !showAll && (
        <button className="btn btn-sm btn-outline-secondary mt-3" onClick={() => setShowAll(true)}>
          Show all ({items.length})
        </button>
      )}
    </div>
  );
}

export default TruncatedList;