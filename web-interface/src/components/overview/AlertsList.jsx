import React from 'react';
import Reflux from 'reflux';

import LoadingSpinner from "../misc/LoadingSpinner";
import numeral from "numeral";
import moment from "moment";
import Routes from "../../util/Routes";

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
      <tr key={key} className="text-danger">
        <td>{alert.id}</td>
        <td>{alert.type}</td>
        <td>{moment(alert.first_seen).fromNow()}</td>
        <td>{moment(alert.last_seen).fromNow()}</td>
        <td>{numeral(alert.frame_count).format('0,0')}</td>
        <td><a href={Routes.ALERTS.SHOW(alert.id)}>Details</a></td>
      </tr>
    )
  }

  _buildTableContent() {
    if (this.state.alerts.length === 0) {
      return (
        <tr>
          <td colSpan={6} className="text-center text-success">No currently active alerts!</td>
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