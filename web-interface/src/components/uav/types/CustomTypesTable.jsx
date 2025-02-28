import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Paginator from "../../misc/Paginator";

export default function CustomTypesTable(props) {

  const types = props.types;

  const page = props.page;
  const perPage = props.perPage;
  const setPage = props.setPage;

  if (types == null) {
    return <LoadingSpinner />
  }

  if (types.count === 0) {
    return <div className="alert alert-info mb-0">No custom UAV types defined.</div>
  }

  return (
    <React.Fragment>
      <Paginator itemCount={types.count} perPage={perPage} setPage={setPage} page={page} />
    </React.Fragment>
  )

}