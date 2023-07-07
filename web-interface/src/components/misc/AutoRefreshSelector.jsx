import React from "react";
import moment from "moment/moment";

function AutoRefreshSelector(props) {

  const isAutoRefresh = props.isAutoRefresh;
  const setIsAutoRefresh = props.setIsAutoRefresh;
  const lastUpdated = props.lastUpdated;

  const onChange = function() {
    setIsAutoRefresh(!isAutoRefresh);
  }

  return (
      <div className="form-check form-switch mb-1">
        <input className="form-check-input"
               type="checkbox"
               role="switch"
               id="autoRefresh"
               onChange={onChange}
               checked={isAutoRefresh} />
        <label className="form-check-label"
               htmlFor="autoRefresh"
               title={"Last Updated: " + moment(lastUpdated).format()} >
          Automatically Refresh
        </label>
      </div>
  )

}

export default AutoRefreshSelector;