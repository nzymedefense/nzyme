import React from "react";
import CardTitleWithControls from "../../shared/CardTitleWithControls";

export default function ReconPage() {

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-12">
          <h1>Reconnaissance Activity</h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="ARP Scans" />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}