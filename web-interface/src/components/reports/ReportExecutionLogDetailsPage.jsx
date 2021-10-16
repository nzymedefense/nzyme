import React from 'react';
import Routes from "../../util/Routes";
import ReportName from "./ReportName";
import ReportsService from "../../services/ReportsService";
import LoadingSpinner from "../misc/LoadingSpinner";

class ReportExecutionLogDetailsPage extends React.Component {

    constructor(props) {
        super(props);

        this.reportName = decodeURIComponent(props.match.params.name);
        this.executionId = decodeURIComponent(props.match.params.executionId);

        this.state = {
            report: undefined,
            log: undefined
        }

        this.reportsService = new ReportsService();
        this.reportsService.findOne = this.reportsService.findOne.bind(this);
        this.reportsService.findExecutionLog = this.reportsService.findExecutionLog.bind(this);
    }

    componentDidMount() {
        this.reportsService.findOne(this.reportName);
        this.reportsService.findExecutionLog(this.reportName, this.executionId);
    }

    render () {
        if (!this.state.report || !this.state.log) {
            return <LoadingSpinner />
        }

        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <nav aria-label="breadcrumb">
                            <ol className="breadcrumb">
                                <li className="breadcrumb-item"><a href={Routes.REPORTS.INDEX}>Reports</a></li>
                                <li className="breadcrumb-item">
                                    <a href={Routes.REPORTS.DETAILS(this.reportName)}><ReportName name={this.reportName} /></a>
                                </li>
                                <li className="breadcrumb-item active"  aria-current="page">
                                    Execution #{this.executionId}
                                </li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h1><ReportName name={this.reportName} /> Execution #{this.executionId}</h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <iframe title="Report Page" className="report-execution-content" srcDoc={this.state.log.content} />
                    </div>
                </div>
            </div>
        )
    }

}

export default ReportExecutionLogDetailsPage;