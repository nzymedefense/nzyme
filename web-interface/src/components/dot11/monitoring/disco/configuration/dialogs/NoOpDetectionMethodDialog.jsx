import React from "react";

function NoOpDetectionMethodDialog(props) {

  const onSubmit = props.onSubmit;

  const type = "NOOP";

  return (
      <React.Fragment>
        <button className="btn btn-primary" onClick={(e) => onSubmit(e, type, {})}>
          Save Configuration
        </button>
      </React.Fragment>
  )

}

export default NoOpDetectionMethodDialog;