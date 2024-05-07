import React from 'react'

import numeral from 'numeral'
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import ASN from "../shared/ASN";

function DNSContactAttempsSummaryTable (props) {
  if (!props.data || !props.data.pair_summary) {
    return <GenericWidgetLoadingSpinner height={250} />
  }

  return (
        <table className="table table-sm table-hover table-striped mt-3">
            <thead>
            <tr>
              <th>Server</th>
              <th>Server ASN</th>
              <th>Contact Attempts</th>
              <th>Unique Clients</th>
            </tr>
            </thead>
            <tbody>
            {props.data.pair_summary.map(function (key, i) {
              return (
                    <tr key={'pair-' + i}>
                      <td>
                        <a href="#" className="ip-address">
                          {props.data.pair_summary[i].server}
                        </a>
                      </td>
                      <td><ASN geo={props.data.pair_summary[i].server_geo} /></td>
                      <td>{numeral(props.data.pair_summary[i].request_count).format()}</td>
                      <td>{numeral(props.data.pair_summary[i].client_count).format()}</td>
                    </tr>
              )
            })}
            </tbody>
        </table>
  )
}

export default DNSContactAttempsSummaryTable
