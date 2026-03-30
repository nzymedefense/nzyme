import React from "react";

export default function AppliedFilterList(props) {

  const filters = props.filters;
  const onFilterRemoved = props.onFilterRemoved;
  const hideHeadline = props.hideHeadline;
  const appliedMonitor = props.appliedMonitor;

  if (!filters || Object.keys(filters).length === 0) {
    return (
      <div className="applied-filters-empty mt-3">
        <span className="applied-filters-empty-text">No filters defined.</span>
      </div>
    );
  }

  const filterGroups = Object.keys(filters);

  return (
    <>
      {hideHeadline ? null : <h5 className="applied-filters-headline mt-3">Active Filters
        { appliedMonitor ? <small> &#40;Monitor: {appliedMonitor.name}	&#41;</small> : null}</h5> }

      <div className="applied-filters mt-1">
        {filterGroups.map((filterGroup, i) => {
          return (
            <React.Fragment key={i}>
              <div className="applied-filters-filtergroup">
                {filters[filterGroup].map((filter, x) => {
                  return (
                    <React.Fragment key={i + "-" + x}>
                      {x > 0 && (
                        <span className="applied-filter-connector applied-filter-connector--or">OR</span>
                      )}
                      <span className="applied-filter-chip">
                        <span className="applied-filter-name">{filter.name}</span>
                        <span title={filter.operator} className="applied-filter-operator">{filter.sign}</span>
                        <span className="applied-filter-value">{filter.value}</span>
                        {onFilterRemoved ? (
                          <a className="applied-filter-remove" onClick={function(e) { onFilterRemoved(e, filter); }}>
                            <i className="fa-solid fa-xmark" />
                          </a>
                        ) : null}
                      </span>
                    </React.Fragment>
                  );
                })}
              </div>
              {i < filterGroups.length - 1 && (
                <div className="applied-filter-connector applied-filter-connector--and">AND</div>
              )}
            </React.Fragment>
          );
        })}
      </div>
    </>
  );
}