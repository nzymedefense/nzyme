import React, { useEffect, useState } from 'react'
import TapsService from '../../../services/TapsService'
import TapsTable from './TapsTable'
import ApiRoutes from "../../../util/ApiRoutes";
import useSelectedTenant from "../tenantselector/useSelectedTenant";

const tapsService = new TapsService()

function TapsPage () {

  const [organizationId, tenantId] = useSelectedTenant();

  const [taps, setTaps] = useState(null)

  useEffect(() => {
    tapsService.findAllTaps(organizationId, tenantId, setTaps)
  }, [organizationId, tenantId])

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-10">
          <h1>Taps</h1>
        </div>
        <div className="col-md-2">
          <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.CREATE(organizationId, tenantId)}
             className="btn btn-primary float-end">
            Add Tap
          </a>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <TapsTable taps={taps} />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )
}

export default TapsPage
