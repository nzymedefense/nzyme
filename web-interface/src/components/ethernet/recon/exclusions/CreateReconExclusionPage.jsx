import React, {useEffect, useState} from "react";
import {Navigate, useLocation} from "react-router-dom";
import {toast} from "react-toastify";
import ApiRoutes from "../../../../util/ApiRoutes";
import usePageTitle from "../../../../util/UsePageTitle";
import useSelectedTenant from "../../../system/tenantselector/useSelectedTenant";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import ReconService from "../../../../services/ethernet/ReconService";
import ReconExclusionForm from "./ReconExclusionForm";

const reconService = new ReconService();

const prefillFromScan = (scan) => {
  const values = {
    name: "Exclude " + scan.source,
    description: "Created from " + scan.type_human_readable.toLowerCase() + " by " + scan.source + "."
  };

  if (scan.source_address) {
    values.source_cidr = scan.source_address;
  } else if (scan.source_mac) {
    values.source_mac = scan.source_mac;
  }

  return values;
}

export default function CreateReconExclusionPage() {

  usePageTitle("Create Recon Exclusion");

  const [organizationId, tenantId] = useSelectedTenant();
  const scanUuid = new URLSearchParams(useLocation().search).get("scan");

  const [initialValues, setInitialValues] = useState(scanUuid ? null : {});
  const [redirect, setRedirect] = useState(false);

  useEffect(() => {
    if (!scanUuid) {
      return;
    }

    reconService.findScan(scanUuid, organizationId, tenantId,
      (scan) => setInitialValues(prefillFromScan(scan)),
      () => setInitialValues({}));
  }, [scanUuid, organizationId, tenantId]);

  const create = (payload, onError) => {
    reconService.createExclusion(organizationId, tenantId, payload, () => {
      toast.success("Exclusion created.");
      setRedirect(true);
    }, onError);
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.ETHERNET.RECON.SCANS.EXCLUSIONS.INDEX} />
  }

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-12">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.OVERVIEW}>Ethernet</a></li>
              <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.RECON.SCANS.INDEX}>Recon</a></li>
              <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.RECON.SCANS.EXCLUSIONS.INDEX}>Exclusions</a></li>
              <li className="breadcrumb-item active" aria-current="page">Create</li>
            </ol>
          </nav>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-8">
          <h1>Create Exclusion</h1>
        </div>

        <div className="col-4 text-end">
          <a href={ApiRoutes.ETHERNET.RECON.SCANS.EXCLUSIONS.INDEX} className="btn btn-primary">Back to Exclusions</a>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-xl-12 col-xxl-8">
          <div className="card">
            <div className="card-body">
              {initialValues ?
                <ReconExclusionForm organizationId={organizationId}
                                    tenantId={tenantId}
                                    initialValues={initialValues}
                                    submitText="Create Exclusion"
                                    onSubmit={create}
                                    isCreate={true} />
                : <LoadingSpinner />}
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}
