import React, {useContext, useEffect, useState} from "react";
import Dot11Service from "../../../services/Dot11Service";
import {TapContext} from "../../../App";
import LoadingSpinner from "../../misc/LoadingSpinner";
import BSSIDsTable from "./BSSIDsTable";

const dot11Service = new Dot11Service();

function BSSIDsPage() {

  const tapContext = useContext(TapContext);

  const [bssids, setBSSIDs] = useState(null);
  const [minutes, setMinutes] = useState(15);

  const selectedTaps = tapContext.taps;

  useEffect(() => {
    setBSSIDs(null);
    dot11Service.findAllBSSIDs(minutes, selectedTaps, setBSSIDs);
  }, [selectedTaps])

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
              <div className="card">
                <div className="card-body">
                  <BSSIDsTable bssids={bssids} minutes={minutes} />
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default BSSIDsPage;