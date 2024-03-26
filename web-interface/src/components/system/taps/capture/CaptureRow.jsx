import React from 'react'
import numeral from 'numeral'
import ApiRoutes from "../../../../util/ApiRoutes";
import {sanitizeHtml} from "../../../../util/Tools";

function CaptureRow (props) {
  const c = props.capture
  const tap = props.tap;

  return (
    <tr>
      <td>{c.interface_name}</td>
      <td>
        {c.is_running ? <span className="badge bg-success">Running</span> : <span className="badge bg-danger">Failing</span>}
      </td>
      <td>{c.capture_type}</td>
      <td>
        <a href={ApiRoutes.SYSTEM.TAPS.METRICDETAILS(tap.uuid, "gauge", "captures." + sanitizeHtml(c.interface_name) + ".received")}>
         {numeral(c.received).format('0,0')}
       </a>
      </td>
      <td>
        <a href={ApiRoutes.SYSTEM.TAPS.METRICDETAILS(tap.uuid, "gauge", "captures." + sanitizeHtml(c.interface_name) + ".dropped_if")}>
          {numeral(c.dropped_interface).format('0,0')}
        </a>
      </td>
      <td>
        <a href={ApiRoutes.SYSTEM.TAPS.METRICDETAILS(tap.uuid, "gauge", "captures." + sanitizeHtml(c.interface_name) + ".dropped_buffer")}>
          {numeral(c.dropped_buffer).format('0,0')}
        </a>
      </td>
    </tr>
)
}

export default CaptureRow
