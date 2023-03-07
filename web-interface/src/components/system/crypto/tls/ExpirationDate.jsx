import React from "react";
import moment from "moment";

function ExpirationDate(props) {

  const expiration = moment(props.date);

  if (expiration.isBefore(moment())) {
    return (
        <span className="text-danger" title="Certificate has expired!">
          {expiration.format()}
        </span>
    )
  } else {
    if (expiration.subtract(7, 'days').isBefore(moment())) {
      return (
          <span className="text-warning" title="Certificate is expiring soon!">
            {expiration.format()}
          </span>
      )
    } else {
      return (
          <span>
            {expiration.format()}
          </span>
      )
    }
  }

}

export default ExpirationDate