import React from 'react';

import LoadingSpinner from "../misc/LoadingSpinner";

import moment from "moment";
import FrameCount from "./FrameCount";
import AlertField from "./AlertField";
import Routes from "../../util/Routes";
import AlertsService from "../../services/AlertsService";

class AlertDetailsPage extends React.Component {

    constructor(props) {
        super(props);

        this.alertId = props.match.params.id;

        this.alertsService = new AlertsService();
        this.alertsService.findOne = this.alertsService.findOne.bind(this);

        this.state = {
            alert: undefined
        };
    }

    componentDidMount() {
        const alertId = this.alertId;
        this.alertsService.findOne(alertId);
        const self = this;
        setInterval(function() { self.alertsService.findOne(alertId) }, 5000);
    }

    render() {
        const self = this;
        if (!this.state.alert) {
            return <LoadingSpinner/>;
        } else {
            return (
                <div>
                    <div className="row">
                        <div className="col-md-12">
                            <nav aria-label="breadcrumb">
                                <ol className="breadcrumb">
                                    <li className="breadcrumb-item">
                                        <a href={Routes.ALERTS.INDEX}>Alerts</a>
                                    </li>
                                    <li className="breadcrumb-item" aria-current="page">
                                        {this.alertId}
                                    </li>
                                </ol>
                            </nav>
                        </div>
                    </div>

                    <div className="row">
                        <div className="col-md-12">
                            <h2>
                                Alert <em>{this.state.alert.id}</em>

                                <small>
                                    &nbsp;
                                    {this.state.alert.is_active ?
                                        <span className="badge badge-danger pull-right">active/ongoing</span>
                                        : <span className="badge badge-info">inactive</span>}
                                </small>
                            </h2>



                            <blockquote className={"text-danger"} style={{"font-weight":"bold"}}>{this.state.alert.message}</blockquote>

                            <hr />
                        </div>
                    </div>

                    <div className="row">
                        <div className="col-md-6">
                            <dl>
                                <h3>Time</h3>

                                <dt>First seen:</dt>
                                <dd>{moment(this.state.alert.first_seen).format()}  ({moment(this.state.alert.first_seen).fromNow()})</dd>
                                <dt>Last seen:</dt>
                                <dd>{moment(this.state.alert.last_seen).format()} ({moment(this.state.alert.last_seen).fromNow()})</dd>
                            </dl>

                            <hr />

                            <p>
                                <h3>Meta Information</h3>

                                <dl>
                                    {Object.keys(this.state.alert.fields).map(function (key) {
                                        return <AlertField fieldKey={key} value={self.state.alert.fields[key]} fields={self.state.alert.fields} />
                                    })}
                                </dl>
                            </p>

                            <hr />

                            <p>
                                <dl>
                                    <dt>Frames</dt>
                                    <dd><FrameCount alert={this.state.alert} /></dd>
                                    <dt>Subsystem</dt>
                                    <dd>{this.state.alert.subsystem}</dd>
                                    <dt>Alert Type ID</dt>
                                    <dd>{this.state.alert.type}</dd>
                                </dl>
                            </p>
                        </div>

                        <div className="col-md-6">
                            <div className="alert alert-info">
                                <h3>Guidance</h3>
                                <p>
                                    {this.state.alert.description}
                                </p>

                                <h4>Possible False Positives</h4>
                                <ul>
                                    {Object.keys(this.state.alert.false_positives).map(function (key) {
                                        return (<li>{self.state.alert.false_positives[key]}</li>);
                                    })}
                                </ul>

                                <p>
                                    <a href={"https://go.nzyme.org/" + this.state.alert.documentation_link} className="btn btn-primary" target="_blank">
                                        Learn More
                                    </a>
                                </p>
                            </div>
                        </div>
                    </div>

                </div>
            );
        }
    }

}

export default AlertDetailsPage;