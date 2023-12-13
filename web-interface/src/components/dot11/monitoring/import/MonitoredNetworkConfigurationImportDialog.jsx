import React, {useEffect, useState} from "react";
import Dot11Service from "../../../../services/Dot11Service";
import LoadingSpinner from "../../../misc/LoadingSpinner";

const dot11Service = new Dot11Service();

function MonitoredNetworkConfigurationImportDialog(props) {

  const uuid = props.uuid;

  const [previewImport, setPreviewImport] = useState(false);
  const [importData, setImportData] = useState(null);

  useEffect(() => {
    if (previewImport) {
      dot11Service.getMonitoredNetworkImportData(uuid, setImportData)
    }
  }, [uuid, previewImport]);

  if (!previewImport) {
    return (
        <button type="button" className="btn btn-primary btn-sm" onClick={() => setPreviewImport(true)}>
          Preview Import
        </button>
    )
  }

  if (previewImport && !importData) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <h4>BSSIDs </h4>
      </React.Fragment>
  )

}

export default MonitoredNetworkConfigurationImportDialog;