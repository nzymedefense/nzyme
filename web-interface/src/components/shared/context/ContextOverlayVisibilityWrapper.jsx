import React from "react";

function ContextOverlayVisibilityWrapper(props) {

  const visible = props.visible;
  const overlay = props.overlay;

  if (!visible) {
    return null;
  }

  return <div className="context-overlay">{overlay}</div>;

}

export default ContextOverlayVisibilityWrapper;