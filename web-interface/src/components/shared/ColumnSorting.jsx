import React from "react";

export default function ColumnSorting(props) {

  const thisColumn = props.thisColumn;
  const orderColumn = props.orderColumn;
  const setOrderColumn = props.setOrderColumn;
  const orderDirection = props.orderDirection;
  const setOrderDirection = props.setOrderDirection;

  if (orderColumn === thisColumn) {
    // Currently sorted on column.
    if (orderDirection === "ASC") {
      return <i className="fa-solid fa-arrow-up-short-wide order-icon order-icon-selected" onClick={() => setOrderDirection("DESC")}></i>;
    } else {
      return <i className="fa-solid fa-arrow-down-wide-short order-icon order-icon-selected" onClick={() => setOrderDirection("ASC")}></i>
    }
  } else {
    // Not currently sorted on column.
    return <i className="fa-solid fa-arrow-down-wide-short order-icon"
              onClick={() => { setOrderColumn(thisColumn), setOrderDirection("DESC") } }></i>;
  }

}