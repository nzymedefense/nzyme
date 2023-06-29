import React from "react";

function AutoRefreshSelector(props) {

  const isAutoRefresh = props.isAutoRefresh;
  const setIsAutoRefresh = props.setIsAutoRefresh;

  const onChange = function() {
    setIsAutoRefresh(!isAutoRefresh);
  }

  return (
      <div className="form-check form-switch float-end">
        <input className="form-check-input"
               type="checkbox"
               role="switch"
               id="autoRefresh"
               onChange={onChange}
               checked={isAutoRefresh} />
        <label className="form-check-label" htmlFor="autoRefresh">
          Automatically Refresh
        </label>
      </div>
  )

}

export default AutoRefreshSelector;