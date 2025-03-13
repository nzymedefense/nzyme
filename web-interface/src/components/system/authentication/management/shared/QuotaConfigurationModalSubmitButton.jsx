import React from "react";

export default function QuotaConfigurationModalSubmitButton(props) {

  const onClick = props.onClick;
  const onFinishedClick = props.onFinishedClick;
  const disabled = props.disabled;
  const submitting = props.submitting;
  const submittedSuccessfully = props.submittedSuccessfully;

  if (submittedSuccessfully) {
    return (
      <button type="button"
              data-bs-dismiss="modal"
              onClick={onFinishedClick}
              className="btn btn-success w-100">
        <i className="fa-regular fa-thumbs-up"></i>&nbsp;
        <span>Quota Updated &#8211; Close Dialog</span>
      </button>
    )
  } else {
    return (
      <button type="button"
              className="btn btn-primary"
              onClick={onClick}
              disabled={disabled}>
        {submitting
          ? <span><i className="fa-solid fa-circle-notch fa-spin"></i> &nbsp;Saving ...</span>
          : 'Save Quota'}
      </button>
    )
  }
}