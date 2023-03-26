import React from "react";

function PaginatorPageSelector(props) {

  const totalPages = props.totalPages;
  const page = props.page;
  const toPage = props.toPage;
  const onPageClick = props.onPageClick;

  let adjustedToPage = toPage;

  if (toPage < 0) {
    adjustedToPage = page+toPage;
  }

  if (adjustedToPage <= 0 || adjustedToPage > totalPages) {
    return null;
  }

  return (
      <li className={"page-item " + (page === toPage ? "active" : "")}>
        <a className="page-link" href="#" onClick={(e) => onPageClick(e, adjustedToPage)}>{adjustedToPage}</a>
      </li>
  )

}

export default PaginatorPageSelector;