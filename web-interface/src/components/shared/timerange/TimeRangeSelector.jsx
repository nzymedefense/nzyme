import React from "react";
import {Named, Relative} from "./TimeRange";

function TimeRangeSelector(props) {

  const setTimeRange = props.setTimeRange;

  const option = (range, name) => {
    return <a href="#" onClick={(e) => { e.preventDefault(); setTimeRange(range) }}>{name}</a>
  }

  return (
    <div className="timerange-selector">

      <div className="accordion" id="timerange-selector-accordion">
        <div className="accordion-item">
          <h2 className="accordion-header" id="trs-presets-heading">
            <button className="accordion-button" type="button" data-bs-toggle="collapse"
                    data-bs-target="#trs-presets-collapse" aria-expanded="false" aria-controls="trs-presets-collapse">
              <i className="accordion-icon fa-regular fa-clock"></i> Presets
            </button>
          </h2>
          <div id="trs-presets-collapse" className="accordion-collapse collapse show" aria-labelledby="trs-presets-heading"
               data-bs-parent="#timerange-selector-accordion">
            <div className="accordion-body">
              <div className="row">
                <div className="col-md-4">
                  <h3>Relative</h3>
                  <ul>
                    <li>{option(Relative(1),     "Last 1 Minute")}</li>
                    <li>{option(Relative(5),     "Last 5 Minutes")}</li>
                    <li>{option(Relative(15),    "Last 15 Minutes")}</li>
                    <li>{option(Relative(30),    "Last 30 Minutes")}</li>
                    <li>{option(Relative(60),    "Last 60 Minutes")}</li>
                    <li>{option(Relative(240),   "Last 4 Hours")}</li>
                    <li>{option(Relative(1440),  "Last 24 Hours")}</li>
                    <li>{option(Relative(2880),  "Last 2 Days")}</li>
                    <li>{option(Relative(10080), "Last 7 Days")}</li>
                  </ul>
                </div>
                <div className="col-md-4">
                  <h3>Quick Selectors</h3>
                  <ul>
                    <li>{option(Named("today"),         "Today")}</li>
                    <li>{option(Named("yesterday"),     "Yesterday")}</li>
                    <li>{option(Named("week_to_date"),  "Week to date")}</li>
                    <li>{option(Named("month_to_date"), "Month to date")}</li>
                  </ul>
                </div>
                <div className="col-md-4">
                  <h3>Other</h3>
                  <ul>
                    <li>{option(Named("all_time"), "All Time")}</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="accordion-item">
          <h2 className="accordion-header" id="trs-range-heading">
            <button className="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                    data-bs-target="#trs-range-collapse" aria-expanded="false" aria-controls="trs-range-collapse">
              <i className="accordion-icon fa-regular fa-calendar"></i> Time Range
            </button>
          </h2>
          <div id="trs-range-collapse" className="accordion-collapse collapse" aria-labelledby="trs-range-heading"
               data-bs-parent="#timerange-selector-accordion">
            <div className="accordion-body">
              <strong>WORK IN PROGRESS.</strong>
            </div>
          </div>
        </div>
      </div>

    </div>
  )

}

export default TimeRangeSelector;