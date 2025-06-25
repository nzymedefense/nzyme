import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import LoadingSpinner from "../../misc/LoadingSpinner";
import AssetsService from "../../../services/ethernet/AssetsService";
import AlphaFeatureAlert from "../../shared/AlphaFeatureAlert";
import SectionMenuBar from "../../shared/SectionMenuBar";
import {ASSETS_MENU_ITEMS} from "./AssetsMenuItems";
import ApiRoutes from "../../../util/ApiRoutes";
import moment from "moment/moment";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import AssetHostnamesTable from "./AssetHostnamesTable";
import {Presets} from "../../shared/timerange/TimeRange";
import AssetIpAddressesTable from "./AssetIpAddressesTable";

const assetsService = new AssetsService();

export default function AssetDetailsPage() {

  const {uuid} = useParams();

  const [organizationId, tenantId] = useSelectedTenant();

  const [asset, setAsset] = useState(null);

  const [hostnames, setHostnames] = useState(null);
  const [hostnamesTimeRange, setHostnamesTimeRange] = useState(Presets.ALL_TIME);
  const [hostnamesOrderColumn, setHostnamesOrderColumn] = useState("last_seen");
  const [hostnamesOrderDirection, setHostnamesOrderDirection] = useState("DESC");
  const hostnamesPerPage = 10;
  const [hostnamesPage, setHostnamesPage] = useState(1);

  const [ipAddresses, setIpAddresses] = useState(null);
  const [ipAddressesTimeRange, setIpAddressesTimeRange] = useState(Presets.ALL_TIME);
  const [ipAddressesOrderColumn, setIpAddressesOrderColumn] = useState("last_seen");
  const [ipAddressesOrderDirection, setIpAddressesOrderDirection] = useState("DESC");
  const ipAddressesPerPage = 10;
  const [ipAddressesPage, setIpAddressesPage] = useState(1);

  useEffect(() => {
    setAsset(null);
    assetsService.findAsset(uuid, organizationId, tenantId, setAsset);
  }, [organizationId, tenantId]);

  useEffect(() => {
    setHostnames(null);
    assetsService.findAssetHostnames(
        uuid,
        organizationId,
        tenantId,
        hostnamesTimeRange,
        hostnamesOrderColumn,
        hostnamesOrderDirection,
        hostnamesPerPage,
        (hostnamesPage-1)*hostnamesPerPage,
        setHostnames
    );
  }, [organizationId, tenantId, hostnamesTimeRange, hostnamesOrderColumn, hostnamesOrderDirection, hostnamesPage])

  useEffect(() => {
    setIpAddresses(null);
    assetsService.findAssetIpAddresses(
        uuid,
        organizationId,
        tenantId,
        ipAddressesTimeRange,
        ipAddressesOrderColumn,
        ipAddressesOrderDirection,
        ipAddressesPerPage,
        (ipAddressesPage-1)*ipAddressesPerPage,
        setIpAddresses
    );
  }, [organizationId, tenantId, ipAddressesTimeRange, ipAddressesOrderColumn, ipAddressesOrderDirection, ipAddressesPage])

  if (!asset) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <AlphaFeatureAlert />

        <div className="row">
          <div className="col-md-12">
            <SectionMenuBar items={ASSETS_MENU_ITEMS}
                            activeRoute={ApiRoutes.ETHERNET.ASSETS.INDEX} />
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-8">
            <h1>
              Asset <span className="machine-data">{asset.mac.address}</span>{' '}
              {asset.name ? <span className="context-name">{asset.name}</span>
                  : null}
            </h1>
          </div>

          <div className="col-md-4 text-end">
            <a href={ApiRoutes.ETHERNET.ASSETS.INDEX} className="btn btn-primary">Back to Assets List</a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Details" />

                <dl className="mb-0">
                  <dt>MAC Address</dt>
                  <dd><span className="mac-address">{asset.mac.address}</span></dd>
                  <dt>Name</dt>
                  <dd>{asset.name ?
                      <span className="context-name">{asset.name}</span>
                      : <span className="text-muted">n/a</span>
                  }</dd>
                  <dt>OUI</dt>
                  <dd>{asset.oui ? asset.oui : <span className="text-muted">n/a</span>}</dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Metadata" />

                <p className="text-muted">Asset metadata is never automatically retention cleaned.</p>

                <dl className="mb-0">
                  <dt>First Seen</dt>
                  <dd>
                    {moment(asset.first_seen).format("YYYY-MM-DDTHH:mm:ss.SSSZ")} ({moment(asset.first_seen).fromNow()})
                  </dd>
                  <dt>Last Seen</dt>
                  <dd>
                    {moment(asset.last_seen).format("YYYY-MM-DDTHH:mm:ss.SSSZ")} ({moment(asset.last_seen).fromNow()})
                  </dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="row mt-3">
            <div className="col-md-6">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="Hostnames"
                                         timeRange={hostnamesTimeRange}
                                         setTimeRange={setHostnamesTimeRange} />

                  <AssetHostnamesTable hostnames={hostnames}
                                       page={hostnamesPage}
                                       setPage={setHostnamesPage}
                                       perPage={hostnamesPerPage}
                                       setOrderColumn={setHostnamesOrderColumn}
                                       orderColumn={hostnamesOrderColumn}
                                       setOrderDirection={setHostnamesOrderDirection}
                                       orderDirection={hostnamesOrderDirection} />
                </div>
              </div>
            </div>

            <div className="col-md-6">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="IP Addresses"
                                         timeRange={ipAddressesTimeRange}
                                         setTimeRange={setIpAddressesTimeRange} />

                  <AssetIpAddressesTable addresses={ipAddresses}
                                         page={ipAddressesPage}
                                         setPage={setIpAddressesPage}
                                         perPage={ipAddressesPerPage}
                                         setOrderColumn={setIpAddressesOrderColumn}
                                         orderColumn={ipAddressesOrderColumn}
                                         setOrderDirection={setIpAddressesOrderDirection}
                                         orderDirection={ipAddressesOrderDirection} />
                </div>
              </div>
            </div>
          </div>

        </div>
      </React.Fragment>
  )

}