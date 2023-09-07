import React from "react";
import LoadingSpinner from "../../../misc/LoadingSpinner";

function CustomBanditsTable(props) {

  const bandits = props.bandits;

  if (!bandits) {
    return <LoadingSpinner />
  }

  if (bandits.bandits.length === 0) {
    return <div className="alert alert-info mt-3">No custom bandits defined yet.</div>
  }

  return (
      <React.Fragment>
        table
      </React.Fragment>
  )

}

export default CustomBanditsTable;