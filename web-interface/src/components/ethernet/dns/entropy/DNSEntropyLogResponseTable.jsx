import React from "react";
import DNSData from "../DNSData";
import ETLD from "../../shared/ETLD";

const COLSPAN = 8;

export default function DNSEntropyLogResponseTable(props) {

  const responses = props.responses;
  const show = props.show;

  if (!show) {
    return null;
  }

  if (!responses || responses.length === 0) {
    return (
        <tr>
          <td colSpan={COLSPAN}>No responses recorded.</td>
        </tr>
    )
  }

  return (
      <tr>
        <td colSpan={COLSPAN} style={{paddingLeft: 20, paddingRight: 20}}>
          <table className="table table-sm table-hover table-striped mb-1 mt-0">
            <thead>
            <tr>
              <th>Type</th>
              <th>Response</th>
              <th>eTLD</th>
            </tr>
            </thead>
            <tbody>
            {responses.map((response, i) => {
              return (
                  <tr key={i}>
                    <td>{response.data_type}</td>
                    <td title={response.data_value}><a href="#"><DNSData value={response.data_value}/></a></td>
                    <td title={response.data_value_etld}><ETLD etld={response.data_value_etld}/></td>
                  </tr>
              )
            })}
            </tbody>
          </table>

        </td>
      </tr>
  )

}