import React, {useEffect, useState} from "react";
import TapsService from "../../services/TapsService";
import LoadingSpinner from "../misc/LoadingSpinner";
import useSelectedTenant from "../system/tenantselector/useSelectedTenant";

const tapsService = new TapsService();

function InlineTapSelector(props) {

  const [organizationId, tenantId] = useSelectedTenant();
  const onTapSelected = props.onTapSelected;

  const [selectedTapUuid, setSelectedTapUuid] = useState("");
  const [taps, setTaps] = useState(null);

  useEffect(() => {
    tapsService.findAllTapsHighLevel(organizationId, tenantId, (response) => {
      setTaps(response.data.taps);
    })
  }, [organizationId, tenantId]);

  useEffect(() => {
    if (taps && taps.length > 0) {
      setSelectedTapUuid(taps[0].uuid);
      onTapSelected(taps[0].uuid);
    }
  }, [taps]);

  if (taps && taps.length === 0) {
    return (
        <div className="alert alert-warning mt-3 mb-0">
          You have no Nzyme taps. You need Nzyme taps to perform the selected action.
        </div>
    )
  }

  if (!selectedTapUuid) {
    return (
        <div className="mt-4">
          <LoadingSpinner />
        </div>
    )
  }

  return (
      <React.Fragment>
        <div className="input-group">
          <span className="input-group-text">Select Tap:</span>
          <select className="form-select"
                  name="method"
                  value={selectedTapUuid}
                  onChange={(e) => { setSelectedTapUuid(e.target.value); onTapSelected(e.target.value) }}>
          {taps.map((tap, i) => {
            return (
                <option key={i} value={tap.uuid}>
                  {tap.name} {tap.is_online ? null : "(Offline)"}
                </option>
            )
          })}
          </select>
        </div>
      </React.Fragment>
  )

}

export default InlineTapSelector;