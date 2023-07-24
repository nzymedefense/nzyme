import React, {useEffect, useState} from "react";
import ApiRoutes from "../../../util/ApiRoutes";
import Dot11Service from "../../../services/Dot11Service";
import SSIDSuggestions from "./SSIDSuggestions";
import LoadingSpinner from "../../misc/LoadingSpinner";
import {Navigate} from "react-router-dom";

const dot11Service = new Dot11Service();

function CreateDot11MonitoredNetworkPage() {

  const [ssidNames, setSSIDNames] = useState(null);
  const [ssid, setSSID] = useState("");

  const [formSubmitting, setFormSubmitting] = useState(false);
  const [submittedSuccessfully, setSubmittedSuccessfully] = useState(false);

  const onSuggestionSelected = function (ssid) {
    setSSID(ssid);
  }

  const onSubmitForm = function () {
    setFormSubmitting(true);

    dot11Service.createMonitoredSSID(ssid, function (){
      setSubmittedSuccessfully(true);
    }, function () {
      setFormSubmitting(false);
    })
  }

  useEffect(() => {
    dot11Service.findAllSSIDNames(setSSIDNames);
  }, [])

  if (submittedSuccessfully) {
    return <Navigate to={ApiRoutes.DOT11.MONITORING.INDEX} />
  }

  if (ssidNames === null) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.INDEX}>Monitoring</a></li>
                <li className="breadcrumb-item active" aria-current="page">Create Monitored Network</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-primary float-end" href={ApiRoutes.DOT11.MONITORING.INDEX}>Back</a>
          </div>

          <div className="row">
            <div className="col-md-6">
              <div className="card">
                <div className="card-body">
                  <h3>Create Monitored Network</h3>

                  <form className="mt-3">
                    <div className="mb-3">
                      <label htmlFor="ssid" className="form-label">
                        SSID{' '}
                        <span className="badge bg-warning" style={{position: "relative", top: -2, right: -3}}>
                          Case-sensitive
                        </span>
                      </label>

                      <input type="text"
                             className="form-control"
                             id="ssid" value={ssid} onChange={(e) => { setSSID(e.target.value) }}/>

                      <div className="form-text">
                        The SSID (network name) of the network you want to monitor. You will add details of the
                        new monitored network after it has been created.
                      </div>
                    </div>

                    <SSIDSuggestions input={ssid}
                                     ssidNames={ssidNames}
                                     onSuggestionSelected={onSuggestionSelected} />

                    <button type="button" className="btn btn-primary" onClick={onSubmitForm}>
                      {formSubmitting ? "Please wait ..." : "Create Monitored Network"}
                    </button>
                  </form>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default CreateDot11MonitoredNetworkPage;