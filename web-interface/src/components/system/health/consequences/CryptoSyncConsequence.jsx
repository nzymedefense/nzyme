import React from "react";
import Consequence from "../Consequence";
import CryptoSyncProcedure from "./procedures/CryptoSyncProcedure";

function CryptoSyncConsequence(props) {

  if (!props.show) {
    return null
  }

  return (
      <Consequence
          indicator="Crypto Sync"
          color="red"
          problem="Not all nodes share the same PGP key. This should not happen except if you manually changed keys."
          acceptableRange={[
            "n/a"
          ]}
          consequences={[
            "Encrypted configuration parameters will be written using differing PGP keys, depending on which node " +
              "handles the request",
            "Encrypted configuration parameters cannot be reliably decrypted and read, leading to failures and errors " +
              "depending on which node handles the request"
          ]}
          procedure={<CryptoSyncProcedure />}
      />
  )

}

export default CryptoSyncConsequence;