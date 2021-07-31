import React from 'react';
import BanditsTable from "./BanditsTable";
import Routes from "../../util/Routes";
import TrackersTable from "./trackers/TrackersTable";

class BanditsPage extends React.Component {

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Bandits</h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-9">
                        <p>
                            A bandit is a description of behavior and characteristics of a device that advertises networks.
                            You can use bandit definitions to track and detect rogue actors who use mutating attributes in
                            frames (different MAC addresses, advertised SSIDs, etc.) by focusing on selected attributes that
                            do not change. The tracking mode of nzyme can be used to physically locate rogue devices identified
                            as contacts. Active tracks expire after 5 minutes.
                        </p>
                    </div>

                    <div className="col-md-3">
                        <div className="float-right">
                            <a href="https://go.nzyme.org/bandits-and-trackers" className="btn btn-primary" target="_blank" rel="noopener noreferrer">
                                Help
                            </a>
                            &nbsp;
                            <a href={Routes.BANDITS.NEW} className="btn btn-success">Create Bandit</a>
                        </div>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <BanditsTable />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h1>Trackers</h1>
                    </div>
                </div>
                <div className="row">
                    <div className="col-md-12">
                        <TrackersTable />
                    </div>
                </div>

            </div>
        )
    }

}

export default BanditsPage;