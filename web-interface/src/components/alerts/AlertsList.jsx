import React from 'react';
import Reflux from 'reflux';

import LoadingSpinner from "../misc/LoadingSpinner";
import moment from "moment";
import Routes from "../../util/Routes";
import FrameCount from "./FrameCount";

class AlertsList extends Reflux.Component {

  constructor(props) {
    super(props);

    this.state = {
      alerts: props.alerts
    }
  }

  componentWillReceiveProps(newProps) {
    this.setState({alerts: newProps.alerts});
  }

  static _buildAlertRow(key, alert) {
    return (
      <tr key={key} className={alert.is_active ? "text-danger" : "text-warning"}>
        <td>{alert.id}</td>
        <td>{alert.type}</td>
        <td>{moment(alert.first_seen).fromNow()}</td>
        <td>{moment(alert.last_seen).fromNow()}</td>
        <td><FrameCount alert={alert} /></td>
        <td><a href={Routes.ALERTS.SHOW(alert.id)}>Details</a></td>
      </tr>
    )
  }

  _buildTableContent() {
    if (this.state.alerts.length === 0) {
      return (
        <tr>
          <td colSpan={6} className="text-center text-success">No alerts found!</td>
        </tr>
      )
    } else {
      let self = this;
      return Object.keys(this.state.alerts).map(function (key) {
        return (
          AlertsList._buildAlertRow(key, self.state.alerts[key])
        )
      })
    }
  }

  render() {
    let self = this;
    if (!this.state.alerts) {
      return <LoadingSpinner />;
    } else {
      return (
        <div className="row">
          <div className="col-md-12">
            <table className="table table-sm table-hover table-striped">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Type</th>
                  <th>First Seen</th>
                  <th>Last Seen</th>
                  <th>Frames</th>
                  <th>&nbsp;</th>
                </tr>
              </thead>
              <tbody>
              {self._buildTableContent()}
              </tbody>
            </table>
          </div>
        </div>
      );
    }
  }

}

export default AlertsList;