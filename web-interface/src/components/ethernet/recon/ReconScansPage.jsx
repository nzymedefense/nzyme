import React from "react";
import SectionMenuBar from "../../shared/SectionMenuBar";
import ApiRoutes from "../../../util/ApiRoutes";
import usePageTitle from "../../../util/UsePageTitle";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {RECON_MENU_ITEMS} from "./ReconMenuItems";

export default function ReconScansPage() {

  usePageTitle("Ethernet Recon Scans");

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-10">
          <SectionMenuBar items={RECON_MENU_ITEMS}
                          activeRoute={ApiRoutes.ETHERNET.RECON.SCANS.INDEX}/>
        </div>

        <div className="col-md-2">
          <a href="https://go.nzyme.org/ethernet-recon" className="btn btn-secondary float-end">Help</a>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Scans" />

              <p className="text-muted">
                Hosts that are probing your network, such as port scans, host sweeps or ARP scans.
              </p>

              <div className="alert alert-info mb-0">
                Scan detection is not implemented yet.
              </div>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}
