import React from "react";

function EventsTableFilters(props) {

  const filters = props.filters;
  const setFilters = props.setFilters;

  const handleSelect = function(name) {
    const selection = [...filters];
    if (selection.includes(name)) {
      const idx = selection.indexOf(name);
      selection.splice(idx, true);
    } else {
      selection.push(name);
    }

    setFilters(selection);
  }

  return (
      <React.Fragment>
        <div className="form-check-inline">
          <strong>Type Filter:</strong>
        </div>

        <div className="form-check form-switch form-check-inline">
          <input className="form-check-input"
                 id="filterSystem"
                 type="checkbox"
                 role="switch"
                 checked={filters.includes("SYSTEM")}
                 onChange={() => handleSelect("SYSTEM") } />
          <label className="form-check-label" htmlFor="filterSystem">
            System
          </label>
        </div>

        <div className="form-check form-switch form-check-inline">
          <input className="form-check-input"
                 id="filterDetection"
                 type="checkbox"
                 role="switch"
                 checked={filters.includes("DETECTION")}
                 onChange={() => handleSelect("DETECTION") } />
          <label className="form-check-label" htmlFor="filterDetection">
            Detection
          </label>
        </div>
      </React.Fragment>
  )

}

export default EventsTableFilters;