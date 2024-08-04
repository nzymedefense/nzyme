import React, {useContext, useEffect, useState} from 'react';
import {TapContext} from "../../../App";
import {Presets} from "../../shared/timerange/TimeRange";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import BluetoothService from "../../../services/BluetoothService";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import BluetoothDevicesTable from "./BluetoothDevicesTable";
import AlphaFeatureAlert from "../../shared/AlphaFeatureAlert";

const btService = new BluetoothService();

export default function BluetoothDevicesPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [devices, setDevices] = useState(null);

  const [devicesTimeRange, setDevicesTimeRange] = useState(Presets.RELATIVE_MINUTES_15);
  const [devicesPage, setDevicesPage] = useState(1);

  const perPage = 50;

  useEffect(() => {
    setDevices(null);
    btService.findAllDevices(setDevices, devicesTimeRange, selectedTaps, perPage, (devicesPage-1)*perPage);
  }, [selectedTaps, devicesTimeRange, devicesPage])

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
            <h1>Bluetooth Devices</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Devices"
                                       timeRange={devicesTimeRange}
                                       setTimeRange={setDevicesTimeRange}/>

                <BluetoothDevicesTable devices={devices}
                                       page={devicesPage}
                                       perPage={perPage}
                                       setPage={setDevicesPage} />
              </div>
            </div>
          </div>
        </div>

      </React.Fragment>
)

}