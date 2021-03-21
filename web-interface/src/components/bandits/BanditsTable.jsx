import React from 'react';
import LoadingSpinner from "../misc/LoadingSpinner";
import BanditsTableRow from "./BanditsTableRow";
import Routes from "../../util/Routes";
import BanditsService from "../../services/BanditsService";

class BanditsTable extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            bandits: undefined
        }

        this.banditsService = new BanditsService();
        this.banditsService.findAll = this.banditsService.findAll.bind(this);
    }

    componentDidMount() {
        this.banditsService.findAll();

        const self = this;
        setInterval(function () {
            self.banditsService.findAll();
        }, 5000);
    }

    render() {
        if (!this.state.bandits) {
            return <LoadingSpinner />
        }

        if (this.state.bandits.length === 0) {
            return (
                <div className="alert alert-info">
                    No bandits defined yet. <a href={Routes.BANDITS.NEW} className="text-dark"><u>Create a new bandit</u></a>
                </div>
            )
        }

        const self = this;
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
                        </tr>
                        </thead>
                        <tbody>
                        {Object.keys(this.state.bandits).map(function (key,i) {
                            return <BanditsTableRow key={"bandit-"+i} bandit={self.state.bandits[key]} />
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }

}

export default BanditsTable;