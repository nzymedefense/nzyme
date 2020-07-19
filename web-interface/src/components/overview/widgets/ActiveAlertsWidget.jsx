import React from 'react';
import Reflux from 'reflux';

class ActiveAlertsWidget extends Reflux.Component {

    render() {
        return (
            <div className={"card text-center overview-card " + (this.props.activeAlerts > 0 ? "bg-danger" : "bg-success") }>
                <div className="card-body">
                    <p>Active Alerts</p>
                    <span>{this.props.activeAlerts}</span>
                </div>
            </div>
        );
    }

}

export default ActiveAlertsWidget;



