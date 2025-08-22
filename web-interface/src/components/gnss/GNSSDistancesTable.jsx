import React from "react";
import LoadingSpinner from "../misc/LoadingSpinner";
import numeral from "numeral";

export default function GNSSDistancesTable({distances}) {

  const formattedDistance = (distance) => {
    if (!distance) {
      return <span className="text-muted">n/a</span>
    }

    return (
        <span title={numeral(distance*3.28084).format("0,0.0") + "ft"}>{numeral(distance).format("0,0.00")}m</span>
    )
  }

  if (!distances) {
    return <LoadingSpinner height={400} />
  }

  if (distances.length === 0) {
    return <div className="alert alert-warning mb-0">None of the selected taps have a geographical
      position (latitude, longitude) associated. You can configure a position in the tap settings.</div>;
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped mt-2">
          <thead>
          <tr>
            <th>Tap</th>
            <th>GPS</th>
            <th>GLONASS</th>
            <th>BeiDou</th>
            <th>Galileo</th>
          </tr>
          </thead>
          <tbody>
          {distances.map((d, i) => {
            return (
                <tr key={i}>
                  <td>{d.tap.name}</td>
                  <td>{formattedDistance(d.gps)}</td>
                  <td>{formattedDistance(d.glonass)}</td>
                  <td>{formattedDistance(d.beidou)}</td>
                  <td>{formattedDistance(d.galileo)}</td>
                </tr>
            )
          })}
          </tbody>
        </table>
      </React.Fragment>
  )

}