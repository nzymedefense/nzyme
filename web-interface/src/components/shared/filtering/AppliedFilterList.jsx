import React from "react";

export default function AppliedFilterList(props) {

  const filters = props.filters;
  const onFilterRemoved = props.onFilterRemoved;
  const hideHeadline = props.hideHeadline;

  if (!filters || Object.keys(filters).length === 0) {
    return <ul className="applied-filters mt-3"><li>No filters defined.</li></ul>
  }

  return (
      <React.Fragment>
        { hideHeadline ? null : <h5 className="mt-3">Active Filters</h5> }

        <ul className="applied-filters mt-1">
          {Object.keys(filters).map((filterGroup, i) => {
            return (
                <ul className="applied-filters-filtergroup" key={i}>
                  <li>
                    {filters[filterGroup].map((filter, x) => {
                      return (
                          <React.Fragment key={i + "-" + x}>
                            <span className="applied-filter-name">{filter.name}</span>{' '}
                            <span title={filter.operator} className="applied-filter-operator">{filter.sign}</span>{' '}
                            <span className="applied-filter-value">{filter.value}</span>{' '}
                            { onFilterRemoved ?
                            <a className="applied-filter-remove" onClick={(e) => onFilterRemoved(e, filter)}>
                              <i className="fa-solid fa-xmark" />
                            </a> : null }{' '}
                            {filters[filterGroup].length === x + 1 ? null :
                                <span className="applied-filter-connector">OR</span>}
                            {' '}
                          </React.Fragment>
                      )
                    })}
                  </li>
                  {Object.keys(filters).length === i + 1 ? null :
                      <li><span className="applied-filter-connector">AND</span></li>
                  }
                </ul>
            )
          })}
        </ul>
      </React.Fragment>
  )

}