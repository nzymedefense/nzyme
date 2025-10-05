import React from 'react';

/*
 * This shortens a fingerprint but forces the user to copy the full fingerprint when copying text from the browser.
 *
 * Useful for shortened fingerprints that a user copies and pastes into a filter field for example.
 */

export default function FullCopyShortenedFingerprint({ fingerprint }) {
  if (!fingerprint) {
    return <span className="text-muted">None</span>;
  }

  const short = fingerprint.slice(0, 6);

  return (
    <span className="machine-data"
          title={fingerprint}
          onCopy={(e) => {
            e.preventDefault();
            e.clipboardData.setData("text/plain", fingerprint);
          }}>
      {short}
    </span>
  );
}