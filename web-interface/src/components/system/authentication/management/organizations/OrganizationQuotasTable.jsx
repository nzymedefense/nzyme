import React, {useState} from "react";
import LoadingSpinner from "../../../../misc/LoadingSpinner";

import numeral from "numeral";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import QuotaConfigurationModal from "../shared/QuotaConfigurationModal";
import WithExactRole from "../../../../misc/WithExactRole";

const authenticationManagementService = new AuthenticationManagementService();

export default function OrganizationQuotasTable(props) {

  const organization = props.organization;
  const quotas = props.quotas;
  const onUpdate = props.onUpdate;

  const onSave = (type, value, onSuccess, onError) => {
    authenticationManagementService.setQuotaOfOrganization(organization.id, type, value, onSuccess, onError);
  }

  const aboveQuotaWarning = (q) => {
    if (q.quota == null || q.use <= q.quota) {
      return null;
    }

    return (
        <span className="text-danger text-bold"><i className="fa fa-warning" /> Quota Exceeded</span>
    )
  }

  if (!quotas) {
    return <LoadingSpinner />
  }

  return (
    <React.Fragment>
      <table className="table table-sm table-hover table-striped mb-0">
        <thead>
        <tr>
          <th>Quota Type</th>
          <th>Used</th>
          <th>Quota</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {quotas.map((q, i) => {
          return (
              <tr key={i}>
                <td>{q.type_human_readable}</td>
                <td className={q.quota == null ? "text-muted" : ""}>
                  {numeral(q.use).format("0,0")} {aboveQuotaWarning(q)}
                </td>
                <td>
                  {q.quota != null ? numeral(q.quota).format("0,0") :
                      <span className="text-success">No Quota / Unlimited</span>}
                </td>
                <td>
                  <WithExactRole role="SUPERADMIN">
                    <a href="#"
                       data-bs-toggle="modal"
                       data-bs-target={"#quota-config-" + q.type}>
                      Configure Quota
                    </a>

                    <QuotaConfigurationModal quota={q} onSubmit={onSave} onFinish={onUpdate} />
                  </WithExactRole>
                </td>
              </tr>
          )
        })}
        </tbody>
      </table>

    </React.Fragment>
  )

}