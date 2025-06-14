import React from "react";

export default function AssetName(props) {

  const name = props.name;

  if (!name || name.trim() === "") {
    return <span className="text-muted">None</span>
  }

  return <span className="context-name">{name}</span>;

}