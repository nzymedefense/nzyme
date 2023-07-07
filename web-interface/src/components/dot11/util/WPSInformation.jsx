import React from "react";

function WPSInformation(props) {

  const wps = props.wps;

  let has_true = false;
  let has_false = false;

  for (const wps_setting of wps) {
    if (wps_setting) {
      has_true = true;
    } else {
      has_false = true;
    }
  }

  if (has_true && has_false) {
    return "Enabled, Disabled";
  }

  if (has_true && !has_false) {
    return "Enabled";
  }

  if (!has_true && has_false) {
    return "Disabled";
  }

  return "INVALID";

}

export default WPSInformation;