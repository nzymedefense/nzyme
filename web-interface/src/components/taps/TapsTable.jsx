import React from "react";
import LoadingSpinner from "../misc/LoadingSpinner";
import TapRow from "./TapRow";


function TapsTable(props) {

    if (!props.taps) {
        return <LoadingSpinner />
    }

    const taps = props.taps.taps;

    return (
        <div className="row">
            <div className="col-md-12">
                <table className="table table-sm table-hover table-striped">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Throughput</th>
                        <th>Total data processed</th>
                        <th>CPU Load</th>
                        <th>Memory Used</th>
                        <th>Last Seen</th>
                    </tr>
                    </thead>
                    <tbody>
                    {Object.keys(taps).map(function (key, i) {
                        return <TapRow key={"tap-" + i} tap={taps[i]} />
                    })}
                    </tbody>
                </table>
            </div>
        </div>
    )

}

export default TapsTable;