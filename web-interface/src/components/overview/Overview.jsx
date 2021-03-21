import React from 'react';

import LoadingSpinner from "../misc/LoadingSpinner";
import ActiveAlertsWidget from "./widgets/ActiveAlertsWidget";
import ActiveContactsWidget from "./widgets/ActiveContactsWidget";
import SystemStatusWidget from "./widgets/SystemStatusWidget";
import FrameThroughputWidget from "./widgets/FrameThroughputWidget";
import AlertsTable from "../alerts/AlertsTable";
import ContactsTable from "../bandits/ContactsTable";
import ProbesTable from "../system/ProbesTable";
import DashboardService from "../../services/DashboardService";

class Overview extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            dashboard: undefined
        }


        this.dashboardService = new DashboardService();
        this.dashboardService.findAll = this.dashboardService.findAll.bind(this);

        this._loadData = this._loadData.bind(this);
    }

    componentDidMount() {
        this._loadData();

        setInterval(this._loadData, 5000);
    }

    _loadData() {
        this.dashboardService.findAll();
    }

    render() {
        if (!this.state.dashboard) {
            return <LoadingSpinner/>;
        }

        return (
            <div>
                <div className="row">
                    <div className="col-md-4">
                        <ActiveAlertsWidget activeAlerts={this.state.dashboard.active_alerts} />
                    </div>

                    <div className="col-md-4">
                        <ActiveContactsWidget activeContacts={this.state.dashboard.active_contacts} />
                    </div>

                    <div className="col-md-4">
                        <SystemStatusWidget systemHealthStatus={this.state.dashboard.system_health_status} />
                    </div>
                </div>

                <div className="row mt-md-3">
                    <div className="col-md-12">
                        <h4>802.11 Frame Throughput</h4>
                        <FrameThroughputWidget frameThroughputHistogram={this.state.dashboard.frame_throughput_histogram} />
                    </div>
                </div>

                <div className="row mt-md-3">
                    <div className="col-md-12">
                        <h4>Alerts (last 5)</h4>
                        <AlertsTable alerts={this.state.dashboard.alerts.alerts} />
                    </div>
                </div>

                <div className="row mt-md-3">
                    <div className="col-md-12">
                        <h4>Recent Bandit Contacts (last 5)</h4>
                        <ContactsTable contacts={this.state.dashboard.contacts} />
                    </div>
                </div>

                <div className="row mt-md-3">
                    <div className="col-md-12">
                        <h4>Probes</h4>
                        <ProbesTable probes={this.state.dashboard.probes.probes} />
                    </div>
                </div>
            </div>
        );
    }

}

export default Overview;



