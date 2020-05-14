import React from 'react';
import Reflux from 'reflux';

import StatisticsStore from "../../stores/StatisticsStore";
import StatisticsActions from "../../actions/StatisticsActions";
import AlertsStore from "../../stores/AlertsStore";
import AlertsActions from "../../actions/AlertsActions"
import ProbesStore from "../../stores/ProbesStore";
import LoadingSpinner from "../misc/LoadingSpinner";
import AlertsList from "../alerts/AlertsList";

import numeral from "numeral";

import SimpleLineChart from "../charts/SimpleLineChart";

class GlobalStatistics extends Reflux.Component {

  static ALERT_LIMIT = 25;

  constructor(props) {
    super(props);

    this.stores = [StatisticsStore, AlertsStore, ProbesStore];

    this.state = {
      global_statistics: undefined,
      active_alerts: undefined,
    };
  }

  componentDidMount() {
    this._loadData();

    setInterval(this._loadData, 5000);
  }

  _loadData() {
    StatisticsActions.findGlobal();
    AlertsActions.findActive(GlobalStatistics.ALERT_LIMIT);
  }

  render() {
    if (!this.state.global_statistics) {
      return <LoadingSpinner />;
    } else {
      return (
        <div>
          <div className="row">
            <div className="col-md-4">
              <div className="card bg-success text-center overview-statistic">
                <div className="card-body">
                  <p>Total 802.11 frames recorded:</p>
                  <span>{numeral(this.state.global_statistics.total_frames).format('0,0')}</span>
                </div>
              </div>

              <SimpleLineChart
                  title="802.11 Frame Throughput"
                  width={335}
                  height={150}
                  data={this.state.global_statistics.histogram_frame_throughput}/>
            </div>

            <div className="col-md-4">
              <div className="card bg-success text-center overview-statistic">
                <div className="card-body">
                  <p>802.11 Access points</p>
                  <span>{numeral(this.state.global_statistics.current_bssids.length).format('0,0')}</span>
                </div>
              </div>

              <SimpleLineChart
                  title="802.11 Access Points"
                  width={335}
                  height={150}
                  data={this.state.global_statistics.histogram_bssids} />
            </div>
          </div>

          <div className="row mt-md-4">
            <div className="col-md-12">
              <h3>Alerts <small>(Top {GlobalStatistics.ALERT_LIMIT})</small></h3>

              <AlertsList alerts={this.state.active_alerts} />
            </div>
          </div>

        </div>
      )
    }
  }

}

export default GlobalStatistics;