import React from "react";

function EventTypesTableFilter(props) {

  const categories = props.categories;
  const setCategories = props.setCategories;

  const handleCategorySelect = function(name) {
    const selection = [...categories];
    if (selection.includes(name)) {
      const idx = selection.indexOf(name);
      selection.splice(idx, true);
    } else {
      selection.push(name);
    }

    setCategories(selection);
  }

  return (
      <React.Fragment>
        <div className="form-check-inline">
          <strong>Category Filter:</strong>
        </div>

        <div className="form-check form-switch form-check-inline">
          <input className="form-check-input"
                 id="filterAuthentication"
                 type="checkbox"
                 role="switch"
                 checked={categories.includes("AUTHENTICATION")}
                 onChange={() => handleCategorySelect("AUTHENTICATION") } />
          <label className="form-check-label" htmlFor="filterAuthentication">
            Authentication
          </label>
        </div>

        <div className="form-check form-switch form-check-inline">
          <input className="form-check-input"
                 id="filterHealthIndicator"
                 type="checkbox"
                 role="switch"
                 checked={categories.includes("HEALTH_INDICATOR")}
                 onChange={() => handleCategorySelect("HEALTH_INDICATOR") } />
          <label className="form-check-label" htmlFor="filterHealthIndicator">
            System Health Indicator
          </label>
        </div>
      </React.Fragment>
  )

}

export default EventTypesTableFilter;