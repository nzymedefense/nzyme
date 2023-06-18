import React from "react";

function SSIDsList(props) {

  const ssids = props.ssids.join(", ");

  if (ssids.length > 50) {
    return ssids.slice(0, 49) + "\u2026"
  } else {
    return ssids
  }

}

export default SSIDsList;