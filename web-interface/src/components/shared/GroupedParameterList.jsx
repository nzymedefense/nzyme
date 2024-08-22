import React from 'react';

export default function GroupedParameterList(props) {

  const list = props.list;
  const valueTransform = props.valueTransform;

  const formatValue = (value) => {
    if (valueTransform) {
      return valueTransform(value);
    } else {
      return value;
    }
  }

  if (!list || list.length === 0 || list[0] == null) {
    return <span className="text-muted">None</span>
  }

  return (
      <React.Fragment>
        {list.map((x, i) => {
          return (
              <React.Fragment key={i}>
                {formatValue(x)}{i < list.length-1 ? ", " : null}
              </React.Fragment>
          )
        })}
      </React.Fragment>
  )

}