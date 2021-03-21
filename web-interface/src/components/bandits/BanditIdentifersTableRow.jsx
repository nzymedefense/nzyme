import React from 'react';

class BanditIdentifiersTableRow extends React.Component {

    render() {
        const identifier = this.props.identifier;

        return (
            <tr>
                <td>{identifier.type}</td>
                <td>{identifier.matches}</td>
                <td>
                    <span className="float-right">
                        <button className="btn btn-sm btn-danger" onClick={(e) => this.props.onDelete(e, identifier)}>Delete</button>
                    </span>
                </td>
            </tr>
        );
    }

}

export default BanditIdentifiersTableRow;