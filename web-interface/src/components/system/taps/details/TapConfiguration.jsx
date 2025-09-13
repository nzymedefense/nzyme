import React from "react";

export default function TapConfiguration({configuration}) {

  if (!configuration || configuration === "{}") {
    return <div className="alert alert-info mb-0">This tap is not reporting configuration information.</div>
  }

  return (
      <pre>
        {JSON.stringify(JSON.parse(configuration), null, 2)}
      </pre>
  )

}