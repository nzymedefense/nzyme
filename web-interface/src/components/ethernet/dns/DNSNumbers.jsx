import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import NumberCard from "../../presentation/NumberCard";
import ByteSizeCard from "../../presentation/ByteSizeCard";

function DNSNumbers(props) {

    if (!props.data || !props.data.traffic_summary) {
        return <LoadingSpinner />
    }

    return (
        <React.Fragment>
            <div className="col-md-3">
                <NumberCard title="Recorded DNS Packets" value={props.data.traffic_summary.total_packets} />
            </div>

            <div className="col-md-3">
                <ByteSizeCard title="Recorded DNS Traffic" value={props.data.traffic_summary.total_traffic_bytes} />
            </div>

            <div className="col-md-3">
                <NumberCard title="Recorded NXDOMAIN Responses" value={props.data.traffic_summary.total_nxdomains} />
            </div>

            <div className="col-md-3">
                <NumberCard title="Alerts Triggered" value={0} />
            </div>
        </React.Fragment>
    )

}

export default DNSNumbers;