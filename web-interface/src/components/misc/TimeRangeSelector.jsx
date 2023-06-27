import React, {useState} from "react";
import TimeRangeSelectorOptions from "./TimeRangeSelectorOptions";

function TimeRangeSelector(props) {

  const selectedTimeRange = props.setSelectedTimeRange;
  const setSelectedTimeRange = props.setSelectedTimeRange; // start, end

  const [show, setShow] = useState(false);

  const onTrigger = function(range) {
    setShow(!show);
  }

  return (
    <div className="timerange-selector">
      <button type="button" className="btn btn-sm btn-secondary mb-2" onClick={onTrigger}>
        Last 15 minutes <i className="fa-solid fa-caret-down"></i>
      </button>

      <TimeRangeSelectorOptions show={show} />
    </div>
  )

}

export default TimeRangeSelector;