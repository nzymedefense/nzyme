import React, {useEffect, useState} from "react";

function SimulatorButton(props) {

  const setToggled = props.setToggled;
  const isToggled = props.isToggled;

  useEffect(() => {
    setToggled(isToggled);
  }, [setToggled, isToggled]);

  if (isToggled) {
    return (
        <button className="btn btn-secondary" onClick={() => setToggled(false)}>
          Hide Simulator
        </button>
    )
  } else {
    return (
        <button className="btn btn-secondary" onClick={() => setToggled(true)}>
          Simulate
        </button>
    )
  }



}

export default SimulatorButton;