import React from "react";
import {useParams} from "react-router-dom";

function AlertDetailsPage() {

  const {uuid} = useParams();

  return (
      <div className="alert alert-info">Under Construction.</div>
  )

}

export default AlertDetailsPage;