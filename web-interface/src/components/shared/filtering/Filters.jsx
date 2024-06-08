import React, {useState} from 'react';
import AppliedFilterList from "./AppliedFilterList";

export default function Filters(props) {

  const fields = props.fields;
  const filters = props.filters;
  const setFilters = props.setFilters;

  const [selectedFilter, setSelectedFilter] = useState({ name: "", field: "0" });
  const [selectedOperator, setSelectedOperator] = useState("==");
  const [filterValue, setFilterValue] = useState("");

  const onFilterSelected = (e) => {
    e.preventDefault();

    const selectedOption = e.target.options[e.target.selectedIndex];

    setSelectedFilter({ name: selectedOption.text, field: selectedOption.value });
  }

  const onOperatorSelected = (e) => {
    e.preventDefault();

    setSelectedOperator(e.target.value);
  }

  const onFilterValueChanged = (e) => {
    e.preventDefault();

    setFilterValue(e.target.value);
  }

  const onFilterAdded = (e) => {
    e.preventDefault();

    const f = {
      name: selectedFilter.name,
      field: selectedFilter.field,
      operator: selectedOperator,
      value: filterValue
    }

    setFilters(prev => [...prev, f]);
  }

  return (
      <div className="filters mt-3">
        <h4>Filters</h4>

        <div className="input-group mb-3">
          <select className="form-select form-select-sm" value={selectedFilter.field} style={{width: 160}}
                  onChange={onFilterSelected}>
            <option value="0">Select a Field</option>

            {fields.map((field, i) => {
              return <option value={field.name} key={i}>{field.title}</option>
            })}
          </select>

          <select className="form-select form-select-sm" value={selectedOperator}
                  onChange={onOperatorSelected}>
            <option value="==">==</option>
            <option value=">">&gt;</option>
            <option value="<">&lt;</option>
          </select>

          <input className="form-control" type="text" style={{width: 250}}
                 value={filterValue} onChange={onFilterValueChanged} />

          <button className="btn btn-primary" type="button" disabled={selectedFilter.field === "0"} onClick={onFilterAdded}>
            Add Filter
          </button>
        </div>

        <AppliedFilterList filters={filters} />
      </div>
  )

}