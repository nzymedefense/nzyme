import React from "react";

export default function GenericWidgetLoadingSpinner(props) {

  const height = props.height;

  return (
      <div className="widget-loading-spinner text-muted" style={{height: height}}>
        <i className="fa-solid fa-spinner fa-fade"></i>
      </div>
  )
}