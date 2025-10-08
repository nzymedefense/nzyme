import React from "react";

export default function AssetActiveIndicator({active}) {

  if (active) {
    return <span title="Asset is active. It was seen in the previous 30 minutes.">
      <i className="fa fa-circle text-success"></i> Active</span>
  } else {
    return <span title="Asset is not active. It was not seen in the previous 30 minutes.">
      <i className="fa fa-circle text-danger"></i> Inactive</span>
  }

}