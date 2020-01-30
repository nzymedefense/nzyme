import React from 'react';
import Reflux from 'reflux';
import Routes from "../../util/Routes";

class BanditIdentifiersTable extends Reflux.Component {

    constructor(props) {
        super(props);
    }

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
          <span>hi</span>
        );
    }

}

export default BanditIdentifiersTable;