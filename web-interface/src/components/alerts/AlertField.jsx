import React from 'react';
import Routes from "../../util/Routes";

class AlertField extends React.Component {

    render() {
        const key = this.props.fieldKey;
        const value = this.props.value;
        const fields = this.props.fields;

        console.log(this.props);

        let additional;
        // Link to bandit.
        if ((key === "bandit_name" && fields.hasOwnProperty("bandit_uuid"))|| key === "bandit_uuid") {
            additional = <a href={Routes.BANDITS.SHOW(fields.bandit_uuid)}><i className="fas fa-link" /></a>
        }

        // Link to network details if channel is known.
        if (fields.hasOwnProperty("ssid") && fields.hasOwnProperty("bssid") && fields.hasOwnProperty("channel")) {
            // We have all fields to link to a network. Link the right fields.
            if(key === "ssid" || key === "bssid") {
                additional = <a href={Routes.NETWORKS.SHOW(fields.bssid, fields.ssid, fields.channel)}><i className="fas fa-link" /></a>;
            }
        }

        // Link to network details.
        if (fields.hasOwnProperty("ssid") && fields.hasOwnProperty("bssid") && !fields.hasOwnProperty("channel")) {
            // We have all fields to link to a network. Link the right fields.
            if(key === "ssid" || key === "bssid") {
                additional = <a href={Routes.NETWORKS.PROXY(fields.bssid, fields.ssid)}><i className="fas fa-link" /></a>;
            }
        }

        return (
            <React.Fragment>
                <dt>{key}</dt>
                <dd>{value} {additional}</dd>
            </React.Fragment>
        )
    }

}

export default AlertField;