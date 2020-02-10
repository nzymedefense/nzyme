import React from 'react';
import Reflux from 'reflux';
import Routes from "../../util/Routes";
import BanditIdentifersTableRow from "./BanditIdentifersTableRow";

class BanditIdentifiersTable extends Reflux.Component {

    render() {
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
                        {Object.keys(identifiers).map(function (key,i) {
                            return <BanditIdentifersTableRow identifier={identifiers[key]} />
                        })}
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }

}

export default BanditIdentifiersTable;