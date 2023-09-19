import React from "react";

function TrackDetectorConfigModalButton(props) {

  const formSubmitted = props.formSubmitted;
  const onSuccessModalClose = props.onSuccessModalClose;
  const onSubmit = props.onSubmit;
  const disabled = props.disabled;

  if (formSubmitted) {
    return (
        <button type="button" className="btn btn-success" data-bs-dismiss="modal" onClick={onSuccessModalClose}>
          <i className="fa-regular fa-thumbs-up"></i>&nbsp;
          <span>Configuration Saved &#8211; Close Dialog</span>
        </button>
    )
  } else {
    return (
        <button type="button" className="btn btn-primary" onClick={onSubmit} disabled={disabled}>
          Save changes
        </button>
    )
  }

}

export default TrackDetectorConfigModalButton;