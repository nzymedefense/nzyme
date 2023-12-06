import React from "react";
import createDOMPurify from 'dompurify'
import {marked} from "marked";

const DOMPurify = createDOMPurify(window);

function ContextNotes(props) {

  const notes = props.notes;

  if (!notes || !notes.trim()) {
    return <div className="alert alert-info mb-0">This context has no notes attached to it.</div>
  }

  return <div className="rendered-markdown"
              dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(marked.parse(notes)) }} />;

}

export default ContextNotes;