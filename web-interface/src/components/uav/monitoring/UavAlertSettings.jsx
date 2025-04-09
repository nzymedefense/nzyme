import React, {useEffect, useState} from "react";
import UavService from "../../../services/UavService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import {notify} from "react-notify-toast";

const uavService = new UavService();

export default function UavAlertSettings(props) {

  const organizationId = props.organizationId;
  const tenantId = props.tenantId;

  const [config, setConfig] = useState(null);

  const [alertOnUnknown, setAlertOnUnknown] = useState(false);
  const [alertOnFriendly, setAlertOnFriendly] = useState(false);
  const [alertOnNeutral, setAlertOnNeutral] = useState(false);
  const [alertOnHostile, setAlertOnHostile] = useState(false);

  const [revision, setRevision] = useState(new Date());

  useEffect(() => {
    setConfig(null);
    uavService.getMonitoringConfig(setConfig, organizationId, tenantId);
  }, [organizationId, tenantId, revision]);

  useEffect(() => {
    if (config) {
      setAlertOnUnknown(config.alert_on_unknown);
      setAlertOnFriendly(config.alert_on_friendly);
      setAlertOnNeutral(config.alert_on_neutral);
      setAlertOnHostile(config.alert_on_hostile);
    }
  }, [config])

  const onSave = () => {
    if (!confirm("Really save new UAV alert settings?")) {
      return;
    }

    uavService.setMonitoringConfig(
        alertOnUnknown,
        alertOnFriendly,
        alertOnNeutral,
        alertOnHostile,
        organizationId,
        tenantId, () => {
          notify.show("UAV alert settings have been updated.", "success");
          setRevision(new Date());
        });
  }

  if (!config) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th style={{width: 65}}>Enabled</th>
            <th>Alert Type</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td>
              <input className="form-check-input" type="checkbox" checked={alertOnUnknown}
                     onChange={(e) => setAlertOnUnknown(e.target.checked)} />
            </td>
            <td>Alert when UAV with <em>Unknown</em> classification is detected.</td>
          </tr>
          <tr>
            <td>
              <input className="form-check-input" type="checkbox" checked={alertOnFriendly}
                     onChange={(e) => setAlertOnFriendly(e.target.checked)} />
            </td>
            <td>Alert when UAV with <em>Friendly</em> classification is detected.</td>
          </tr>
          <tr>
            <td>
              <input className="form-check-input" type="checkbox" checked={alertOnNeutral}
                     onChange={(e) => setAlertOnNeutral(e.target.checked)} />
            </td>
            <td>Alert when UAV with <em>Neutral</em> classification is detected.</td>
          </tr>
          <tr>
            <td>
              <input className="form-check-input" type="checkbox" checked={alertOnHostile}
                     onChange={(e) => setAlertOnHostile(e.target.checked)} />
            </td>
            <td>Alert when UAV with <em>Hostile</em> classification is detected.</td>
          </tr>
          </tbody>
        </table>

        <button className="btn btn-primary" onClick={onSave}>Save Changes</button>
      </React.Fragment>
  )

}