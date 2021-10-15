import React from 'react';
import moment from "moment";
import ReportExecutionLogStatusBadge from "./ReportExecutionLogStatusBadge";

class ReportExecutionLog extends React.Component {

    render() {
        const logs = this.props.logs;

        if (!logs || logs.length === 0) {
            return (
                <div className="alert alert-info">
                    No report executions yet.
                </div>
            )
        } else {
            return (
                <table className="table table-sm table-hover table-striped">
                    <thead>
                    <tr>
                        <th>Execution Time</th>
                        <th>Result</th>
                        <th>Message</th>
                    </tr>
                    </thead>
                    <tbody>
                    {Object.keys(logs).map(function (key,i) {
                        return (
                        <tr>
                            <td title={logs[key].created_at}>{moment(logs[key].created_at).format()}</td>
                            <td><ReportExecutionLogStatusBadge status={logs[key].result} /></td>
                            <td>{logs[key].message}</td>
                        </tr>
                        )
                    })}
                    </tbody>
                </table>
            )
        }
    }

}

export default ReportExecutionLog;