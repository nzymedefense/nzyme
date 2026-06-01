import React from "react";

export default function TimelineFilters({filters, onFilterChange}) {
  return (
    <div className="mt-3">
      <h4>Event Types</h4>
      {filters.map((filter) => {
        const id = `timeline-filter-${filter.value}`;
        return (
          <div className="form-check" key={filter.value}>
            <input
              className="form-check-input"
              type="checkbox"
              id={id}
              name={filter.value}
              defaultChecked={filter.include}
              onChange={(e) => onFilterChange(filter.value, e.target.checked)}
            />
            <label className="form-check-label" htmlFor={id}>
              {filter.name}
            </label>
          </div>
        );
      })}
    </div>
  );
}