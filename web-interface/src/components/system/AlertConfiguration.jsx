import React from 'react';

import LoadingSpinner from "../misc/LoadingSpinner";
import AlertsService from "../../services/AlertsService";

class AlertConfiguration extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            alert_configuration: undefined
        };

        this.alertsService = new AlertsService();
        this.alertsService.getConfiguration = this.alertsService.getConfiguration.bind(this);
    }

    componentDidMount() {
        this.alertsService.getConfiguration();
    }

    render() {
        const self = this;
        if (!this.state.alert_configuration) {
            return <LoadingSpinner />;
        } else {
            return (
                <div>
                    <div className="row">
                        <div className="col-md-12">
                            <h3>Configured Alerts <small><a href="https://go.nzyme.org/alerts-explained" target="_blank" rel="noopener noreferrer">help</a></small></h3>

                            <ul>
                                {Object.keys(this.state.alert_configuration.enabled).map(function (key) {
                                    return <li className={"text-success"} key={key}>{self.state.alert_configuration.enabled[key]}</li>
                                })}

                                {Object.keys(this.state.alert_configuration.disabled).map(function (key) {
                                    return <li className={"state-not-active"} key={key}>{self.state.alert_configuration.disabled[key]}</li>
                                })}
                            </ul>
                        </div>
                    </div>
                </div>
            )
        }
    }

}

export default AlertConfiguration;