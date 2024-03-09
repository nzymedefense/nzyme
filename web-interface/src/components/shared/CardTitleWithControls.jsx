import React, {useState} from "react";
import TimeRangeSelector from "./timerange/TimeRangeSelector";
import AppliedTimeRange from "./timerange/AppliedTimeRange";

function CardTitleWithControls(props) {

  const title = props.title;

  // Optional styling.
  const slim = props.slim;

  // Required for time range selector.
  const setTimeRange = props.setTimeRange;
  const timeRange = props.timeRange;

  // Optional.
  const smallTextParam = props.smallText;
  const fixedAppliedTimeRange = props.fixedAppliedTimeRange;

  const [timeRangeDialogOpened, setTimeRangeDialogOpened] = useState(false);

  const smallText = () => {
    if (!smallTextParam) {
      return null;
    }

    return <small>{smallTextParam}</small>
  }

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

  const appliedTimeRange = () => {
    if (!timeRange && !fixedAppliedTimeRange) {
      return null;
    }

    return (
        <div className="row">
          <div className="col-12">
            <AppliedTimeRange timeRange={timeRange ? timeRange : fixedAppliedTimeRange}/>
          </div>
        </div>
    )
  }

  return (
      <div className="card-title" style={slim ? {marginBottom:0} : {}}>
        <div className="row">
          <div className="col-11">
            <h3>{title} {smallText()}</h3>
          </div>

          <div className="col-1 text-end">{timeRangeButton()}</div>

          {timeRangeDialog()}
        </div>

        {appliedTimeRange()}

        {slim ? null : <hr />}
      </div>
)

}

export default CardTitleWithControls;