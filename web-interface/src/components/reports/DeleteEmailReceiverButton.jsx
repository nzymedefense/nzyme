import React, { useCallback } from 'react'
import { notify } from 'react-notify-toast'

function DeleteEmailReceiverButton (props) {
  const onDeleteEmailReceiver = useCallback((e) => {
    if (!window.confirm('Delete email receiver?')) {
      return
    }

    props.reportsService.deleteEmailReceiver(props.report.name, props.address,
      function () {
        notify.show('Report receiver deleted.', 'success')

        // Update report data / refresh email receivers.
        props.reportsService.findOne(props.report.name, props.setReport)
      },
      function () {
        notify.show('Could not delete report receiver. Please check nzyme log file.', 'error')
      }
    )
  }, [props.reportsService, props.report, props.setReport, props.address])

  return (
        <button className="btn btn-sm btn-danger" onClick={onDeleteEmailReceiver}>Delete</button>
  )
}

export default DeleteEmailReceiverButton
