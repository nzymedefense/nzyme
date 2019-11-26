import React from 'react';
import Reflux from 'reflux';
import Routes from "../../util/Routes";

class AlertsButton extends Reflux.Component {

    render() {
        if (this.props.hasAlerts) {
            return (
                <a href={Routes.ALERTS.INDEX} className="btn btn-outline-danger blink" title="Alerts">
                    <i className="fas fa-exclamation-triangle"/>
                </a>
            )
        } else {
            return (
                <a href={Routes.ALERTS.INDEX} className="btn btn-outline-dark" title="Alerts">
                    <i className="fas fa-check-circle" />
                </a>
            )
        }
    }

}

export default AlertsButton;