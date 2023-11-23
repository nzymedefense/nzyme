import React from "react";
import ApiRoutes from "../../../../util/ApiRoutes";
import Dot11MacAddress from "./Dot11MacAddress";
import Dot11MacAddressType, {ACCESS_POINT, CLIENT} from "./Dot11MacAddressType";
import MacAddress from "./MacAddress";

function AutomaticDot11MacAddressLink(props) {

  const address = props.mac;
  const type = props.type;

  const link = (bssid, type) => {
    switch (type) {
      case ACCESS_POINT:
        return ApiRoutes.DOT11.NETWORKS.BSSID(address);
      case CLIENT:
        return ApiRoutes.DOT11.CLIENTS.DETAILS(address);
    }
  }

  if (type === ACCESS_POINT || type === CLIENT) {
    return (
        <React.Fragment>
          <Dot11MacAddressType type={type}/>
          <MacAddress address={address} href={link(address, type)} />
        </React.Fragment>
    )
  } else {
    return <Dot11MacAddress address={address} type={type} />
  }

}

export default AutomaticDot11MacAddressLink;