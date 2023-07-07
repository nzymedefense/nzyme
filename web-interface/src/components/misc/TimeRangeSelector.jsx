import React, {useState} from "react";
import TimeRangeSelectorOptions from "./TimeRangeSelectorOptions";

function TimeRangeSelector(props) {

  const disabled = true;

  const [show, setShow] = useState(false);

  const onTrigger = function(range) {
    setShow(!show);
  }

  return (
      <React.Fragment>
        <button type="button" className="btn btn-outline-secondary" onClick={onTrigger} disabled={disabled}>
          { disabled ? "No Time Range Required" : <span>Last 15 minutes <i className="fa-solid fa-caret-down"></i></span> }
        </button>

        <TimeRangeSelectorOptions show={show} />
      </React.Fragment>
  )

}

export default TimeRangeSelector;