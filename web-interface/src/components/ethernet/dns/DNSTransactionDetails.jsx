import React, {useContext, useEffect, useState} from "react";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import {TapContext} from "../../../App";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import LoadingSpinner from "../../misc/LoadingSpinner";

export default function DNSTransactionDetails({ transactionId }) {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [organizationId, tenantId] = useSelectedTenant();

  const [transaction, setTransaction] = useState(null);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  if (!transaction) {
    return <LoadingSpinner />
  }

  return (
    <>
      dns
    </>
  )

}