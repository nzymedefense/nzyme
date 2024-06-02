import React from "react";

export default function SSHVersion(props) {

  const version = props.version;

  if (!version) {
    return null;
  }

  return (
      <span>
        {version.software} {version.comments ? "(" + version.comments + ")" : null}
      </span>
  )

}