import React from 'react';
import Reflux from 'reflux';
import LoadingSpinner from "../misc/LoadingSpinner";
import BanditsActions from "../../actions/BanditsActions";
import BanditsStore from "../../stores/BanditsStore";
import BanditRow from "./BanditRow";

class BanditsTable extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = BanditsStore;

        this.state = {
            bandits: undefined
        }
    }

    componentDidMount() {
        BanditsActions.findAll();
    }

    render() {
        if (!this.state.bandits) {
            return <LoadingSpinner />
        }

        const bandits = this.state.bandits;

        return (
            <div className="row">
                <div className="col-md-12">
                    <table className="table table-sm table-hover table-striped">
                        <thead>
                        <tr>
                            <th>Name</th>
                            <th>Last Contact</th>
                            <th>Created</th>
                            <th>Last Updated</th>
                            <th>ID</th>
                        </tr>
                        </thead>
                        {Object.keys(bandits).map(function (key,i) {
                            return <BanditRow bandit={bandits[key]} />
                        })}
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }

}

export default BanditsTable;