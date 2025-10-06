import React from "react";

/*
 * This shortens a value but forces the user to copy the full value when copying
 * text from the browser.
 *
 * Useful for shortened fingerprints that a user copies and pastes into a filter field for example.
 */
export default function FullCopy({ shortValue, fullValue }) {

  return (
    <span title={fullValue}
          onCopy={(e) => {
            e.preventDefault();
            e.clipboardData.setData("text/plain", fullValue);
          }}>
      {shortValue}
    </span>
  );

}