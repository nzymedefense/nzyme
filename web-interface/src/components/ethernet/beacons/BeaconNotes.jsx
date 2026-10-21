import React, {useEffect, useState} from "react";
import moment from "moment";
import {toast} from "react-toastify";
import LoadingSpinner from "../../misc/LoadingSpinner";
import BeaconsService from "../../../services/ethernet/BeaconsService";

const beaconsService = new BeaconsService();

export default function BeaconNotes({beacon, organizationId, tenantId, canEdit}) {

  const [notes, setNotes] = useState(null);
  const [revision, setRevision] = useState(new Date());
  const [newNote, setNewNote] = useState("");
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    setNotes(null);
    beaconsService.findBeaconNotes(beacon.uuid, organizationId, tenantId, setNotes);
  }, [beacon.uuid, organizationId, tenantId, revision]);

  const onSubmit = (e) => {
    e.preventDefault();

    if (newNote.trim().length === 0) {
      toast.error("The note cannot be empty.");
      return;
    }

    setIsSaving(true);
    beaconsService.createBeaconNote(beacon.uuid, organizationId, tenantId, newNote.trim(), () => {
      toast.success("Note added.");
      setNewNote("");
      setIsSaving(false);
      setRevision(new Date());
    }, () => {
      toast.error("Could not add note.");
      setIsSaving(false);
    });
  }

  const onDelete = (e, note) => {
    e.preventDefault();

    if (!confirm("Really delete this note?")) {
      return;
    }

    beaconsService.deleteBeaconNote(beacon.uuid, note.uuid, organizationId, tenantId, () => {
      toast.success("Note deleted.");
      setRevision(new Date());
    });
  }

  const author = (note) => {
    if (note.author_name && note.author_email) {
      return <span title={note.author_email}>{note.author_name}</span>;
    }

    return note.author_name || note.author_email || <span className="text-muted">Unknown author</span>;
  }

  if (!notes) {
    return <LoadingSpinner />
  }

  return (
    <React.Fragment>
      {notes.notes.length === 0 ?
        <p className="text-muted">No notes yet.</p>
        : <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th style={{width: 180}}>Time</th>
            <th style={{width: 180}}>Author</th>
            <th>Note</th>
            {canEdit ? <th style={{width: 60}}>&nbsp;</th> : null}
          </tr>
          </thead>
          <tbody>
          {notes.notes.map((note, i) => {
            return (
              <tr key={i}>
                <td title={moment(note.created_at).fromNow()}>{moment(note.created_at).format()}</td>
                <td>{author(note)}</td>
                <td style={{whiteSpace: "pre-wrap"}}>{note.note}</td>
                {canEdit ? <td><a href="#" onClick={(e) => onDelete(e, note)}>Delete</a></td> : null}
              </tr>
            )
          })}
          </tbody>
        </table>}

      {canEdit ?
        <form onSubmit={onSubmit} className="mt-2">
          <div className="mb-2">
            <textarea className="form-control" id="beacon-new-note" rows={3} maxLength={10000}
                      value={newNote} onChange={(e) => setNewNote(e.target.value)} />
          </div>
          <button className="btn btn-sm btn-primary" type="submit" disabled={isSaving}>
            {isSaving ? "Please wait..." : "Add Note"}
          </button>
        </form> : null}
    </React.Fragment>
  )

}
