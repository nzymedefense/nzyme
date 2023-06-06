import React, {useState} from "react";

function EventSubscriptionActionSelector(props) {

  const onSubmit = props.onSubmit;
  const actions = props.actions;

  const [selectedAction, setSelectedAction] = useState("");

  if (!actions || actions.length === 0) {
    return (
      <div className="input-group mt-3">
        <select id="action" className="form-select" disabled={true}>
          <option value="">No actions have been created yet.</option>
        </select>
        <button className="btn btn-primary" type="button" disabled={true}>
          Subscribe Action
        </button>
      </div>
    )
  }

  return (
      <div className="input-group mt-3">
        <select id="action"
                className="form-select"
                value={selectedAction}
                name="action"
                onChange={(e) => setSelectedAction(e.target.value)}>
          <option value="">Please select an action</option>
          {actions.sort((a, b) => a.action_type_human_readable.localeCompare(b.action_type_human_readable)).map((action, i) => {
            return (
              <option key={"actionselector-" + i} value={action.id}>
                ({action.action_type_human_readable}) {action.name}
              </option>
            )
          })}
        </select>
        <button className="btn btn-primary"
                type="button"
                disabled={selectedAction === ""}
                onClick={() => onSubmit(selectedAction)}>
          Subscribe Action
        </button>
      </div>
  )

}

export default EventSubscriptionActionSelector;