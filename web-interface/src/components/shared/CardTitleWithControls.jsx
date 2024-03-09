import React, {useState} from "react";
import TimeRangeSelector from "./timerange/TimeRangeSelector";
import AppliedTimeRange from "./timerange/AppliedTimeRange";

function CardTitleWithControls(props) {

  const title = props.title;

  // Optional styling.
  const slim = props.slim;

  // Required if using time range selector.
  const setTimeRange = props.setTimeRange;
  const timeRange = props.timeRange;

  // Required if using help link.
  const helpLink = props.helpLink;

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

  const helpLinkButton = () => {
    if (!helpLink) {
      return null;
    }

    return (
        <a href={helpLink} className="card-title-option" title="Help" target="_blank">
          <i className="fa-solid fa-question"></i>
        </a>
    )
  }

  const timeRangeDialog = () => {
    if (!timeRangeDialogOpened) {
      return null;
    }

    return (
        <div className="mb-3">
          <TimeRangeSelector timeRange={timeRange} setTimeRange={setTimeRange}/>
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
          <div className="col-10">
            <h3>{title} {smallText()}</h3>
          </div>

          <div className="col-2 text-end">
            {timeRangeButton()}
            {helpLinkButton()}
          </div>

          {timeRangeDialog()}
        </div>

        {appliedTimeRange()}

        {slim ? null : <hr />}
      </div>
)

}

export default CardTitleWithControls;