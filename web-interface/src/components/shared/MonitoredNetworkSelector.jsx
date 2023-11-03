import React, {useContext, useEffect, useState} from "react";
import Dot11Service from "../../services/Dot11Service";
import {MonitoredNetworkContext} from "../dot11/disco/DiscoPage";
import {useSearchParams} from "react-router-dom";

const dot11Service = new Dot11Service();

export const SEARCHPARAM_KEY = "monitored-network-id";

function MonitoredNetworkSelector(props) {

  const [searchParams, setSearchParams] = useSearchParams();

  const monitoredNetworkContext = useContext(MonitoredNetworkContext);

  const [networks, setNetworks] = useState(null);

  useEffect(() => {
    dot11Service.findAllMonitoredSSIDs(setNetworks);
  }, [])

  const deleteMonitoredNetworkSearchParam = () => {
    searchParams.delete(SEARCHPARAM_KEY);
    setSearchParams(searchParams);
  }

  const reset = () => {
    if (monitoredNetworkContext.network) {
      return <a href="#" className="btn btn-outline-secondary"
                onClick={() => {
                  monitoredNetworkContext.setNetwork("");
                  deleteMonitoredNetworkSearchParam();
                }}>Reset</a>
    }
  }

  const onChange = (e, networkUuid) => {
    e.preventDefault();
    monitoredNetworkContext.setNetwork(networkUuid);

    if (networkUuid && networkUuid !== "") {
      searchParams.set(SEARCHPARAM_KEY, networkUuid);
      setSearchParams(searchParams);
    } else {
      deleteMonitoredNetworkSearchParam();
    }

  }

  if (!networks) {
    return (
        <select className="monitored-network-selector form-select" disabled={true}>
          <option value="">Loading Monitored Network Filter ...</option>
        </select>
    )
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <select className="monitored-network-selector form-select"
                    value={monitoredNetworkContext.network}
                    onChange={(e) => onChange(e, e.target.value)}>
              <option value="">Filter by Monitored Network</option>
              {networks.map(function(network, i){
                return (
                    <option value={network.uuid} key={i}>
                      {network.ssid}
                    </option>
                )
              })}
            </select>

            {reset()}
          </div>
        </div>
      </React.Fragment>
  )

}

export default MonitoredNetworkSelector;