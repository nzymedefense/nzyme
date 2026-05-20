import React from "react";
import usePageTitle from "../../util/UsePageTitle";
import LocationsTable from "./LocationsTable";

export default function LocationsOverviewPage() {

  usePageTitle("Locations");

  return (
    <>
      <div className="row">
        <div className="col-12">
          <h1>Locations</h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <h3>All Locations</h3>
              
              <LocationsTable />
            </div>
          </div>
        </div>
      </div>
    </>
  )

}