import React from "react";
import LoadingSpinner from "../misc/LoadingSpinner";
import ApiRoutes from "../../util/ApiRoutes";

export default function GNSSTapsTable({taps}) {

  if (!taps) {
    return <LoadingSpinner />
  }

  return (
    <table className="table table-sm table-hover table-striped">
      <thead>
      <tr>
        <th>Name</th>
      </tr>
      </thead>
      <tbody>
      {taps.taps.map((t, i) => {
        return (
          <tr key={i}>
            <td><a href={ApiRoutes.GNSS.TAP_DETAILS.FIX(t.id)}>{t.name}</a></td>
          </tr>
        )
      })}
      </tbody>
    </table>
  )

}