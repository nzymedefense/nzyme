import React from "react";

function AlertActionMultiSelector(props) {

  const show = props.show;
  const selectedRows = props.selectedRows;
  const onDeleteSelected = props.onDeleteSelected;
  const onResolveSelected = props.onResolveSelected;

  if (!show) {
    return null;
  }

  return (
      <div className="dropdown">
        <a className="btn btn-sm btn-outline-secondary dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
          {selectedRows.length} Selected ...
        </a>

        <ul className="dropdown-menu">
          <li>
            <a className={"dropdown-item" + (selectedRows.length === 0 ? " disabled" : "")}
               onClick={onDeleteSelected}
               href="#">
              Delete
            </a>
          </li>
          <li>
            <a className={"dropdown-item" + (selectedRows.length === 0 ? " disabled" : "")}
               onClick={onResolveSelected}
               href="#">
              Mark as Resolved
            </a>
          </li>
        </ul>
      </div>
  )

}

export default AlertActionMultiSelector;