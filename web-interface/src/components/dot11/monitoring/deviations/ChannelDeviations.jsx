import React from "react";
import {dot11FrequencyToChannel} from "../../../../util/Tools";

function ChannelDeviations(props) {

  const deviations = props.deviations;

  if (!deviations || deviations.length === 0) {
    return null;
  }

  return (
      <React.Fragment>
        The following unexpected channels were recorded:

        <ul className="mt-2 mb-2">
        {deviations.sort().map(function(freq, i) {
          return (
              <li key={"unexpectedchannel-" + i}>
                {freq} MHz (Channel {dot11FrequencyToChannel(freq)})
              </li>
          )
        })}
        </ul>
      </React.Fragment>
  )

}

export default ChannelDeviations;