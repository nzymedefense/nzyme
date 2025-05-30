import React from "react";

export default function WebhookActionDetails(props) {

  const action = props.action;

  const config = action.configuration;

  return (
      <React.Fragment>
        <dl className="mb-0">
          <dt>Webhook URL</dt>
          <dd>{config.url}</dd>
          <dt>Allow Insecure Connections</dt>
          <dd>{config.allow_insecure ? "Yes" : "No"}</dd>
          <dt>Bearer Token</dt>
          <dd>{config.bearer_token != null && config.bearer_token !== "" ? <em>[configured / encrypted]</em> : "None"}</dd>
        </dl>
      </React.Fragment>
  )
}