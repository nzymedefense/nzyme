import React from "react";

export default function AppliedFilterList(props) {

  const filters = props.filters;

  if (!filters || filters.length === 0) {
    return <i>No Filters defined.</i>
  }

  return (
      <ul className="applied-filters">
        <li>FILTAS</li>
      </ul>
  )

}