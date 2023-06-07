import React from "react";

function SubscriptionsOfActionTable(props) {

  const subscriptions = props.subscriptions;

  if (!subscriptions || subscriptions.length === 0) {
    return <div className="alert alert-info mt-0 mb-0">Action is not subscribed to any events.</div>
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Category</th>
          <th>Event Name</th>
        </tr>
        </thead>
        <tbody>
        {subscriptions.sort((a, b) => a.category_id.localeCompare(b.category_id)).map((type, i) => {
          return (
              <tr key={"eventtype-" + i}>
                <td title={type.category_id}>{type.category_name}</td>
                <td title={type.id}>{type.name}</td>
              </tr>
          )
        })}
        </tbody>
      </table>
  )

}

export default SubscriptionsOfActionTable;