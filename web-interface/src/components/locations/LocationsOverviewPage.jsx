import React from "react";
import usePageTitle from "../../util/UsePageTitle";
import LocationsTable from "./LocationsTable";
import CardTitleWithControls from "../shared/CardTitleWithControls";

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
              <CardTitleWithControls title="All Locations"
                                     helpLink="https://go.nzyme.org/locations"
                                     slim={true} />

              <LocationsTable />
            </div>
          </div>
        </div>
      </div>
    </>
  )

}