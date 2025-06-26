import React, {useContext, useEffect, useState} from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";
import DNSTransactionCountChart from "../dns/logs/widgets/DNSTransactionCountChart";
import {TapContext} from "../../../App";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import DNSTransactionsTable from "../dns/logs/DNSTransactionsTable";
import ApiRoutes from "../../../util/ApiRoutes";

export default function AssetDetailsDnsTransactions(props) {

  const tapContext = useContext(TapContext);

  const asset = props.asset;

  const FILTERS = {
    "client_mac": [{
      field: "client_mac",
      operator: "equals",
      value: asset.mac.address,
    }]
  };

  const [timerange, setTimerange] = useState(Presets.RELATIVE_HOURS_24);
  const [revision, setRevision] = useState(new Date());

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  const onRefresh = () => {
    setRevision(new Date());
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
                                       internalLink={ApiRoutes.ETHERNET.DNS.TRANSACTION_LOGS + "?filters=" + JSON.stringify(FILTERS)}
                                       refreshAction={onRefresh} />


                <DNSTransactionCountChart timeRange={timerange}
                                          filters={FILTERS}
                                          revision={revision} />

                <DNSTransactionsTable timeRange={timerange}
                                      filters={FILTERS}
                                      perPage={25}
                                      revision={revision}/>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}