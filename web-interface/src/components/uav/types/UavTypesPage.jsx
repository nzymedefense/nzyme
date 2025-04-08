import React, {useEffect, useState} from "react";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import OrganizationAndTenantSelector from "../../shared/OrganizationAndTenantSelector";
import SelectedOrganizationAndTenant from "../../shared/SelectedOrganizationAndTenant";
import WithPermission from "../../misc/WithPermission";
import BuiltInTypesTable from "./BuiltInTypesTable";
import ApiRoutes from "../../../util/ApiRoutes";
import CustomTypesTable from "./CustomTypesTable";
import UavService from "../../../services/UavService";
import {notify} from "react-notify-toast";

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

  const [revision, setRevision] = useState(new Date());

  useEffect(() => {
    setCustomTypes(null);

    if (organizationId && tenantId) {
      uavService.findAllCustomTypes(
        setCustomTypes, organizationId, tenantId, customTypesPerPage, (customTypesPage-1)*customTypesPerPage
      );

      uavService.findAllBuiltInTypes(
        setBuiltInTypes, organizationId, tenantId, customTypesPerPage, (customTypesPage-1)*customTypesPerPage
      );
    }
  }, [organizationId, tenantId, customTypesPage, revision]);

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

  const onDeleteCustomType = (e, uuid) => {
    e.preventDefault();

    if (!confirm("Really delete custom type?")) {
      return;
    }

    uavService.deleteCustomType(uuid, organizationId, tenantId, () => {
      notify.show("Custom UAV type deleted.", "success");
      setRevision(new Date());
    })
  }

  if (!organizationId || !tenantId) {
    return <OrganizationAndTenantSelector onOrganizationChange={onOrganizationChange}
                                          onTenantChange={onTenantChange}
                                          autoSelectCompleted={tenantSelected} />
  }

  return (
    <React.Fragment>
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
        <div className="col-xl-12 col-xxl-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Custom Types" />

              <p className="text-muted mt-0">
                You can use custom types to associate UAVs with additional details, such as name, make, or model. These
                types extend existing built-in types, allowing for more precise identification. A common use case is
                assigning names or extra information to frequently detected UAVs, such as those in your own fleet or
                drones that regularly appear in your area.
              </p>

              <CustomTypesTable types={customTypes}
                                page={customTypesPage}
                                onDeleteType={onDeleteCustomType}
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
        <div className="col-xl-12 col-xxl-6">
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