import React from 'react';

class EmailReceivers extends React.Component {

  render() {
    const receivers = this.props.receivers;
    const self = this;

    if (receivers && receivers.length > 0) {
      return (
          <ul>
            {Object.keys(receivers).map(function (key,i) {
              return (
                  <li key={"receiver-" + i}>
                    {receivers[key]}

                    <button className="btn btn-sm btn-link" onClick={(e) => self.props.onReceiverDelete(e, receivers[key])}>
                      <i className="fas fa-trash reports-remove-receiver" />
                    </button>
                  </li>
              )
            })}
          </ul>
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

export default EmailReceivers;