import React from "react";
import SubsystemsConfiguration from "../../shared/SubsystemsConfiguration";
import SystemService from "../../../services/SystemService";

const systemService = new SystemService();

export default function SubsystemsPage() {

  return (
    <div>
      <div className="row">
        <div className="col-md-12">
          <h1>Subsystems</h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-xl-12 col-xxl-8">
          <div className="card">
            <div className="card-body">
              <h3>Subsystem Configuration</h3>

              <p>
                This setting governs subsystem availability across the entire nzyme cluster. <strong>You can also
                configure subsystems at the organization and tenant levels</strong>, with the higher-level configuration
                taking precedence.
              </p>

              <SubsystemsConfiguration dbUpdateCallback={systemService.updateSubsystemsConfiguration} />
            </div>
          </div>
        </div>
      </div>

    </div>
  )

}