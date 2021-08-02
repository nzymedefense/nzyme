import React from 'react';
import Routes from "../../util/Routes";
import LoadingSpinner from "../misc/LoadingSpinner";
import ReportName from "./ReportName";
import ReportsService from "../../services/ReportsService";
import moment from "moment";

class ReportDetailsPage extends React.Component {

    constructor(props) {
        super(props);

        this.reportName = decodeURIComponent(props.match.params.name);

        this.state = {
            report: undefined
        }

        this.reportsService = new ReportsService();
        this.reportsService.findOne = this.reportsService.findOne.bind(this);
    }

    componentDidMount() {
        this.reportsService.findOne(this.reportName);
    }

    render() {
        if (!this.state.report) {
            return <LoadingSpinner />
        }

        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <nav aria-label="breadcrumb">
                            <ol className="breadcrumb">
                                <li className="breadcrumb-item"><a href={Routes.REPORTS.INDEX}>Reports</a></li>
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
                    <div className="col-md-12">
                        <pre style={{"backgroundColor": "#fff"}}>{JSON.stringify(this.state.report, null ,2)}</pre>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-3">
                        <dl>
                            <dt>Created at:</dt>
                            <dd>foo</dd>
                        </dl>
                    </div>
                </div>

            </div>
        )
    }

}

export default ReportDetailsPage;