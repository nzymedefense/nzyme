import React from "react";
import Subsystem from "../../../../misc/Subsystem";
import ApiRoutes from "../../../../../util/ApiRoutes";

function DetectionSubscriptionsOfActionTable(props) {

  const subscriptions = props.subscriptions;
  const organizationId = props.organizationId;

  if (!subscriptions || subscriptions.length === 0) {
    return <div className="alert alert-info mt-0 mb-0">Action is not subscribed to any detection events.</div>
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Detection Event Name</th>
          <th>Title</th>
          <th>Subsystem</th>
        </tr>
        </thead>
        <tbody>
        {subscriptions.map((type, i) => {
          return (
              <tr key={"eventtype-" + i}>
                <td>
                  { type.event === "WILDCARD" ? type.event :
                    <a href={ApiRoutes.ALERTS.SUBSCRIPTIONS.DETAILS(organizationId, type.event)}>{type.event}</a>
                  }
                </td>
                <td>{type.title}</td>
                <td><Subsystem subsystem={type.subsystem} /></td>
              </tr>
          )
        })}
        </tbody>
      </table>
  )

}

export default DetectionSubscriptionsOfActionTable;