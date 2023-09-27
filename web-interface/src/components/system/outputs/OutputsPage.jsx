import React from "react";

function OutputsPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h1>Outputs</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>OpenSearch / Data Prepper</h3>

                <p>
                  The OpenSearch full-text search engine, together by its Dashboards component, offers capabilities for
                  detailed analysis of recorded data and triggered alerts. All data forwarded adheres to the ECS schema,
                  facilitating seamless integration with OpenSearch Security Analytics and additional plugins.
                </p>

                <table className="table table-sm table-hover table-striped">
                  <thead>
                  <tr>
                    <th>Address</th>
                    <th>Batch Size</th>
                    <th>Data</th>
                    <th>&nbsp;</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr>
                    <td>http://100.81.142.139:2021/log/ingest</td>
                    <td>50</td>
                    <td>IPv4, TCP, UDP, DNS, ARP, WiFi/802.11</td>
                    <td>
                      <a href="#">Details</a>
                    </td>
                  </tr>
                  </tbody>
                </table>

                <a href="#" className="btn btn-sm btn-secondary">Create OpenSearch Output</a>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default OutputsPage;