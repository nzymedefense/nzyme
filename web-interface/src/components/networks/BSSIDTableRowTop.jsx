import React from 'react';
import Reflux from "reflux";

class BSSIDTableRowTop extends Reflux.Component {

    constructor(props) {
        super(props);

        this.bssidClickSuperHandler = props.clickHandler;
        this._bssidClick = this._bssidClick.bind(this);
    }

    _bssidClick(e) {
        e.preventDefault();
        this.bssidClickSuperHandler(e);
    }


    _printSSIDs() {
        let x = "";
        const ssids = this.props.bssid.ssids;

        let total = this.props.bssid.ssids.length;

        ssids.forEach(function (ssid, ix) {
            x += ssid.trim();

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
        if (quality >= -60) {
            return "text-success";
        }

        if (quality >= -70) {
            return "text-warning";
        }

        if (quality < -50) {
            return "text-danger";
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
        const secs = this.props.bssid.security_mechanisms;

        let total = this.props.bssid.security_mechanisms.length;

        secs.forEach(function (sec, ix) {
            x += sec.trim();

            if(ix < total-1) {
                x += ", ";
            }
        });

        if (x.length > 25) {
            x = x.slice(0, 25) + " ...";
        }

        if (x === "NONE") {
            return (
                <span className="text-warning">NONE</span>
            )
        }

        return x;
    }

    render() {
        return (
            <tr>
                <td><a href={"#" + this.props.bssid.bssid} title={this.props.bssid.last_seen}
                       onClick={this._bssidClick}>{this.props.bssid.bssid}</a></td>
                <td>
                    <span className={this._signalQualityColor(this.props.bssid.signal_strength)}>
                        {this.props.bssid.signal_strength} dBm
                    </span>
                </td>
                <td title={this.props.bssid.last_seen}>{this._printSSIDs()}</td>
                <td>{this.props.bssid.oui}</td>
                <td>{this._printSecurity()}</td>
                <td>{this.props.bssid.fingerprint_count}</td>
                <td>{BSSIDTableRowTop._decideWPSStatus(this.props.bssid.has_wps)}</td>
            </tr>
        )
    }

}

export default BSSIDTableRowTop;