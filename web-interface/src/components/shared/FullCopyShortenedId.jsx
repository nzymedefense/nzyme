import React from 'react';
import FullCopy from "./FullCopy";

export default function FullCopyShortenedId({ value }) {
  if (!value) {
    return <span className="text-muted">None</span>;
  }

  const short = value.slice(0, 6);

  return (
    <span className="machine-data">
      <FullCopy shortValue={short} fullValue={value} />
    </span>
  )

}