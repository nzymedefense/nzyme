import React from 'react'

function ConfigurationSubmitButton (props) {
  if (props.submittedSuccessfully) {
    return (
            <button type="button"
                    data-bs-dismiss="modal"
                    onClick={props.onFinishedClick}
                    className="btn btn-success configuration-submitted">
                <i className="fa-regular fa-thumbs-up"></i>&nbsp;
                <span>Configuration Saved &#8211; Close Dialog</span>
            </button>
    )
  } else {
    return (
            <button type="button"
                    className="btn btn-primary configuration-submit"
                    onClick={props.onClick}
                    disabled={props.disabled}>
                {props.submitting
                  ? <span><i className="fa-solid fa-circle-notch fa-spin"></i> &nbsp;Saving ...</span>
                  : 'Save Changes'}
            </button>
    )
  }
}

export default ConfigurationSubmitButton
