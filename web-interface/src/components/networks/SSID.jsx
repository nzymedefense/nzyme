import React from 'react';
import Routes from "../../util/Routes";

class SSID extends React.Component {

    render() {
        if (this.props.ssid.human_readable) {
            return (
                <a href={Routes.NETWORKS.SHOW(encodeURIComponent(this.props.ssid.bssid), encodeURIComponent(this.props.ssid.name))}>
                    {this.props.ssid.name.trim()}
                </a>
            );
        } else {
            return (
                <span className="text-muted">
                    {this.props.ssid.name.trim()}
                </span>
            );
        }

    }

}

export default SSID;