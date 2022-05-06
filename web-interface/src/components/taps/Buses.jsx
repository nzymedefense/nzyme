import React from "react";
import Bus from "./Bus";

function Buses(props) {

    return (
        <div>
            {Object.keys(props.buses).map(function (key, i) {
               return <Bus key={"bus-" + i} bus={props.buses[i]} />
            })}
        </div>
    )

}

export default Buses;