import React from 'react'
import Routes from '../../util/ApiRoutes'
import BanditIdentifiersTableRow from './BanditIdentifersTableRow'

class BanditIdentifiersTable extends React.Component {
  render () {
    const self = this

    const bandit = this.props.bandit
    const identifiers = bandit.identifiers

    if (!identifiers || identifiers.length === 0) {
      return (
                <div className="alert alert-warning">
                    This bandit has no identifiers configured yet. <a href={Routes.BANDITS.NEW_IDENTIFIER(bandit.uuid)} className="text-dark"><u>Create a new identifier</u></a>
                </div>
      )
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
                        {Object.keys(identifiers).map(function (key, i) {
                          return <BanditIdentifiersTableRow
                            key={identifiers[key].uuid}
                            identifier={identifiers[key]}
                            bandit={bandit}
                            trackers={self.props.trackers}
                            setBandit={self.props.setBandit}
                          />
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
    )
  }
}

export default BanditIdentifiersTable
