import LoadingSpinner from "../../misc/LoadingSpinner";
import React from "react";
import Paginator from "../../misc/Paginator";
import {truncate} from "../../../util/Tools";
import numeral from "numeral";
import moment from "moment";
import ApiRoutes from "../../../util/ApiRoutes";

export default function GNSSMonitoringRulesTable({rules, page, setPage, perPage}) {

  if (!rules) {
    return <LoadingSpinner />
  }

  if (rules.count === 0) {
    return <div className="alert alert-info mb-0">
      No GNSS monitoring rules configured.
    </div>
  }

  return (
      <>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Name</th>
            <th>Description</th>
            <th>Conditions</th>
            <th>Taps</th>
            <th>Updated At</th>
          </tr>
          </thead>
          <tbody>
          {rules.rules.map((rule, i) => {
            return (
                <tr key={i}>
                  <td><a href={ApiRoutes.GNSS.MONITORING.RULES.DETAILS(rule.uuid)}>{rule.name}</a></td>
                  <td>
                    {rule.description ? truncate(rule.description, 100, true)
                        : <span className="text-muted">n/a</span>}
                  </td>
                  <td>{numeral(rule.conditions_count).format("0,0")}</td>
                  <td>{rule.taps ? numeral(rule.taps.length).format("0,0") : "All"}</td>
                  <td title={moment(rule.updated_at).toISOString()}>{moment(rule.updated_at).fromNow()}</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={rules.total} perPage={perPage} setPage={setPage} page={page} />
      </>
  )

}