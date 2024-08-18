import React from 'react'

import numeral from 'numeral'
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import SingleValueCardLoading from "./SingleValueCardLoading";

function NumberCard (props) {

  const title = props.title;
  const value = props.value;
  const numberFormat = props.numberFormat;
  const helpLink = props.helpLink;

  const timeRange = props.timeRange;
  const setTimeRange = props.setTimeRange;
  const fixedAppliedTimeRange = props.fixedAppliedTimeRange;

  if (value === null || value === undefined) {
    return <SingleValueCardLoading title={title}
                                   slim={true}
                                   timeRange={timeRange}
                                   setTimeRange={setTimeRange}
                                   fixedAppliedTimeRange={fixedAppliedTimeRange} />
  }

  return (
      <div className="card">
        <div className="card-body card-number">
          <CardTitleWithControls title={title}
                                 helpLink={helpLink}
                                 slim={true}
                                 timeRange={timeRange}
                                 setTimeRange={setTimeRange}
                                 fixedAppliedTimeRange={fixedAppliedTimeRange}/>

          <div className="value">
            {numeral(value).format(numberFormat)}
          </div>
        </div>
        </div>
  )
}

export default NumberCard
