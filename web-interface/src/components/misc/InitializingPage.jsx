import React from "react";
import DarkMode from "../layout/DarkMode";
import AssetImage from "./AssetImage";

function InitializingPage(props) {

  const darkModeEnabled = props.darkModeEnabled;

  return (
      <div className="nzyme">
        <DarkMode enabled={darkModeEnabled} />

        <AssetImage filename="initializing.png"
                    className="initializing"
                    alt="nzyme is initializing" />

        <AssetImage filename="initializing_layer2.png"
                    className="initializing initializing-layer2"
                    alt="nzyme is initializing" />

      </div>
  )

}

export default InitializingPage;