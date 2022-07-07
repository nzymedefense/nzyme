import React from "react";
import CaptureRow from "./CaptureRow";

function CaptureConfiguration(props) {

    if (!props.tap.active) {
        return <div className="alert alert-danger">No recent data.</div>
    }

    return (
        <table className="table table-sm table-hover table-striped">
            <thead>
            <tr>
                <th>Interface</th>
                <th>Status</th>
                <th>Type</th>
                <th>Received Frames / Packets</th>
                <th>Dropped (Interface)</th>
                <th>Dropped (Buffer)</th>
            </tr>
            </thead>
            <tbody>
            {Object.keys(props.tap.captures.sort((a, b) => a.interface_name.localeCompare(b.interface_name))).map(function (key, i) {
                return <CaptureRow key={"capture-" + i} capture={props.tap.captures[i]} />
            })}
            </tbody>
        </table>
    )

}

export default CaptureConfiguration;