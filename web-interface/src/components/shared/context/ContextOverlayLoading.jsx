import React from "react";
import AssetImage from "../../misc/AssetImage";

export default function ContextOverlayLoading(props) {

  return (
      <React.Fragment>
        <AssetImage filename="loading-miller-notext.png"
                    className="loading-miller"
                    alt="loading ..." />

        <AssetImage filename="loading-miller_layer2-notext.png"
                    className="loading-miller loading-miller-layer2"
                    alt="loading ..." />
      </React.Fragment>
  )

}