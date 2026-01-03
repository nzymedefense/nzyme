import React, {useEffect, useState} from "react";
import TapsService from "../../../services/TapsService";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import LoadingSpinner from "../../misc/LoadingSpinner";
import {notify} from "react-notify-toast";
import GnssService from "../../../services/GnssService";

const tapsService = new TapsService();
const gnssService = new GnssService();

export default function GNSSMonitoringSettingsResetElevationMasks() {

  const [organizationId, tenantId] = useSelectedTenant();

  const [availableTaps, setAvailableTaps] = useState(null);

  useEffect(() => {
    tapsService.findAllTapsHighLevel(organizationId, tenantId, (r) => setAvailableTaps(r.data.taps))
    setAvailableTaps([]);
  }, [organizationId, tenantId]);

  const resetElevationMask = (e, tapUuid) => {
    e.preventDefault();

    if (!confirm("Really reset elevation mask of the selected tap?")) {
      return;
    }

    gnssService.cleanElevationMask(tapUuid, () => {
      notify.show("Elevation mask of tap has been reset.", "success");
    });
  }

  if (availableTaps === null) {
    return <LoadingSpinner />
  }

  if (availableTaps.length === 0) {
    return <div className="alert alert-warning mb-0">No taps configured.</div>
  }

  return (
    <>
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Tap</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {availableTaps.map((tap, i) => {
          return (
            <tr key={i}>
              <td>{tap.name}</td>
              <td><a href="#" onClick={(e) => resetElevationMask(e, tap.uuid)}>Reset Elevation Mask</a></td>
            </tr>
          )
        })}
        </tbody>
      </table>
    </>
  )

}