import React from "react";
import numeral from "numeral";

function Channel(props) {

  const channel = props.channel;
  const frequency = props.frequency;
  const is_main_active = props.is_main_active;

  let className = "";
  let title = "";
  if (is_main_active) {
    className = "main-active-channel";
    title = "This is the most active channel during the selected time frame."
  }

  return <span className={className} title={title}>{channel} ({numeral(frequency).format("0,0")} Mhz)</span>

}

export default Channel;