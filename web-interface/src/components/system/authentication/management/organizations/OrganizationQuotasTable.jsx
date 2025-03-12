import React from "react";
import LoadingSpinner from "../../../../misc/LoadingSpinner";

import numeral from "numeral";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";

const authenticationManagementService = new AuthenticationManagementService();

export default function OrganizationQuotasTable(props) {

  const organization = props.organization;
  const quotas = props.quotas;

  const TEST_setQuota = () => {
    authenticationManagementService.setQuotaOfOrganization(organization.id, "TAPS", 9001, () => {}, () => {})
  }

  if (!quotas) {
    return <LoadingSpinner />
  }

  return (
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
                <td className={q.quota == null ? "text-muted" : ""}>{numeral(q.use).format("0,0")}</td>
                <td>
                  {q.quota != null ? numeral(q.quota).format("0,0") :
                      <span className="text-success">No Quota / Unlimited</span>}
                </td>
                <td><a href="#" onClick={TEST_setQuota}>Configure Quota</a></td>
              </tr>
          )
        })}
        </tbody>
      </table>
  )

}