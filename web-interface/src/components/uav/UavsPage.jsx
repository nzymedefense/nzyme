import React, {useContext, useEffect, useState} from "react";
import UavService from "../../services/UavService";
import {Presets} from "../shared/timerange/TimeRange";
import {TapContext} from "../../App";
import {disableTapSelector, enableTapSelector} from "../misc/TapSelector";
import AlphaFeatureAlert from "../shared/AlphaFeatureAlert";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import UavsTable from "./UavsTable";
import OrganizationAndTenantSelector from "../shared/OrganizationAndTenantSelector";
import SelectedOrganizationAndTenant from "../shared/SelectedOrganizationAndTenant";

const uavService = new UavService();

export default function UavsPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [uavs, setUavs] = useState(null);
  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [page, setPage] = useState(1);
  const perPage = 25;

  const [organizationId, setOrganizationId] = useState(null);
  const [tenantId, setTenantId] = useState(null);
  const [tenantSelected, setTenantSelected] = useState(false);


  useEffect(() => {
    setUavs(null);
    if (organizationId && tenantId) {
      uavService.findAll(setUavs, organizationId, tenantId, timeRange, selectedTaps, perPage, (page-1)*perPage);
    }
  }, [selectedTaps, timeRange, page, organizationId, tenantId]);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  const onOrganizationChange = (uuid) => {
    setOrganizationId(uuid);
  }

  const onTenantChange = (uuid) => {
    setTenantId(uuid);

    if (uuid) {
      setTenantSelected(true);
    }
  }

  const resetTenantAndOrganization = () => {
    setOrganizationId(null);
    setTenantId(null);
  }

  if (!organizationId || !tenantId) {
    return <OrganizationAndTenantSelector onOrganizationChange={onOrganizationChange}
                                          onTenantChange={onTenantChange}
                                          autoSelectCompleted={tenantSelected} />
  }

  return (
      <React.Fragment>
        <AlphaFeatureAlert />

        <div className="row">
          <div className="col-md-10">
            <h1>Unmanned Aerial Vehicles (UAVs)</h1>
          </div>

          <div className="col-md-2 text-end">
            <a href="https://go.nzyme.org/uavs" className="btn btn-secondary">Help</a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="UAVs"
                                       timeRange={timeRange}
                                       setTimeRange={setTimeRange} />

                <p className="text-muted mt-0">The table shows the most recent recorded values. More details and a history of values is available
                on the UAV details pages.</p>

                <SelectedOrganizationAndTenant
                    organizationId={organizationId}
                    tenantId={tenantId}
                    onReset={resetTenantAndOrganization} />

                <UavsTable uavs={uavs} page={page} perPage={perPage} setPage={setPage} />
              </div>
            </div>
          </div>
        </div>

      </React.Fragment>
  )

}