import React from "react";
import {useParams} from "react-router-dom";

function SSIDDetailsPage() {

  const {bssid} = useParams();
  const {ssid} = useParams();
  const {channel} = useParams();

  // taps

  return (
      <React.Fragment>
       <span>{bssid} - {ssid} / {channel}</span>
      </React.Fragment>
  )

}

export default SSIDDetailsPage;