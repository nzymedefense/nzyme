import React, {createContext, useContext, useEffect, useState} from "react";
import DiscoHistogram from "./DiscoHistogram";
import DiscoSendersTable from "./DiscoSendersTable";
import DiscoReceiversTable from "./DiscoReceiversTable";
import DiscoPairsTable from "./DiscoPairsTable";
import MonitoredNetworkSelector from "../../shared/MonitoredNetworkSelector";
import {useLocation} from "react-router-dom";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import {TapContext} from "../../../App";

export const MonitoredNetworkContext = createContext(null);

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}

function DiscoPage() {

  const tapContext = useContext(TapContext);

  let urlQuery = useQuery();

  const [monitoredNetwork, setMonitoredNetwork] = useState(
      urlQuery.get("monitored-network-id") ? urlQuery.get("monitored-network-id") : ""
  );

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);
  
  return (
      <React.Fragment>
        <MonitoredNetworkContext.Provider value={{network: monitoredNetwork, setNetwork: setMonitoredNetwork}} >
          <div className="row">
            <div className="col-md-3">
              <h1>Disconnection Activity</h1>
            </div>

            <div className="col-md-9">
              <div className="float-end">
                <a href="https://go.nzyme.org/disco" className="btn btn-secondary">Help</a>
              </div>
            </div>
          </div>

          <div className="row">
            <div className="col-md-12">
              <MonitoredNetworkSelector />
            </div>
          </div>

          <div className="row mt-3">
            <div className="col-md-12">
              <div className="card">
                <div className="card-body">
                  <h3>Disconnection Frame Monitor</h3>

                  <DiscoHistogram discoType="disconnection" minutes={24*60} />

                  <p className="mb-0 mt-3 text-muted">
                    <i>Disconnection activity</i> refers to the sum of deauthentication and disassociation frames.
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div className="row mt-3">
            <div className="col-md-6">
              <div className="card">
                <div className="card-body">
                  <h3>Deauthentication Frames Observed</h3>

                  <DiscoHistogram discoType="deauthentication" minutes={24*60} />
                </div>
              </div>
            </div>

            <div className="col-md-6">
              <div className="card">
                <div className="card-body">
                  <h3>Disassociation Frames Observed</h3>

                  <DiscoHistogram discoType="disassociation" minutes={24*60} />
                </div>
              </div>
            </div>
          </div>

          <div className="row mt-3">
            <div className="col-md-12">
              <div className="card">
                <div className="card-body">
                  <h3 className="mb-0">Top Pairs</h3>

                  <DiscoPairsTable />
                </div>
              </div>
            </div>
          </div>

          <div className="row mt-3">
            <div className="col-md-6">
              <div className="card">
                <div className="card-body">
                  <h3 className="mb-0">Top Senders</h3>

                  <DiscoSendersTable />
                </div>
              </div>
            </div>

            <div className="col-md-6">
              <div className="card">
                <div className="card-body">
                  <h3 className="mb-0">Top Receivers</h3>

                  <DiscoReceiversTable />
                </div>
              </div>
            </div>
          </div>
        </MonitoredNetworkContext.Provider>
      </React.Fragment>
  )

}

export default DiscoPage;