import React from 'react'
import DeleteIdentifierButton from './management/identifiers/DeleteIdentifierButton'

class BanditIdentifiersTableRow extends React.Component {
  render () {
    const identifier = this.props.identifier

    return (
            <tr>
                <td>{identifier.type}</td>
                <td>{identifier.matches}</td>
                <td>
                    <span className="float-right">
                        <DeleteIdentifierButton 
                         bandit={this.props.bandit}
                         trackers={this.props.trackers}
                         identifier={identifier}
                         setBandit={this.props.setBandit} />
                    </span>
                </td>
            </tr>
    )
  }
}

export default BanditIdentifiersTableRow
