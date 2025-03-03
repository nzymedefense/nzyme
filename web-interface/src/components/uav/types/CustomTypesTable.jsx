import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Paginator from "../../misc/Paginator";
import UavClassification from "../util/UavClassification";
import UavModelType from "../util/UavModelType";
import UavSerialNumberMatch from "../util/UavSerialNumberMatch";

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

      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Name</th>
          <th>Model</th>
          <th>Type</th>
          <th>Default Classification</th>
          <th>Serial Number</th>
        </tr>
        </thead>
        <tbody>
        {types.types.map((t, i) => {
          return (
            <tr key={i}>
              <td><a href="#">{t.name}</a></td>
              <td>{t.model ? t.model : <span className="text-muted">n/a</span>}</td>
              <td><UavModelType type={t.type} /></td>
              <td><UavClassification classification={t.default_classification} /></td>
              <td><UavSerialNumberMatch matchType={t.match_type} matchValue={t.match_value} /></td>
            </tr>
          )
        })}
        </tbody>
      </table>

      <Paginator itemCount={types.count} perPage={perPage} setPage={setPage} page={page} />
    </React.Fragment>
  )

}