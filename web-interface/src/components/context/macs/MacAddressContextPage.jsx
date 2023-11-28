import React from "react";
import ApiRoutes from "../../../util/ApiRoutes";
import MacAddressContextTable from "./MacAddressContextTable";
import WithPermission from "../../misc/WithPermission";

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
              <WithPermission permission="mac_aliases_manage">
                <a className="btn btn-primary" href={ApiRoutes.CONTEXT.MAC_ADDRESSES.CREATE}>Create Context</a>
              </WithPermission>
            </span>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>All MAC Addresses with Context</h3>

                <MacAddressContextTable />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default MacAddressContextPage;