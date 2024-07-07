import React from "react";

import moment from "moment";

export default function ConnectStatus(props) {

  const status = props.status;

  switch (status.connection_summary) {
    case "disabled":
      return (
          <div className="connect-status connect-status-disabled">
            <div>
              <div className="connect-status-summary">
                <i className="fa-solid fa-link-slash"></i> Disabled
              </div>
              <div className="connect-status-meta">
                This setup is not configured to connect to nzyme Connect. Please enable it and enter an API key.
              </div>
            </div>
          </div>
      )
    case "ok":
      return (
          <div className="connect-status connect-status-ok">
            <div>
              <div className="connect-status-summary">
                <i className="fa-regular fa-square-check"></i> Connected
              </div>
              <div className="connect-status-meta">
                Last Report: {moment(status.last_successful_report_submission).fromNow()}
              </div>
            </div>
          </div>
      )
    case "fail":
      return (
          <div className="connect-status connect-status-fail">
            <div>
              <div className="connect-status-summary">
                <i className="fa-solid fa-triangle-exclamation"></i> Error
              </div>
              <div className="connect-status-meta">
                The last successful connection to Connect
                was {moment(status.last_successful_report_submission).fromNow()}. Please ensure your API key is
                correct.
              </div>
            </div>
          </div>
      )
    case "never_connected":
      return (
          <div className="connect-status connect-status-fail">
            <div>
              <div className="connect-status-summary">
                <i className="fa-solid fa-triangle-exclamation"></i> Error
              </div>
              <div className="connect-status-meta">
                This setup was never able to successfully connect to Connect. Please ensure your API key is
                correct.
              </div>
            </div>
          </div>
      )
  }

}