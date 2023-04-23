import React from "react";

function TapSecret(props) {

  const secret = props.tap.secret;

  return (
      <div className="tap-secret">
        <input type="text" value={secret} />
      </div>
  )

}

export default TapSecret;