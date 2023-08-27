import React from "react";
import numeral from "numeral";
import {notify} from "react-notify-toast";
import Dot11Service from "../../../services/Dot11Service";

const dot11Service = new Dot11Service();

function MonitoredChannelsTable(props) {

  const ssid = props.ssid;
  const bumpRevision = props.bumpRevision;
  const alertingEnabled = props.alertingEnabled;

  const onDelete = function (uuid) {
    if (!confirm("Really delete channel monitoring configuration?")) {
      return;
    }

    dot11Service.deleteMonitoredChannel(ssid.uuid, uuid, function () {
      bumpRevision();
      notify.show("Channel monitoring configuration deleted.", "success");
    })
  }

  if (ssid.channels.length === 0) {
    return (
        <div className="alert alert-info">
          No monitored channels configured yet.
        </div>
    )
  }

  return (
      <React.Fragment>
        {alertingEnabled ? null : <div className="alert alert-warning">Alerting for unexpected channels is disabled.</div>}

        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Frequency</th>
            <th>Channel</th>
            <th>&nbsp;</th>
          </tr>
          </thead>
          <tbody>
          {ssid.channels.map(function(channel, i) {
            return (
                <tr key={"channel-" + i}>
                  <td>{numeral(channel.frequency).format("0,0")} MHz</td>
                  <td>{channel.channel}</td>
                  <td><a href="#" onClick={() => onDelete(channel.uuid)}>Delete</a></td>
                </tr>
            )
          })}
          </tbody>
        </table>
      </React.Fragment>
  )

}

export default MonitoredChannelsTable;