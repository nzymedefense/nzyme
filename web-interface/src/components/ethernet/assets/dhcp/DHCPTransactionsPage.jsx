import React, {useContext, useEffect, useState} from "react";
import SectionMenuBar from "../../../shared/SectionMenuBar";
import {ASSETS_MENU_ITEMS} from "../AssetsMenuItems";
import ApiRoutes from "../../../../util/ApiRoutes";
import {Presets} from "../../../shared/timerange/TimeRange";
import CardTitleWithControls from "../../../shared/CardTitleWithControls";
import AlphaFeatureAlert from "../../../shared/AlphaFeatureAlert";
import {TapContext} from "../../../../App";
import {disableTapSelector, enableTapSelector} from "../../../misc/TapSelector";
import DHCPTransactionsTable from "./DHCPTransactionsTable";

export default function DHCPTransactionsPage() {

  const tapContext = useContext(TapContext);

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  return (
      <React.Fragment>
        <AlphaFeatureAlert />

        <div className="row">
          <div className="col-md-12">
            <SectionMenuBar items={ASSETS_MENU_ITEMS}
                            activeRoute={ApiRoutes.ETHERNET.ASSETS.DHCP.INDEX} />
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="DHCP Transactions"
                                       helpLink="https://go.nzyme.org/ethernet-dhcp"
                                       timeRange={timeRange}
                                       setTimeRange={setTimeRange} />

                <DHCPTransactionsTable timeRange={timeRange} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}