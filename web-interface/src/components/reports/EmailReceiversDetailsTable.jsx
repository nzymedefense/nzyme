import React from 'react'
import DeleteEmailReceiverButton from './DeleteEmailReceiverButton'

class EmailReceiversDetailsTable extends React.Component {

  render () {
    const receivers = this.props.report.email_receivers
    const self = this

    if (receivers && receivers.length > 0) {
      return (
                <table className="table table-sm table-hover table-striped">
                    <thead>
                    <tr>
                        <th>Receiver</th>
                        <th>Type</th>
                        <th>&nbsp;</th>
                    </tr>
                    </thead>
                    <tbody>
                    {Object.keys(receivers).map(function (key, i) {
                      return (
                            <tr key={'receiver-' + i}>
                                <td>{receivers[key]}</td>
                                <td>Email</td>
                                <td>
                                    <span className="float-right">
                                        <DeleteEmailReceiverButton
                                          address={receivers[key]}
                                          report={self.props.report}
                                          reportsService={self.props.reportsService}
                                          setReport={self.props.setReport} />
                                    </span>
                                </td>
                            </tr>
                      )
                    })}
                    </tbody>
                </table>
      )
    } else {
      return (
                <div className="alert alert-warning">
                    No email receivers configured
                </div>
      )
    }
  }
}

export default EmailReceiversDetailsTable
