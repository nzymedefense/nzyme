import React from 'react';

class AlertSSID extends React.Component {

    render() {
        if (this.props.ssid) {
            return this.props.ssid;
        } else {
            return "n/a";
        }

    }

}

export default AlertSSID;