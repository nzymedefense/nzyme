import React from 'react';
import Reflux from 'reflux';
import moment from "moment";
import Routes from "../../util/Routes";

class BanditIdentifersTableRow extends Reflux.Component {

    constructor(props) {
        super(props);

        this.state = {
            identifier: this.props.identifier
        }
    }

    render() {
        const identifier = this.state.identifier;

        return (
            <tr>
                <td>{identifier.type}</td>
                <td>{identifier.matches}</td>
                <td>
                    <a className="btn btn-sm btn-secondary">Edit</a>&nbsp;
                    <a className="btn btn-sm btn-danger">Delete</a>
                </td>
            </tr>
        );
    }

}

export default BanditIdentifersTableRow;