import React, {useState} from "react";
import SystemService from "../../../services/SystemService";
import {notify} from "react-notify-toast";

const systemService = new SystemService();

function HealthConsoleConfiguration(props) {

  const indicators = props.indicators;

  const [configuration, setConfiguration] = useState({});

  const onIndicatorSelect = function(e) {
    const id = e.target.getAttribute("data-indicator-id");
    const active = e.target.checked;

    setConfiguration({
      ...configuration,
      [id]: {active: active}
    })
  }

  const isIndicatorSelected = function(id) {
    if (configuration[id]) {
      return configuration[id].active
    } else {
      return indicators[id].active
    }
  }

  const saveConfiguration = function() {
    systemService.updateHealthIndicatorsConfiguration(configuration, function() {
      notify.show('Configuration updated.', 'success')
    });
  }

  if (!indicators) {
    return <div className="alert alert-info">No indicators.</div>
  }

  return (
      <div className="row">
        <div className="col-md-12">
          <table className="table table-sm table-hover table-striped">
            <thead>
            <tr>
              <th>Indicator</th>
              <th>Active</th>
            </tr>
            </thead>
            <tbody>
            {Object.keys(indicators).sort((a, b) => a.localeCompare(b)).map(function (key, i) {
              return (
                  <tr key={"indicatorconf-" + key}>
                    <td>{indicators[key].name}</td>
                    <td>
                      <input type="checkbox"
                             data-indicator-id={indicators[key].id}
                             checked={isIndicatorSelected(indicators[key].id)}
                             onChange={onIndicatorSelect} />
                    </td>
                  </tr>
              )
            })}
            </tbody>
          </table>

          <button className="btn btn-success" onClick={saveConfiguration}>Save Configuration</button>
        </div>
      </div>
  )

}

export default HealthConsoleConfiguration;