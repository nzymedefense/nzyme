import React, {useContext, useEffect, useState} from "react";
import {UserContext} from "../../../../App";
import OrganizationAndTenantSelector from "../../../shared/OrganizationAndTenantSelector";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../../services/AuthenticationManagementService";
import Dot11Service from "../../../../services/Dot11Service";
import KnownNetworksTable from "./KnownNetworksTable";
import SSIDMonitoringConfiguration from "./SSIDMonitoringConfiguration";
import {notify} from "react-notify-toast";

const authenticationManagementService = new AuthenticationManagementService();
const dot11Service = new Dot11Service();

export default function SSIDMonitoringProxy(props) {

  const user = useContext(UserContext);

  const [knownNetworks, setKnownNetworks] = useState(null);

  const [organizationUUID, setOrganizationUUID] = useState(null);
  const [tenantUUID, setTenantUUID] = useState(null);

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [tenantSelected, setTenantSelected] = useState(false);

  const [revision, setRevision] = useState(new Date());

  const perPage = 25;
  const [page, setPage] = useState(1);

  const onChange = () => {
    setRevision(new Date());
  }

  const onOrganizationChange = (organizationUUID) => {
    if (organizationUUID) {
      setOrganizationUUID(organizationUUID)
    }
  }

  const onTenantChange = (tenantUUID) => {
    if (tenantUUID) {
      setTenantUUID(tenantUUID);
    }

    if (tenantUUID) {
      setTenantSelected(true);
    }
  }

  const resetOrganizationAndTenant = (e) => {
    e.preventDefault();

    setOrganizationUUID(null);
    setTenantUUID(null);
  }

  useEffect(() => {
    if (organizationUUID && tenantUUID) {
      if (user.is_superadmin || user.is_orgadmin) {
        authenticationManagementService.findOrganization(organizationUUID, setOrganization);
        authenticationManagementService.findTenantOfOrganization(organizationUUID, tenantUUID, setTenant);
      }

      setKnownNetworks(null);
      dot11Service.findAllKnownNetworks(
          organizationUUID, tenantUUID, perPage, (page-1)*perPage, setKnownNetworks
      )
    }
  }, [organizationUUID, tenantUUID, page, revision]);

  const onDeleteAll = (e) => {
    e.preventDefault();

    if (!confirm("Really delete all known networks? Each will reappear as unapproved network " +
        "next time nzyme records it.")) {
      return;
    }

    dot11Service.deleteAllKnownNetworks(organizationUUID, tenantUUID, () => {
      notify.show('All known networks deleted.', 'success');
      onChange();
    });
  }

  if (!organizationUUID || !tenantUUID) {
    return (
        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <OrganizationAndTenantSelector
                    onOrganizationChange={onOrganizationChange}
                    onTenantChange={onTenantChange}
                    autoSelectCompleted={tenantSelected} />
              </div>
            </div>
          </div>
        </div>
    )
  }

  if ((user.is_superadmin || user.is_orgadmin) && (!organization || !tenant)) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Monitor Configuration</h3>

                <SSIDMonitoringConfiguration organizationUUID={organizationUUID}
                                             tenantUUID={tenantUUID} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3 style={{display: "inline-block"}}>Known SSIDs/Networks</h3>

                <button className="btn btn-danger btn-sm float-end" onClick={onDeleteAll}>Delete All</button>
                <br style={{clear: "both"}}/>

                {user.is_superadmin || user.is_orgadmin ? <div className="mb-2">
                  <strong>Organization:</strong> {organization.name}, <strong>Tenant:</strong> {tenant.name}
                  {' '}<a href="#" onClick={resetOrganizationAndTenant}>Change</a></div> : null}

                <KnownNetworksTable networks={knownNetworks}
                                    organizationUUID={organizationUUID}
                                    tenantUUID={tenantUUID}
                                    onChange={onChange}
                                    page={page}
                                    setPage={setPage}
                                    perPage={perPage}/>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )
}