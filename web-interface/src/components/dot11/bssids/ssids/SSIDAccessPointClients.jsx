import React from "react";

function SSIDAccessPointClients(props) {

  const clients = props.clients;

  if (!clients || clients.length === 0) {
    return (
        <div className="alert alert-info mb-0">
          No clients have been recorded.
        </div>
    )
  }

  return (
      <ul className="mb-0">
        {clients.filter(c => c).map(function (client, i) {
          return <li key={"client-" + i}>
            {client.mac} ({client.oui ? client.oui : "Unknown Vendor"})
          </li>
        })}
      </ul>
  )

}

export default SSIDAccessPointClients;