import React from 'react';
import Reflux from 'reflux';
import LoadingSpinner from "../misc/LoadingSpinner";
import BanditsActions from "../../actions/BanditsActions";
import BanditsStore from "../../stores/BanditsStore";
import BanditsTableRow from "./BanditsTableRow";
import Routes from "../../util/Routes";

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

        if (bandits.length === 0) {
            return (
                <div className="alert alert-info">
                    No bandits defined yet. <a href={Routes.BANDITS.NEW} className="text-dark"><u>Create a new bandit</u></a>
                </div>
            )
        }

        return (
            <div className="row">
                <div className="col-md-12">
                    <table className="table table-sm table-hover table-striped">
                        <thead>
                        <tr>
                            <th>Name</th>
                            <th>Active</th>
                            <th>Last Contact</th>
                            <th>Created</th>
                            <th>Last Updated</th>
                            <th>ID</th>
                        </tr>
                        </thead>
                        <tbody>
                        {Object.keys(bandits).map(function (key,i) {
                            return <BanditsTableRow key={"bandit-"+key} bandit={bandits[key]} />
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }

}

export default BanditsTable;