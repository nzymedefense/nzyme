import React from 'react';
import Reflux from 'reflux';

import StatisticsStore from "../../stores/StatisticsStore";
import StatisticsActions from "../../actions/StatisticsActions";
import AlertsStore from "../../stores/AlertsStore";
import AlertsActions from "../../actions/AlertsActions"
import LoadingSpinner from "../misc/LoadingSpinner";

import numeral from "numeral";
import AlertsList from "./AlertsList";


class GlobalStatistics extends Reflux.Component {

  static ALERT_LIMIT = 25;

  constructor(props) {
    super(props);

    this.stores = [StatisticsStore, AlertsStore];

    this.state = {
      global_statistics: undefined,
      active_alerts: undefined
    };
  }

  componentDidMount() {
    StatisticsActions.findGlobal();
    AlertsActions.findActive(GlobalStatistics.ALERT_LIMIT);

    setInterval(StatisticsActions.findGlobal, 1000);
    setInterval(AlertsActions.findActive, 1000);
  }

  static _buildChannelRow(channel, data) {
    const quality = ((data.total_frames-data.malformed_frames)*100)/data.total_frames;
    return (
      <tr key={channel}>
        <td>{channel}</td>
        <td>{numeral(data.total_frames).format('0,0')}</td>
        <td className={GlobalStatistics.decideFrameQualityColor(quality)}>{numeral(data.malformed_frames).format('0')}</td>
        <td className={GlobalStatistics.decideFrameQualityColor(quality)}>{numeral(quality).format('0')}</td>
      </tr>
    )
  }

  static _buildFrameTypeRow(name, count, total) {
    return (
      <tr key={name}>
        <td>{name}</td>
        <td>{numeral(count).format('0,0')}</td>
        <td>{numeral(count*100/total).format('0.00')}%</td>
      </tr>
    )
  }

  static decideFrameQualityColor(quality) {
    if (quality < 80) {
      return "text-warning";
    }
  }

  static _getTotalFrameCountFromStatistics(frames) {
    let x = 0;

    Object.keys(frames).map(function (key) {
      return x += frames[key];
    });

    return x;
  }

  render() {
    const self = this;
    if (!this.state.global_statistics) {
      return <LoadingSpinner />;
    } else {
      return (
        <div>
          <div className="row">
            <div className="col-md-4">
              <div className="card bg-success text-center overview-statistic">
                <div className="card-body">
                  <p>Total 802.11 frames considered:</p>
                  <span>{numeral(this.state.global_statistics.total_frames).format('0,0')}</span>
                </div>
              </div>
            </div>

            <div className="col-md-4">
              <div className="card bg-success text-center overview-statistic">
                <div className="card-body">
                  <p>802.11 Probing devices</p>
                  <span>{numeral(this.state.global_statistics.current_probing_devices.length).format('0,0')}</span>
                </div>
              </div>
            </div>

            <div className="col-md-4">
              <div className="card bg-success text-center overview-statistic">
                <div className="card-body">
                  <p>802.11  Access points</p>
                  <span>{numeral(this.state.global_statistics.current_bssids.length).format('0,0')}</span>
                </div>
              </div>
            </div>
          </div>

          <div className="row mt-md-4">
            <div className="col-md-12">
              <h3>Alerts <small>(Top {GlobalStatistics.ALERT_LIMIT})</small></h3>

              <AlertsList alerts={this.state.active_alerts} />
            </div>
          </div>

          <div className="row mt-md-4">
            <div className="col-md-6">
              <h3 style={{display: "block"}} className="text-center">802.11 Channels</h3>

              <table className="table table-sm table-hover table-striped">
                <thead>
                  <tr>
                    <th>Channel</th>
                    <th>Total frames considered</th>
                    <th>Malformed</th>
                    <th>Quality</th>
                  </tr>
                </thead>

                <tbody>
                {Object.keys(this.state.global_statistics.channels).map(function (key) {
                  return GlobalStatistics._buildChannelRow(key, self.state.global_statistics.channels[key])
                })}
                </tbody>
              </table>
            </div>

            <div className="col-md-6">
              <h3 style={{display: "block"}} className="text-center">802.11 Frame Types</h3>
              <table className="table table-sm table-hover table-striped">
                <thead>
                  <tr>
                    <th>Frame Type</th>
                    <th>Total frames considered</th>
                    <th>Percentage</th>
                  </tr>
                </thead>

                <tbody>
                {Object.keys(this.state.global_statistics.frame_types).map(function (key) {
                  return GlobalStatistics._buildFrameTypeRow(key, self.state.global_statistics.frame_types[key], GlobalStatistics._getTotalFrameCountFromStatistics(self.state.global_statistics.frame_types))
                })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )
    }
  }

}

export default GlobalStatistics;