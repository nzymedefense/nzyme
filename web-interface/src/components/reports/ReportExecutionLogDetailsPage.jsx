import React, { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'

import Routes from '../../util/ApiRoutes'
import ReportName from './ReportName'
import ReportsService from '../../services/ReportsService'
import LoadingSpinner from '../misc/LoadingSpinner'
import moment from 'moment'

const reportsService = new ReportsService();

function ReportExecutionLogDetailsPage() {

    const{ reportName, executionId }= useParams();

    const [report, setReport] = useState(null);
    const [log, setLog] = useState(null);

    useEffect(() => {
        reportsService.findOne(reportName, setReport);
        reportsService.findExecutionLog(reportName, executionId, setLog);
    }, [reportName, executionId, setReport, setLog]);

    if (!report || !log) {
        return <LoadingSpinner />
    }

    return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <nav aria-label="breadcrumb">
                            <ol className="breadcrumb">
                                <li className="breadcrumb-item"><a href={Routes.SYSTEM.REPORTS.INDEX}>Reports</a></li>
                                <li className="breadcrumb-item">
                                    <a href={Routes.SYSTEM.REPORTS.DETAILS(report.name)}><ReportName name={report.name} /></a>
                                </li>
                                <li className="breadcrumb-item active" aria-current="page">
                                    Execution #{log.id}
                                </li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h1><ReportName name={report.name} /> Execution #{log.id}</h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-3">
                        <dl>
                            <dt>Completed at:</dt>
                            <dd>{moment(log.created_at).format()}</dd>
                        </dl>
                    </div>
                    <div className="col-md-8">
                        <dl>
                            <dt>Result:</dt>
                            <dd>{log.message}</dd>
                        </dl>
                    </div>
                    <div className="col-md-1">
                        <a href={Routes.SYSTEM.REPORTS.DETAILS(report.name)} className="btn btn-dark">Back</a>&nbsp;
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h2>Report Content:</h2>
                        <iframe title="Report Page" className="report-execution-content mt-1" srcDoc={log.content} />
                    </div>
                </div>
            </div>
    )

}

export default ReportExecutionLogDetailsPage
