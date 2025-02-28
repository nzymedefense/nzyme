import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Paginator from "../../misc/Paginator";

export default function BuiltInTypesTable(props) {

  const types = props.types;

  const page = props.page;
  const perPage = props.perPage;
  const setPage = props.setPage;

  if (types == null) {
    return <LoadingSpinner />
  }

  if (types.length === 0) {
    return <div className="alert alert-warning mb-0">
      No build-in types found. Set up <a href="https://connect.nzyme.org/">nzyme Connect</a> if you want to use this
      feature.
    </div>
  }

  return (
    <React.Fragment>
      <Paginator itemCount={types.count} perPage={perPage} setPage={setPage} page={page} />
    </React.Fragment>
  )

}