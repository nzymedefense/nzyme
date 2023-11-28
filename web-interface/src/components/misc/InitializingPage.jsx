import React from "react";
import DarkMode from "../layout/DarkMode";
import AssetImage from "./AssetImage";

function InitializingPage(props) {

  const darkModeEnabled = props.darkModeEnabled;

  return (
      <div className="nzyme">
        <DarkMode enabled={darkModeEnabled} />

        <AssetImage filename="loading-miller.png"
                    className="loading-miller"
                    alt="nzyme is initializing" />

        <AssetImage filename="loading-miller_layer2.png"
                    className="loading-miller loading-miller-layer2"
                    alt="nzyme is initializing" />
      </div>
  )

}

export default InitializingPage;