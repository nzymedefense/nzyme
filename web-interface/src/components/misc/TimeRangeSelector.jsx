import React, {useState} from "react";
import TimeRangeSelectorOptions from "./TimeRangeSelectorOptions";

function TimeRangeSelector(props) {

  const disabled = true;

  const [show, setShow] = useState(false);

  const onTrigger = function(range) {
    setShow(!show);
  }

  if (disabled) {
    return (
        <div className="dropdown" title="No time range selection required on this page">
          <button className="btn btn-outline-secondary" type="button" disabled={true}>
            No Time Range Required
          </button>
        </div>
    )
  }

  return (
      <React.Fragment>
        <button type="button" className="btn btn-outline-secondary" onClick={onTrigger} disabled={disabled}>
          <span>Last 15 minutes <i className="fa-solid fa-caret-down"></i></span>
        </button>

        <TimeRangeSelectorOptions show={show} />
      </React.Fragment>
  )

}

export default TimeRangeSelector;