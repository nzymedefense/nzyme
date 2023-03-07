import React from "react";
import moment from "moment";

function ValidityDate(props) {

  const validity = moment(props.date);

  if (validity.isAfter(moment())) {
    return (
        <span className="text-danger" title="Certificate is not valid yet!">
          {validity.format()}
        </span>
    )
  } else {
    return (
        <span>
          {validity.format()}
        </span>
    )
  }

}

export default ValidityDate