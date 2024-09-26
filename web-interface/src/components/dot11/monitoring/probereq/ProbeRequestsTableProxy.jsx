import React, {useContext, useEffect, useState} from "react";
import {UserContext} from "../../../../App";
import Dot11Service from "../../../../services/Dot11Service";
import OrganizationAndTenantSelector from "../../../shared/OrganizationAndTenantSelector";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../../services/AuthenticationManagementService";
import ApiRoutes from "../../../../util/ApiRoutes";
import ProbeRequestsTable from "./ProbeRequestsTable";
import {notify} from "react-notify-toast";

const authenticationManagementService = new AuthenticationManagementService();
const dot11Service = new Dot11Service();

export default function ProbeRequestsTableProxy() {

  const user = useContext(UserContext);

  const [probeRequests, setProbeRequests] = useState(null);

  const [organizationUUID, setOrganizationUUID] = useState(null);
  const [tenantUUID, setTenantUUID] = useState(null);
  const [tenantSelected, setTenantSelected] = useState(false);

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);

  const [revision, setRevision] = useState(new Date());

  const perPage = 25;
  const [page, setPage] = useState(1);

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

      setProbeRequests(null);
      dot11Service.findAllMonitoredProbeRequests(
          organizationUUID, tenantUUID, perPage, (page-1)*perPage, setProbeRequests
      )
    }
  }, [organizationUUID, tenantUUID, page, revision]);

  const onDelete = (e, id) => {
    e.preventDefault();

    if (!confirm("Really delete monitored probe request?")) {
      return;
    }

    dot11Service.deleteMonitoredProbeRequest(id, () => {
      notify.show('Monitored probe request deleted.', 'success');
      setRevision(new Date());
    });
  }

  if (!organizationUUID || !tenantUUID) {
    return <OrganizationAndTenantSelector
        onOrganizationChange={onOrganizationChange}
        onTenantChange={onTenantChange}
        autoSelectCompleted={tenantSelected} />
  }

  if ((user.is_superadmin || user.is_orgadmin) && (!organization || !tenant)) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        {user.is_superadmin || user.is_orgadmin ? <div className="mb-2"><strong>Organization:</strong> {organization.name}, <strong>Tenant:</strong> {tenant.name} <a href="#" onClick={resetOrganizationAndTenant}>Change</a></div> : null}

        <ProbeRequestsTable probeRequests={probeRequests}
                            onDelete={onDelete}
                            page={page}
                            setPage={setPage}
                            perPage={perPage} />

        <a href={ApiRoutes.DOT11.MONITORING.PROBE_REQUESTS.CREATE(organizationUUID, tenantUUID)} className="btn btn-sm btn-secondary">Create Monitored Probe Request</a>
      </React.Fragment>
  )

}