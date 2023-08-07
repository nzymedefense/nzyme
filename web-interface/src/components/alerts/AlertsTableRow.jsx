import React from "react";

import moment from "moment";
import Subsystem from "../misc/Subsystem";
import AlertActiveIndicator from "./AlertActiveIndicator";

function AlertsTableRow(props) {

  const alert = props.alert;

  return (
      <tr>
        <td>
          <AlertActiveIndicator active={alert.is_active} />
        </td>
        <td><a href="">{alert.details}</a></td>
        <td>{alert.detection_type}</td>
        <td><Subsystem subsystem={alert.subsystem} /></td>
        <td>{moment(alert.created_at).format()}</td>
        <td>{moment(alert.last_seen).format()}</td>
      </tr>
  )

}

export default AlertsTableRow;