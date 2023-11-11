import React, {useContext, useEffect, useState} from "react";
import Dot11Service from "../../../services/Dot11Service";
import {TapContext} from "../../../App";
import LoadingSpinner from "../../misc/LoadingSpinner";
import BSSIDsTable from "./BSSIDsTable";
import AutoRefreshSelector from "../../misc/AutoRefreshSelector";
import BSSIDAndSSIDChart from "./BSSIDAndSSIDChart";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";

const dot11Service = new Dot11Service();
const MINUTES = 15;

const loadData = function(taps, setBSSIDs, setLastUpdated) {
  dot11Service.findAllBSSIDs(MINUTES, taps, setBSSIDs);
  setLastUpdated(new Date());
}

function BSSIDsPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [bssids, setBSSIDs] = useState(null);

  const [isAutoRefresh, setIsAutoRefresh] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);

  useEffect(() => {
    setBSSIDs(null);
    loadData(selectedTaps, setBSSIDs, setLastUpdated);

    const timer = setInterval(() => {
      if (isAutoRefresh) {
        loadData(selectedTaps, setBSSIDs, setLastUpdated);
      }
    }, 15000);

    return () => clearInterval(timer);
  }, [isAutoRefresh, selectedTaps])

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  if (!bssids) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h1>Access Points</h1>
          </div>

          <div className="row mt-3">
            <div className="col-md-12">
              <div className="row">
                <div className="col-md-6">
                  <div className="card">
                    <div className="card-body">
                      <h3 className="mb-0">Active BSSIDs</h3>

                      <BSSIDAndSSIDChart parameter="bssid_count" />
                    </div>
                  </div>
                </div>
                <div className="col-md-6">
                  <div className="card">
                    <div className="card-body">
                      <h3 className="mb-0">Active SSIDs</h3>

                      <BSSIDAndSSIDChart parameter="ssid_count" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="row mt-3">
            <div className="col-md-12">
              <div className="card">
                <div className="card-body">
                  <h3 style={{display: "inline-block"}}>
                    Access Points <small>Last 15 minutes</small>
                  </h3>

                  <div className="float-end">
                    <AutoRefreshSelector isAutoRefresh={isAutoRefresh}
                                         setIsAutoRefresh={setIsAutoRefresh}
                                         lastUpdated={lastUpdated} />
                  </div>

                  <p className="text-muted">
                    List of all access points advertised by recorded beacon or probe response frames. Click on a BSSID
                    to open a list of all advertised SSIDs and their respective channels.
                  </p>

                  <BSSIDsTable bssids={bssids} minutes={MINUTES} isAutoRefresh={isAutoRefresh} />
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default BSSIDsPage;