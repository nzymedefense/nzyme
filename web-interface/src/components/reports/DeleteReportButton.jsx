import React, { useCallback } from 'react'
import { notify } from 'react-notify-toast'

function DeleteReportButton (props) {
  const deleteReport = useCallback((e) => {
    e.preventDefault()
    if (!window.confirm('Delete report?')) {
      return
    }

    props.reportsService.deleteReport(props.reportName, props.setReportDeleted, function () {
      notify.show('Could not delete report. Please check nzyme log file.', 'error')
    })
  }, [props.reportsService, props.setReportDeleted, props.reportName])

  return (
        <button className="btn btn-danger" onClick={deleteReport}>Delete Report</button>
  )
}

export default DeleteReportButton
