import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";

export default function AssetDetailsDnsTransactions(props) {

  const asset = props.asset;

  const [timerange, setTimerange] = React.useState(Presets.RELATIVE_HOURS_24);

  const [revision, setRevision] = React.useState(new Date());

  const onRefresh = () => {
    setRevision(new Date());
  }

  if (!asset) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row mt-3">
          <div className="col-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="DNS Transactions"
                                       timeRange={timerange}
                                       setTimeRange={setTimerange}
                                       refreshAction={onRefresh} />

              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}