import React, {useEffect, useState} from "react";
import AlphaFeatureAlert from "../../shared/AlphaFeatureAlert";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import OrganizationAndTenantSelector from "../../shared/OrganizationAndTenantSelector";
import SelectedOrganizationAndTenant from "../../shared/SelectedOrganizationAndTenant";
import WithPermission from "../../misc/WithPermission";
import BuiltInTypesTable from "./BuiltInTypesTable";
import ApiRoutes from "../../../util/ApiRoutes";
import CustomTypesTable from "./CustomTypesTable";
import UavService from "../../../services/UavService";

const uavService = new UavService();

export default function UavTypesPage() {

  const [organizationId, setOrganizationId] = useState(null);
  const [tenantId, setTenantId] = useState(null);
  const [tenantSelected, setTenantSelected] = useState(false);

  const [builtInTypes, setBuiltInTypes] = useState(null);
  const [builtInTypesPage, setBuiltInTypesPage] = useState(1);
  const builtInTypesPerPage = 25;

  const [customTypes, setCustomTypes] = useState(null);
  const [customTypesPage, setCustomTypesPage] = useState(1);
  const customTypesPerPage = 25;

  useEffect(() => {
    setCustomTypes(null);

    if (organizationId && tenantId) {
      uavService.findAllCustomTypes(setCustomTypes, organizationId, tenantId, customTypesPerPage, (customTypesPage-1)*customTypesPerPage);
    }
  }, [organizationId, tenantId, customTypesPage]);

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
          <h1>Unmanned Aerial Vehicle (UAV) Types</h1>
        </div>

        <div className="col-md-2 text-end">
          <a href="https://go.nzyme.org/uav-types" className="btn btn-secondary">Help</a>
        </div>
      </div>

      <SelectedOrganizationAndTenant
        organizationId={organizationId}
        tenantId={tenantId}
        onReset={resetTenantAndOrganization} />

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Custom Types" />

              <p className="text-muted mt-0">
                You can use custom types to associate UAVs with additional details such as name, make,
                or model. These custom types extend any existing built-in types.
              </p>

              <CustomTypesTable types={customTypes}
                                page={customTypesPage}
                                setPage={setCustomTypesPage}
                                perPage={customTypesPerPage} />

              <WithPermission permission="uav_monitoring_manage">
                <a href={ApiRoutes.UAV.TYPES.CREATE(organizationId, tenantId)} className="btn btn-sm btn-secondary mt-3">
                  Create Custom Type
                </a>
              </WithPermission>
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Built-In Types" />

              <p className="text-muted mt-0">
                Built-in types, provided through nzyme Connect, cover a wide range of known UAV models.
              </p>

              <BuiltInTypesTable types={builtInTypes}
                                 page={builtInTypesPage}
                                 setPage={setBuiltInTypesPage}
                                 perPage={builtInTypesPerPage} />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}