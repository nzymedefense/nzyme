import React from "react";
import ApiRoutes from "../../util/ApiRoutes";
import Dot11MacAddress from "./Dot11MacAddress";
import Dot11MacAddressType, {ACCESS_POINT, CLIENT} from "./Dot11MacAddressType";

function Dot11MacAddressLink(props) {

  const mac = props.mac;
  const type = props.type;

  const link = (bssid, type) => {
    switch (type) {
      case ACCESS_POINT:
        return ApiRoutes.DOT11.NETWORKS.BSSID(mac);
      case CLIENT:
        return ApiRoutes.DOT11.CLIENTS.DETAILS(mac);
    }
  }

  if (type === ACCESS_POINT || type === CLIENT) {
    return (
        <React.Fragment>
          <Dot11MacAddressType type={type}/><a href={link(mac, type)} className="dot11-mac">{mac}</a>
        </React.Fragment>
    )
  } else {
    return <Dot11MacAddress mac={mac} type={type} />
  }

}

export default Dot11MacAddressLink;