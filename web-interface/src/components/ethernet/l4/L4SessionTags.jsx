import React from "react";
import FilterValueIcon from "../../shared/filtering/FilterValueIcon";
import {L4_SESSIONS_FILTER_FIELDS} from "./L4SessionFilterFields";

export default function L4SessionTags({tags, setFilters}) {

  const filterElement = (tag) => {
    if (!setFilters) {
      return null;
    }

    return <><FilterValueIcon setFilters={setFilters}
                            fields={L4_SESSIONS_FILTER_FIELDS}
                            field="tags"
                            value={tag} />{' '}</>
  }

  if (!tags || tags.length === 0) {
    return <span className="text-muted">None</span>
  }

  return (
      <React.Fragment>
        {tags.sort().map((tag, i) => {
          return (
              <React.Fragment key={i}>
                {tag}{filterElement(tag)}{i < tags.length-1 ? ", " : null}
              </React.Fragment>
          )
        })}
      </React.Fragment>
  )

}