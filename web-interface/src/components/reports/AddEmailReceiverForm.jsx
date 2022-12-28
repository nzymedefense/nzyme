import React, { useState } from 'react'
import { notify } from 'react-notify-toast'

function AddEmailReceiverForm (props) {
  const [filled, setFilled] = useState(false)

  const emailInput = React.createRef()

  const onKeyPress = React.useCallback((e) => {
    if (e.key === 'Enter') {
      e.preventDefault()
    }
  }, [])

  const onChange = React.useCallback((e) => {
    setFilled(emailInput.current && emailInput.current.value.trim() !== '')
  }, [emailInput, setFilled])

  const onSubmit = React.useCallback(() => {
    const receiver = emailInput.current.value
    const report = props.report

    if (receiver && receiver.trim() !== '') {
      if (report.email_receivers.includes(receiver)) {
        notify.show('Cannot add email receiver. Receiver already exists.', 'error')
        return
      }

      emailInput.current.value = ''

      props.reportsService.addEmailReceiver(report.name, receiver,
        function () {
          // we have to set this because the empty reset above does not trigger onChange and button is never disabled
          setFilled(false)

          // Update report data / refresh email receivers.
          props.reportsService.findOne(report.name, props.setReport)
          notify.show('Added report receiver.', 'success')
        },
        function () {
          notify.show('Could not add report receiver. Please check nzyme log file.', 'error')
        }
      )
    }
  }, [emailInput, props.report, props.reportsService, props.setReport, setFilled])

  return (
        <div className="form-group">
            <label htmlFor="addEmail">Add Email Receiver</label>

            <div className="input-group">
                <input id="addEmail"
                    type="text"
                    className="form-control"
                    placeholder="john@example.org"
                    ref={emailInput}
                    onChange={onChange}
                    onKeyPress={onKeyPress} />

                <div className="input-group-append">
                    <button className="btn btn-secondary" type="button" onClick={onSubmit} disabled={!filled}>
                        Add Email Receiver
                    </button>
                </div>
            </div>
        </div>
  )
}

export default AddEmailReceiverForm
