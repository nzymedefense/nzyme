import React from "react";
import SmtpIntegration from "./smtp/SmtpIntegration";

function IntegrationsPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h1>Integrations</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>SMTP / Email</h3>

                <p>
                  With the configuration of an SMTP connection, nzyme gains the capability to send emails for alerts,
                  system events, and other trigger actions.
                </p>

                <SmtpIntegration />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default IntegrationsPage;