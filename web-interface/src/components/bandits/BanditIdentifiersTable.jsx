import React from 'react';
import Reflux from 'reflux';
import Routes from "../../util/Routes";
import BanditIdentifiersTableRow from "./BanditIdentifersTableRow";
import BanditsStore from "../../stores/BanditsStore";
import BanditsActions from "../../actions/BanditsActions";
import {notify} from "react-notify-toast";

class BanditIdentifiersTable extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = BanditsStore;
    }

    render() {
        const self = this;

        const bandit = this.props.bandit;
        const identifiers = bandit.identifiers;

        if (!identifiers || identifiers.length === 0) {
            return (
                <div className="alert alert-warning">
                    This bandit has no identifiers configured yet. <a href={Routes.BANDITS.NEW_IDENTIFIER(bandit.uuid)} className="text-dark"><u>Create a new identifier</u></a>
                </div>
            );
        }

        return (
            <div className="row">
                <div className="col-md-12">
                    <table className="table table-sm table-hover table-striped">
                        <thead>
                        <tr>
                            <th>Type</th>
                            <th>Matches</th>
                            <th>&nbsp;</th>
                        </tr>
                        </thead>
                        <tbody>
                        {Object.keys(identifiers).map(function (key,i) {
                            return <BanditIdentifiersTableRow key={identifiers[key].uuid} identifier={identifiers[key]} onDelete={(e) => self.props.onDeleteIdentifier(e, identifiers[key].uuid)} />
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }

}

export default BanditIdentifiersTable;