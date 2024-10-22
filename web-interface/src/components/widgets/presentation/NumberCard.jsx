import React from 'react'

import numeral from 'numeral'
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import SingleValueCardLoading from "./SingleValueCardLoading";

function NumberCard (props) {

  const title = props.title;
  const value = props.value;
  const numberFormat = props.numberFormat;
  const helpLink = props.helpLink;
  const internalLink = props.internalLink;
  const className = props.className;
  const fullHeight = props.fullHeight;

  const timeRange = props.timeRange;
  const setTimeRange = props.setTimeRange;
  const fixedAppliedTimeRange = props.fixedAppliedTimeRange;

  const format = () => {
    return numeral(value).format(numberFormat)
  }

  if (value === null || value === undefined) {
    return <SingleValueCardLoading title={title}
                                   slim={true}
                                   timeRange={timeRange}
                                   setTimeRange={setTimeRange}
                                   fixedAppliedTimeRange={fixedAppliedTimeRange} />
  }

  return (
      <div className={"card " + (fullHeight ? " card-full-height" : "")}>
        <div className={"card-body card-number " + (className ? className : "")}>
          <CardTitleWithControls title={title}
                                 helpLink={helpLink}
                                 internalLink={internalLink}
                                 slim={true}
                                 timeRange={timeRange}
                                 setTimeRange={setTimeRange}
                                 fixedAppliedTimeRange={fixedAppliedTimeRange}/>

          <div className="value">{format()}</div>
        </div>
        </div>
  )
}

export default NumberCard
