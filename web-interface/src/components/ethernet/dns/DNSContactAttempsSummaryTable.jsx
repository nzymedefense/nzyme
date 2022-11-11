import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";

import numeral from "numeral";

function DNSContactAttempsSummaryTable(props) {

    if (!props.data || !props.data.pair_summary) {
        return <LoadingSpinner />
    }

    return (
        <table className="table table-sm table-hover table-striped">
            <thead>
            <tr>
                <th>DNS Server</th>
                <th>Contact Attempts</th>
                <th>Unique Clients</th>
            </tr>
            </thead>
            <tbody>
            {props.data.pair_summary.map(function (key, i) {
                return (
                    <tr key={"pair-"+i}>
                        <td>{props.data.pair_summary[i].server}</td>
                        <td>{numeral(props.data.pair_summary[i].request_count).format()}</td>
                        <td>{numeral(props.data.pair_summary[i].client_count).format()}</td>
                    </tr>
                )
            })}
            </tbody>
        </table>
    )

}

export default DNSContactAttempsSummaryTable;