import React from 'react'

import numeral from 'numeral'
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import SingleValueCardLoading from "./SingleValueCardLoading";

function ByteSizeCard (props) {

  const title = props.title;
  const value = props.value;
  const fixedAppliedTimeRange = props.fixedAppliedTimeRange;

  if (value === null || value === undefined) {
    return <SingleValueCardLoading title={title} fixedAppliedTimeRange={fixedAppliedTimeRange} />
  }

  return (
        <div className="card">
            <div className="card-body card-number">
              <CardTitleWithControls title={title}
                                     slim={true}
                                     fixedAppliedTimeRange={fixedAppliedTimeRange} />

                <div className="value">
                    {numeral(value).format('0.00b')}
                </div>
            </div>
        </div>
  )
}

export default ByteSizeCard
