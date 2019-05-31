import React from 'react';
import Reflux from 'reflux';

import SSIDTable from "./SSIDTable";

class BSSIDTableRow extends Reflux.Component {

    constructor(props) {
        super(props);

        this.state = {
            displayDetails: false
        }

        this._bssidClick = this._bssidClick.bind(this);
    }

    _printSSIDs() {
        let x = "";
        const ssids = this.props.bssid.ssids;

        let total = Object.keys(ssids).length;

        Object.keys(ssids).forEach(function (key, ix) {
            let ssid = ssids[key].name.trim();

            x += ssid;

            if(ix < total-1) {
                x += ", ";
            }
        });

        if (x.length > 50) {
            x = x.slice(0, 50) + " ...";
        }

        return x;
    }

    _signalQualityColor(quality) {
        if (quality >= 90) {
            return "text-success";
        }

        if (quality >= 50) {
            return "text-warning";
        }

        if (quality < 50) {
            return "text-danger";
        }
    }

    static _decideFingerprintingStatus(status) {
        if (status) {
            return (
                <i className="fas fa-check-square text-success" title="Fingerprinting reports no issues." />
            )
        } else {
            return (
                <i className="fas fa-exclamation-triangle text-danger" title="Multiple fingerprints recorded." />
            )
        }
    }

    static _decideWPSStatus(status) {
        if (status) {
            return (
                <i className="fas fa-check-square text-warning" title="WPS is enabled on this station." />
            )
        } else {
            return (
                <i className="fas fa-times-circle text-muted" title="WPS is not enabled on this station." />
            )
        }
    }

    _printSecurity() {
        let x = "";
        const ssids = this.props.bssid.ssids;
        const total = ssids.length;

        Object.keys(ssids).forEach(function (key, ix) {
            const totalModes = ssids[key].security.length;
            ssids[key].security.forEach(function(security, ix) {
                x += security.wpa_mode;

                if(ix < totalModes-1) {
                    x += ", ";
                }
            });

            if(ix < total-1) {
                x += ", ";
            }
        });

        if (x === "NONE") {
            return (
                <span className="text-warning">NONE</span>
            )
        }

        return x;
    }

    _bssidClick(e) {
        e.preventDefault();

        const oldState = this.state.displayDetails;
        this.setState({displayDetails: !oldState})
    }

    render() {
        const self = this;

        return (
            <React.Fragment>
                <tr>
                    <td><a href="#" title={this.props.bssid.last_seen} onClick={this._bssidClick}>{this.props.bssid.bssid}</a></td>
                    <td>
                        <span className={this._signalQualityColor(this.props.bssid.best_recent_signal_quality)}>
                            {this.props.bssid.best_recent_signal_quality}
                        </span>
                    </td>
                    <td title={this.props.bssid.last_seen}>{this._printSSIDs()}</td>
                    <td>{this.props.bssid.oui}</td>
                    <td>{this._printSecurity()}</td>
                    <td>{BSSIDTableRow._decideFingerprintingStatus(this.props.bssid.fingerprinting_ok)}</td>
                    <td>{BSSIDTableRow._decideWPSStatus(this.props.bssid.is_wps)}</td>
                </tr>

                {Object.keys(this.props.bssid.ssids).map(function (key,i) {
                    return <SSIDTable key={i}  display={self.state.displayDetails} ssid={self.props.bssid.ssids[key]} />;
                })}
            </React.Fragment>
        )
    }

}

export default BSSIDTableRow;