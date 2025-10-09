import React from "react";
import numeral from "numeral";
import HistogramActions from "./HistogramActions";

function HistogramHeader(props) {

  const count = props.count
  const limit = props.limit;
  const setLimit = props.setLimit;
  const mode = props.mode;
  const setMode = props.setMode;
  const showBarChart = props.showBarChart;

  return (
      <div className="mb-1 mt-2">
        <span style={{position: "relative", top: 10}}>
          Total: {numeral(count).format("0,0")}
        </span>

        <span className="float-end">
              <HistogramActions
                  limit={limit}
                  setLimit={setLimit}
                  mode={mode}
                  showBarChart={showBarChart}
                  setMode={setMode} />
            </span>
      </div>
  )

}

export default HistogramHeader;