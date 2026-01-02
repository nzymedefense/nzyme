import React from 'react';
import GenericWidgetLoadingSpinner from "../widgets/GenericWidgetLoadingSpinner";
import moment from "moment";
import numeral from "numeral";
import GNSSDegrees from "./GNSSDegrees";
import ApiRoutes from "../../util/ApiRoutes";
import GNSSPRN from "./GNSSPRN";
import GNSSCn0 from "./GNSSCn0";
import GNSSMultipathIndicator from "./GNSSMultipathIndicator";

export default function GNSSSatellitesInViewTable(props) {

  const satellites = props.satellites;

  if (satellites == null) {
    return <GenericWidgetLoadingSpinner height={500} />
  }

  if (satellites.satellites.length === 0) {
    return <div className="alert alert-info mb-0">No satellites in view.</div>
  }

  return (
    <React.Fragment>
      <div>
        <strong>Total: </strong> {numeral(satellites.satellites.length).format(0,0)}
      </div>

      <table className="table table-sm table-hover table-striped mt-2">
        <thead>
        <tr>
          <th>Constellation</th>
          <th>PRN</th>
          <th>Fix</th>
          <th>Last Seen</th>
          <th title="Carrier-to-Noise Density Ratio">C/N&#8320;</th>
          <th>Doppler Shift</th>
          <th>Multipath</th>
          <th>Azimuth</th>
          <th>Elevation</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {satellites.satellites.map((sat, i) => {
          return (
            <tr key={i}>
              <td>{sat.constellation}</td>
              <td><GNSSPRN constellation={sat.constellation} prn={sat.prn} /></td>
              <td>{sat.used_for_fix ? <i className="fa fa-check-circle text-success" title="Used for fix." />
                : <i className="fa fa-cancel text-muted" title="Not used for fix." />}</td>
              <td title={moment(sat.last_seen).format()}>{moment(sat.last_seen).fromNow()}</td>
              <td><GNSSCn0 cn0={sat.average_sno} /></td>
              <td>{sat.average_doppler_hz ? numeral(sat.average_doppler_hz).format("0,0") + " Hz" : <span className="text-muted">n/a</span>}</td>
              <td><GNSSMultipathIndicator indicator={sat.maximum_multipath_indicator} /></td>
              <td><GNSSDegrees degrees={sat.azimuth_degrees} /></td>
              <td><GNSSDegrees degrees={sat.elevation_degrees} /></td>
              <td><a href={ApiRoutes.GNSS.PRN(sat.constellation, sat.prn)}>Details</a></td>
            </tr>
          )
        })}
        </tbody>
      </table>
    </React.Fragment>
  )

}