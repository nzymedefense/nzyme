import React from 'react'
import moment from 'moment'
import numeral from 'numeral'

function sumValues (sizes) {
  const vals = Object.values(sizes)

  let totalSize = 0
  vals.forEach((size) => {
    totalSize += size
  })

  return totalSize
}

function ServiceSummaryOverview (props) {
  if (!props.summary.is_available) {
    return (
            <div className="alert alert-warning">
                Service summary metrics have not been calculated yet. An nzyme leader node is calculating the
                service summary metrics regularly and immediately after the system starts.
            </div>
    )
  }

  return (
        <div className="row">
            <div className="col-md-6">
                <dl className="retro-servicesummary-topmetrics">
                    <dt>Size:</dt>
                    <dd>{numeral(sumValues(props.summary.total_sizes)).format('0b')}</dd>

                    <dt>Entries:</dt>
                    <dd>{numeral(sumValues(props.summary.entry_counts)).format('0,0')}</dd>

                    <dt>Segments:</dt>
                    <dd>{numeral(sumValues(props.summary.segment_counts)).format('0,0')}</dd>
                </dl>
            </div>

            <div className="col-md-6">
                <dl className="retro-servicesummary-topmetrics">
                    <dt>Last calculated:</dt>
                    <dd title={moment(props.summary.calculated_at).format()}>
                        {moment(props.summary.calculated_at).fromNow()}
                    </dd>

                    <dt>Calculation took:</dt>
                    <dd title={props.summary.took_ms + 'ms'}>
                        {props.summary.took_ms > 1000 ? numeral(props.summary.took_ms / 1000).format("'00:00:00'") : props.summary.took_ms + ' ms'}
                    </dd>
                </dl>
            </div>
        </div>
  )
}

export default ServiceSummaryOverview
