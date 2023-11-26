import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import ContextService from "../../../../../services/ContextService";
import {notify} from "react-notify-toast";

const contextService = new ContextService();

function MacAddressContextOverlay(props) {

  const address = props.address;

  const [context, setContext] = useState(null);
  const [is404, setIs404] = useState(false);

  useEffect(() => {
    contextService.findMacAddressContext(address, setContext, (error) => {
      // Error.
      if (error.response) {
        if (error.response.status === 404) {
          setIs404(true);
        } else {
          notify.show('Could not load MAC address context. (HTTP ' + error.response.status + ')', 'error')
        }
      } else {
        notify.show('Could not load MAC address context. No response.', 'error')
      }
    });
  }, []);

  if (!context) {
    if (is404) {
      return (
          <React.Fragment>
            not found
          </React.Fragment>
      )
    } else {
      return <LoadingSpinner />
    }
  }

  return (
      <React.Fragment>
        <h6>MAC</h6>
      </React.Fragment>
  )

}

export default MacAddressContextOverlay;