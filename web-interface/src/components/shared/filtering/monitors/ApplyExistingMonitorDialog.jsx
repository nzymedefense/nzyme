import React, {useEffect, useState} from "react";
import useSelectedTenant from "../../../system/tenantselector/useSelectedTenant";
import MonitorsService from "../../../../services/MonitorsService";
import MonitorsTable from "../../../monitors/shared/MonitorsTable";

const monitorsService = new MonitorsService();

export default function ApplyExistingMonitorDialog({monitorType, onApply, onClose}) {

  const [organizationId, tenantId] = useSelectedTenant();

  const [monitors, setMonitors] = useState(null);

  const [page, setPage] = useState(1);
  const perPage = 15;

  useEffect(() => {
    monitorsService.findAllOfType(
      monitorType, organizationId, tenantId, perPage, (page-1)*perPage, setMonitors
    )
  }, [page, perPage])

  return (
    <React.Fragment>
      <div className="modal-backdrop fade show"></div>
      <div className="modal fade show" style={{display: "block"}}>
        <div className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
          <div className="modal-content">
            <div className="modal-header">
              <h1 className="modal-title fs-5">Apply Existing Monitor</h1>
              <button type="button" className="btn-close" onClick={onClose}></button>
            </div>
            <div className="modal-body">
              <MonitorsTable monitors={monitors}
                             page={page}
                             setPage={setPage}
                             perPage={perPage}
                             onApplyMonitor={onApply} />

              <div className="alert alert-info mt-4 mb-0">
                Applying the monitor will select all monitor taps as your tap selection.
              </div>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={onClose}>Close</button>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}