import React from 'react';
import Routes from "../../util/Routes";
import LoadingSpinner from "../misc/LoadingSpinner";
import ReportName from "./ReportName";
import ReportsService from "../../services/ReportsService";
import moment from "moment";
import ReportFireTime from "./ReportFireTime";
import {notify} from "react-notify-toast";
import EmailReceiversDetailsTable from "./EmailReceiversDetailsTable";
import {Redirect} from "react-router-dom";
import ReportExecutionLog from "./ReportExecutionLog";

class ReportDetailsPage extends React.Component {

    constructor(props) {
        super(props);

        this.reportName = decodeURIComponent(props.match.params.name);

        this.state = {
            report: undefined,
            reportDeleted: false,
            addEmailFilled: false
        }

        this.addEmail = React.createRef();
        this._deleteReport = this._deleteReport.bind(this);
        this._updateAddEmail = this._updateAddEmail.bind(this);
        this._addEmailReceiver = this._addEmailReceiver.bind(this);

        this.reportsService = new ReportsService();
        this.reportsService.findOne = this.reportsService.findOne.bind(this);
        this.reportsService.deleteReport = this.reportsService.deleteReport.bind(this);

        this.deleteEmailReceiver = this.deleteEmailReceiver.bind(this);
    }

    componentDidMount() {
        this.reportsService.findOne(this.reportName);
    }

    deleteEmailReceiver(email) {
        const self = this;

        this.reportsService.deleteEmailReceiver(self.reportName, email,
            function() {
                notify.show("Report receiver deleted.", "success");

                // Update report data / refresh email receivers.
                self.reportsService.findOne(self.reportName);
            },
            function() {
                notify.show("Could not delete report receiver. Please check nzyme log file.", "error");
            }
        );
    }

    _deleteReport() {
        if (!window.confirm("Delete report?")) {
            return;
        }

        this.reportsService.deleteReport(this.reportName, function() {
            notify.show("Could not delete report. Please check nzyme log file.", "error");
        });
    }

    _addEmailReceiver() {
        const self = this;
        const receiver = this.addEmail.current.value;

        if (receiver && receiver.trim() !== "") {
            if (this.state.report.email_receivers.includes(receiver)) {
                notify.show("Email receiver already exists.", "error");
                return;
            }

            this.addEmail.current.value = "";

            this.reportsService.addEmailReceiver(self.reportName, receiver,
                function() {
                    notify.show("Added report receiver.", "success");

                    self.setState(prevState => ({
                        addEmailFilled: false // we have to set this because the reset about does not trigger onChange and button is never disabled
                    }))

                    // Update report data / refresh email receivers.
                    self.reportsService.findOne(self.reportName);
                },
                function() {
                    notify.show("Could not add report receiver. Please check nzyme log file.", "error");
                }
            );
        }
    }

    _updateAddEmail() {
        this.setState({
            addEmailFilled: this.addEmail.current && this.addEmail.current.value.trim() !== ""
        })
    }

    render() {
        if (this.state.reportDeleted) {
            return <Redirect to={Routes.SYSTEM.REPORTS.INDEX} />
        }

        if (!this.state.report) {
            return <LoadingSpinner />
        }

        const self = this;
        const report = this.state.report;

        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <nav aria-label="breadcrumb">
                            <ol className="breadcrumb">
                                <li className="breadcrumb-item"><a href={Routes.SYSTEM.REPORTS.INDEX}>Reports</a></li>
                                <li className="breadcrumb-item active" aria-current="page">
                                    <ReportName name={this.state.report.name} />
                                </li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h1>Report Details</h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-3">
                        <dl>
                            <dt>Name:</dt>
                            <dd><ReportName name={report.name} /></dd>
                        </dl>
                    </div>
                    <div className="col-md-3">
                        <dl>
                            <dt>Created at:</dt>
                            <dd>{moment(report.created_at).format()}</dd>
                        </dl>
                    </div>

                    <div className="col-md-6">
                        <span className="float-right">
                            <a href={Routes.SYSTEM.REPORTS.INDEX} className="btn btn-dark">Back</a>&nbsp;
                            <button className="btn btn-danger" onClick={this._deleteReport}>Delete Report</button>&nbsp;
                        </span>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-3">
                        <dl>
                            <dt>Next Fire Time:</dt>
                            <dd><ReportFireTime time={report.next_fire_time} /></dd>
                        </dl>
                    </div>

                    <div className="col-md-3">
                        <dl>
                            <dt>Previous Fire Time:</dt>
                            <dd><ReportFireTime time={report.previous_fire_time} /></dd>
                        </dl>
                    </div>

                    <div className="col-md-3">
                        <dl>
                            <dt>Schedule:</dt>
                            <dd title={report.cron_expression}>{report.schedule_string}</dd>
                        </dl>
                    </div>

                    <div className="col-md-3">
                        <dl>
                            <dt>Trigger State:</dt>
                            <dd>{report.trigger_state}</dd>
                        </dl>
                    </div>
                </div>

                <hr />

                <div className="row">
                    <div className="col-md-12">
                        <h2>Receivers</h2>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-6">
                        <div className="form-group" ref={this.formDetails}>
                            <label htmlFor="addEmail">Add Email Receiver</label>

                            <div className="input-group">
                                <input id="addEmail"
                                       type="text"
                                       className="form-control"
                                       placeholder="john@example.org"
                                       ref={this.addEmail}
                                       onChange={this._updateAddEmail}
                                       onKeyPress={(e) => { e.key === 'Enter' && e.preventDefault(); }} />
                                <div className="input-group-append">
                                    <button className="btn btn-secondary" type="button" onClick={this._addEmailReceiver} disabled={!this.state.addEmailFilled}>
                                        Add Email Receiver
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <EmailReceiversDetailsTable report={report} onDeleteEmailReceiver={self.deleteEmailReceiver} />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h2>Execution Log <small>(previous 14 executions)</small></h2>
                    </div>

                    <div className="col-md-12">
                        <ReportExecutionLog reportName={this.reportName} logs={report.recent_execution_log} />
                    </div>
                </div>

            </div>
        )
    }

}

export default ReportDetailsPage;