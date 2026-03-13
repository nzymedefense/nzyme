import React, {useContext, useEffect} from 'react'
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import {TapContext} from "../../../App";
import usePageTitle from "../../../util/UsePageTitle";

function BeaconsPage() {

  usePageTitle("Ethernet Beacons");

  const tapContext = useContext(TapContext);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  return (
      <div className="alert alert-danger">
        Under construction.
      </div>
  )

}

export default BeaconsPage;
