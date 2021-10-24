import React from 'react';
import Routes from "../../util/Routes";

class SSID extends React.Component {

    render() {
        if (this.props.ssid.human_readable) {
            return (
                <span>
                    <a href={Routes.NETWORKS.SHOW(this.props.ssid.bssid, this.props.ssid.name, this.props.channel)}>
                        {this.props.ssid.name.trim()}
                    </a>
                    {this.props.ssid.is_monitored ? <i className="fas fa-heartbeat text-muted monitored-network" title="Monitored Network" /> : undefined }
                </span>
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