import React from 'react';
import Reflux from 'reflux';

import AlertsStore from "../../stores/AlertsStore";
import AlertsActions from "../../actions/AlertsActions";
import LoadingSpinner from "../misc/LoadingSpinner";

class AlertConfiguration extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = AlertsStore;

        this.state = {
            alert_configuration: undefined
        };
    }

    componentDidMount() {
        AlertsActions.getConfiguration();
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
                            <h3>Configured Alerts</h3>

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