import React from 'react'

// ATTENTION: This component is not only used in the default configuration modals.

function ConfigurationCloseButton (props) {
  if (props.submittedSuccessfully) {
    return null
  } else {
    return (
            <button type="button"
                    className="btn btn-secondary"
                    data-bs-dismiss="modal"
                    onClick={props.onClick}
                    disabled={props.submitting}>
                Cancel
            </button>
    )
  }
}

export default ConfigurationCloseButton
