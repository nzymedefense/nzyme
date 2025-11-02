import React from "react";

export default function L4SessionTags({tags}) {

  if (!tags || tags.length === 0) {
    return <span className="text-muted">None</span>
  }

  return (
      <React.Fragment>
        {tags.sort().map((tag, i) => {
          return (
              <React.Fragment key={i}>
                {tag}{i < tags.length-1 ? ", " : null}
              </React.Fragment>
          )
        })}
      </React.Fragment>
  )

}