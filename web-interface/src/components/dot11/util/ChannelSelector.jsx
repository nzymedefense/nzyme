import React from "react";
import numeral from "numeral";
import {dot11FrequencyToChannel} from "../../../util/Tools";

function ChannelSelector(props) {

  const frequencies = props.frequencies;
  const currentFrequency = props.currentFrequency;
  const setFrequency = props.setFrequency;

  return (
      <select className="form-select form-select-sm" style={{width: 215}}
              onChange={(e) => { setFrequency(parseInt(e.target.value, 10)); }}
              value={currentFrequency}>
        {frequencies.filter(f => f).map(function(frequency) {
          return (
              <option key={"csw-" + frequency} value={frequency}>
                Channel {dot11FrequencyToChannel(frequency)} ({numeral(frequency).format("0,0")} MHz)
              </option>
          )
        })}
      </select>
  )

}

export default ChannelSelector;