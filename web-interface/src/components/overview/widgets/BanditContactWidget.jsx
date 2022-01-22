import React from 'react'
import BanditContactWidgetTableRow from './BanditContactWidgetTableRow'

class BanditContactWidget extends React.Component {
  render () {
    const contacts = this.props.contacts

    if (!contacts || contacts.length === 0) {
      return (
                <div className="alert alert-info">
                    No contacts yet.
                </div>
      )
    }

    return (
            <div className="row">
                <div className="col-md-12">
                    <table className="table table-sm table-hover table-striped">
                        <thead>
                        <tr>
                            <th>Bandit</th>
                            <th>Active</th>
                            <th>Duration</th>
                            <th>First Seen</th>
                            <th>Last Seen</th>
                            <th>&nbsp;</th>
                        </tr>
                        </thead>
                        <tbody>
                        {Object.keys(contacts).map(function (key, i) {
                          return <BanditContactWidgetTableRow key={contacts[key].uuid} contact={contacts[key]}/>
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
    )
  }
}

export default BanditContactWidget
