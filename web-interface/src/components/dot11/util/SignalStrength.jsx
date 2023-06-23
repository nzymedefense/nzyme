import React from "react";

function SignalStrength(props) {

  const strength = parseFloat(props.strength);

  let strengthClassName;
  if (strength > -50) {
    strengthClassName = "signal-strength-good";
  } else if (strength <= -50 && strength >= -71) {
    strengthClassName = "signal-strength-ok";
  } else if (strength < -71) {
    strengthClassName = "signal-strength-bad";
  }

  return (
      <span>
        <i className={"fa-solid fa-signal " + strengthClassName }></i>{' '}
        {Math.round(strength)} dBm
      </span>
  )

}

export default SignalStrength;