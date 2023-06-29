import React, {useContext, useEffect, useState} from "react";
import Dot11Service from "../../../services/Dot11Service";
import {TapContext} from "../../../App";
import LoadingSpinner from "../../misc/LoadingSpinner";
import BSSIDsTable from "./BSSIDsTable";
import TimeRangeSelector from "../../misc/TimeRangeSelector";
import AutoRefreshSelector from "../../misc/AutoRefreshSelector";
import moment from "moment";
import BSSIDAndSSIDChart from "./BSSIDAndSSIDChart";

const dot11Service = new Dot11Service();

function BSSIDsPage() {

  const tapContext = useContext(TapContext);

  const [bssids, setBSSIDs] = useState(null);

  const [minutes, setMinutes] = useState(15);
  const [isAutoRefresh, setIsAutoRefresh] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);

  const selectedTaps = tapContext.taps;

  const loadData = function() {
    dot11Service.findAllBSSIDs(minutes, selectedTaps, setBSSIDs);
    setLastUpdated(new Date());
  }

  useEffect(() => {
    setBSSIDs(null);
    loadData();
  }, [selectedTaps])

  useEffect(() => {
    const timer = setInterval(() => {
      if (isAutoRefresh) {
        loadData();
      }
    }, 15000);

    return () => clearInterval(timer);
  }, [isAutoRefresh])


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
                  <TimeRangeSelector />
                  <AutoRefreshSelector isAutoRefresh={isAutoRefresh} setIsAutoRefresh={setIsAutoRefresh} />

                  {moment(lastUpdated).format()}
                  <BSSIDsTable bssids={bssids} minutes={minutes} isAutoRefresh={isAutoRefresh} />
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default BSSIDsPage;