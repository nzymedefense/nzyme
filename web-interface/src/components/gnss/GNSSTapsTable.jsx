import React from "react";
import LoadingSpinner from "../misc/LoadingSpinner";
import ApiRoutes from "../../util/ApiRoutes";
import GNSSFixSparkHistogram from "./GNSSFixSparkHistogram";

export default function GNSSTapsTable({taps}) {

  if (!taps) {
    return <LoadingSpinner />
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Name</th>
          <th>Location</th>
          <th>BeiDou Fix</th>
          <th>Galileo Fix</th>
          <th>GLONASS Fix</th>
          <th>GPS Fix</th>
        </tr>
        </thead>
        <tbody>
        {taps.taps.map((t, i) => (
          <tr key={i}>
            <td><a href={ApiRoutes.GNSS.TAP_DETAILS.FIX(t.id)}>{t.name}</a></td>
            <td>{t.location_name ? t.location_name : <span className="text-muted">n/a</span>}</td>
            <td style={{ lineHeight: 0 }}>
              <GNSSFixSparkHistogram histogram={t.fix_quality_histogram} constellation="beidou" />
            </td>
            <td style={{ lineHeight: 0 }}>
              <GNSSFixSparkHistogram histogram={t.fix_quality_histogram} constellation="galileo" />
            </td>
            <td style={{ lineHeight: 0 }}>
              <GNSSFixSparkHistogram histogram={t.fix_quality_histogram} constellation="glonass" />
            </td>
            <td style={{ lineHeight: 0 }}>
              <GNSSFixSparkHistogram histogram={t.fix_quality_histogram} constellation="gps" />
            </td>
          </tr>
        ))}
        </tbody>
      </table>
    );

}