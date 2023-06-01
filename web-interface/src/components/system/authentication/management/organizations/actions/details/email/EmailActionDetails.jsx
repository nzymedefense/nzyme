import React from "react";
import EmailReceiverList from "../../forms/email/EmailReceiverList";

function EmailActionDetails(props) {

  const action = props.action;

  const config = action.configuration;

  return (
      <React.Fragment>
        <dl>
          <dt>Subject Prefix</dt>
          <dd>{config.subject_prefix}</dd>
        </dl>

        <h4>Receivers</h4>
        <EmailReceiverList receivers={config.receivers} readOnly={true} />
      </React.Fragment>
  )

}

export default EmailActionDetails;