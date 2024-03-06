import React, {useState} from "react";
import TimeRangeSelector from "../shared/timerange/TimeRangeSelector";

function CardTitleWithSettings(props) {

  const title = props.title;

  // Required for time range selector.
  const setTimeRange = props.setTimeRange;
  const timeRange = props.timeRange;

  const [timeRangeDialogOpened, setTimeRangeDialogOpened] = useState(true);

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
      <React.Fragment>
        <div className="row">
          <div className="col-11">
            <h3>{title}</h3>
          </div>

          <div className="col-1 text-end">{timeRangeButton()}</div>

          {timeRangeDialog()}
        </div>
      </React.Fragment>
  )

}

export default CardTitleWithSettings;