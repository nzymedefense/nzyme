import React from "react";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import Paginator from "../../../misc/Paginator";

export default function KnownNetworksTable(props) {

    const networks = props.networks;
    const page = props.page;
    const setPage = props.setPage;
    const perPage = props.perPage;

    if (!networks) {
        return <LoadingSpinner />
    }

    if (networks.networks.length === 0) {
        return <div className="alert alert-warning mt-3">
            No known networks discovered yet. Make sure that SSID monitoring is enabled and that networks are in range.
            Note that it will take at least 5 minutes to discover known networks after SSID monitoring is enabled.
        </div>
    }

    return (
        <React.Fragment>
            <table className="table table-sm table-hover table-striped">
                <thead>
                <tr>
                    <th>SSID</th>
                    <th>Notes</th>
                    <th>&nbsp;</th>
                    <th>&nbsp;</th>
                </tr>
                </thead>
                <tbody>

                </tbody>
            </table>

                <Paginator page={page} setPage={setPage} perPage={perPage} itemCount={networks.total}/>
        </React.Fragment>
)

}