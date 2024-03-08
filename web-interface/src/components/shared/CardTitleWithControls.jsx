import React, {useState} from "react";
import TimeRangeSelector from "./timerange/TimeRangeSelector";
import AppliedTimeRange from "./timerange/AppliedTimeRange";

function CardTitleWithControls(props) {

  const title = props.title;

  // Required for time range selector.
  const setTimeRange = props.setTimeRange;
  const timeRange = props.timeRange;

  const [timeRangeDialogOpened, setTimeRangeDialogOpened] = useState(false);

  const timeRangeButton = () => {
    if (setTimeRange && timeRange) {
      return (
          <button className="btn card-title-option"
                  title="Toggle Time Range Selector"
                  onClick={(e) => {
                    e.preventDefault();
                    setTimeRangeDialogOpened(!timeRangeDialogOpened);
                  }}>
            <i className="fa-regular fa-clock"></i>
          </button>
      )
    }
  }

  const timeRangeDialog = () => {
    if (!timeRangeDialogOpened) {
      return null;
    }

    return (
        <div className="mb-3">
          <TimeRangeSelector timeRange={timeRange} setTimeRange={setTimeRange} />
        </div>
    );
  }

  return (
      <div className="card-title">
        <div className="row">
          <div className="col-11">
            <h3>{title}</h3>
          </div>

          <div className="col-1 text-end">{timeRangeButton()}</div>

          {timeRangeDialog()}
        </div>

        <div className="row">
          <div className="col-12">
            <AppliedTimeRange timeRange={props.timeRange}/>
          </div>
        </div>

        <hr />
      </div>
)

}

export default CardTitleWithControls;