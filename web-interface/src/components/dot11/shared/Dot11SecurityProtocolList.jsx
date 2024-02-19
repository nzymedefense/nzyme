import React from "react";
import Dot11SecurityProtocol from "./Dot11SecurityProtocol";

function Dot11SecurityProtocolList(props) {

  const protocols = props.protocols;

  if (!protocols || protocols.length === 0) {
    return "None"
  }

  return (
      <React.Fragment>
        {protocols.map((p, i) => {
          return (
              <React.Fragment key={i}>
                <Dot11SecurityProtocol key={i} protocol={p} />{i < protocols.length-1 ? ", " : null}
              </React.Fragment>
          )
        })}
      </React.Fragment>
  )


}

export default Dot11SecurityProtocolList;