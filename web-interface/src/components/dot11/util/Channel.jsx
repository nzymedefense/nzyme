import React from "react";
import numeral from "numeral";

function Channel(props) {

  const channel = props.channel;
  const frequency = props.frequency;
  const is_main_active = props.is_main_active;

  let className = "";
  if (is_main_active) {
    className = "main-active-channel";
  }

  return <span className={className}>{channel} ({numeral(frequency).format("0,0")} Mhz)</span>

}

export default Channel;