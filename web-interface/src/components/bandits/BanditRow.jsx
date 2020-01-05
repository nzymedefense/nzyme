import React from 'react';
import Reflux from 'reflux';
import moment from "moment";
import Routes from "../../util/Routes";

class BanditRow extends Reflux.Component {

    constructor(props) {
        super(props);

        this.state = {
            bandit: this.props.bandit
        }
    }

    render() {
        const bandit = this.state.bandit;

        return (
            <tr>
                <td><a href={Routes.BANDITS.SHOW(bandit.uuid)}>{bandit.name}</a></td>
                <td>never</td>
                <td title={moment(bandit.created_at).format()}>{moment(bandit.created_at).fromNow()}</td>
                <td title={moment(bandit.updated_at).format()}>{moment(bandit.updated_at).fromNow()}</td>
                <td>{bandit.uuid.substr(0, 8)}</td>
            </tr>
        );
    }

}

export default BanditRow;