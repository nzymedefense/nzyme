import React from "react";
import BuiltinBanditsTable from "./BuiltinBanditsTable";
import CustomBanditsTableProxy from "./CustomBanditsTableProxy";
import HeadlineMenu from "../../../shared/HeadlineMenu";
import {MONITORING_HEADLINE_MENU_ITEMS} from "../../Dot11HeadlineMenuItems";
import ApiRoutes from "../../../../util/ApiRoutes";

export default function BanditsPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <HeadlineMenu headline={"Bandits"}
                          items={MONITORING_HEADLINE_MENU_ITEMS}
                          activeRoute={ApiRoutes.DOT11.MONITORING.BANDITS.INDEX} />
          </div>

          <div className="col-md-2">
            <a href="https://go.nzyme.org/wifi-network-monitoring" className="btn btn-secondary float-end">Help</a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Bandits</h3>

                <p className="text-muted mb-0">
                  All nzyme taps are constantly looking for known attack platforms, called <i>bandits</i>. Once a bandit
                  is detected, an alarm is raised.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Built-In Bandits</h3>

                <p className="text-muted">
                  The built-in bandits ship with nzyme by default.
                </p>

                <BuiltinBanditsTable />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Custom Bandits</h3>

                <p className="text-muted">
                  You can define and manage your own bandit definitions.
                </p>

                <CustomBanditsTableProxy />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}