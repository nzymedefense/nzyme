import React from "react";

function MacAddressContextPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-8">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item">Context</li>
                <li className="breadcrumb-item active" aria-current="page">MAC Addresses</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row">
          <div className="col-md-8">
            <h1>
              Context: MAC Addresses
            </h1>
          </div>

          <div className="col-md-4">
            <span className="float-end">
              <a className="btn btn-primary" href="#">Create Context</a>
            </span>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>All MAC Addresses with Context</h3>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default MacAddressContextPage;