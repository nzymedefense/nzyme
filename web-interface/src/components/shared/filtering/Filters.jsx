import React, {useState} from 'react';
import AppliedFilterList from "./AppliedFilterList";

export default function Filters(props) {

  const fields = props.fields;
  const filters = props.filters;
  const setFilters = props.setFilters;

  const [selectedFilter, setSelectedFilter] = useState("0");
  const [selectedOperator, setSelectedOperator] = useState("==");

  const onFilterSelected = (e) => {
    e.preventDefault();

    setSelectedFilter(e.target.value);
  }

  const onOperatorSelected = (e) => {
    e.preventDefault();

    setSelectedOperator(e.target.value);
  }

  return (
      <div className="filters mt-3">
        <h4>Filters</h4>

        <div className="input-group mb-3">
          <select className="form-select form-select-sm" value={selectedFilter} style={{width: 160}} onChange={onFilterSelected}>
            <option value="0">Select a Field</option>

            {fields.map((field, i) => {
              return <option value={field.name} key={i}>{field.title}</option>
            })}
          </select>

          <select className="form-select form-select-sm" value={selectedOperator} onChange={onOperatorSelected}>
            <option value="==">==</option>
            <option value=">">&gt;</option>
            <option value="<">&lt;</option>
          </select>

          <input className="form-control" type="text" style={{width: 250}} />

          <button className="btn btn-primary" type="button" disabled={selectedFilter === "0"}>
            Add Filter
          </button>
        </div>

        <AppliedFilterList filters={filters}/>
      </div>
  )

}