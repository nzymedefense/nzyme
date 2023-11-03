import React from "react";
import LimitSelector from "../LimitSelector";
import {MODE_BAR_CHART, MODE_EXPORT, MODE_TABLE} from "./HistogramModes";

function HistogramActions(props) {

  const limit = props.limit;
  const setLimit = props.setLimit;
  const mode = props.mode;
  const setMode = props.setMode;

  return (
      <React.Fragment>
        <LimitSelector limit={limit} setLimit={setLimit} style={{display: "block", float: "left", marginRight: 2}} />

        <a href="#" className={"btn btn-sm " + (mode === MODE_TABLE ? "btn-secondary btn-disabled" : "btn-outline-secondary")}
           style={{marginRight: 2}}
           title="Switch to table view"
           onClick={(e) => {
             e.preventDefault();
             setMode(MODE_TABLE);
           }}>
          <i className="fa-solid fa-table-list"></i>
        </a>

        <a href="#" className={"btn btn-sm " + (mode === MODE_BAR_CHART ? "btn-secondary btn-disabled" : "btn-outline-secondary")}
           style={{marginRight: 2}}
           title="Switch to bar chart view"
           onClick={(e) => {
             e.preventDefault();
             setMode(MODE_BAR_CHART);
           }}>
          <i className="fa-solid fa-chart-bar"></i>
        </a>

        <a href="#" className={"btn btn-sm " + (mode === MODE_EXPORT ? "btn-secondary btn-disabled" : "btn-outline-secondary")}
           title="Switch to data export view"
           onClick={(e) => {
             e.preventDefault();
             setMode(MODE_EXPORT);
           }}>
          <i className="fa-solid fa-file-arrow-down"></i>
        </a>
      </React.Fragment>
  )

}

export default HistogramActions;