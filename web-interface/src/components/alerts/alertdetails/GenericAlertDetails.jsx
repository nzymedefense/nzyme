import React from "react";

function GenericAlertDetails(props) {

  const alert = props.alert;

  return (
      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <h3>Details</h3>

              <pre className="mb-0">
                {JSON.stringify(alert.attributes, null, 2)}
              </pre>
            </div>
          </div>
        </div>
      </div>
  )

}

export default GenericAlertDetails;