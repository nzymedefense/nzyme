import React, {useEffect, useState} from "react";
import usePageTitle from "../../util/UsePageTitle";
import LoadingSpinner from "../misc/LoadingSpinner";
import {useParams} from "react-router-dom";
import useSelectedTenant from "../system/tenantselector/useSelectedTenant";
import LocationsService from "../../services/LocationsService";
import ApiRoutes from "../../util/ApiRoutes";

const locationsService = new LocationsService();

export default function LocationDetailsPage() {

  const {uuid} = useParams();

  const [organizationId, tenantId] = useSelectedTenant();

  const [location, setLocation] = useState(null);

  usePageTitle(location ? `Location: ${location.name}` : "Location Details");

  useEffect(() => {
    locationsService.findOne(uuid, organizationId, tenantId, setLocation);
  }, [uuid, organizationId, tenantId]);

  if (!location) {
    return <LoadingSpinner />;
  }

  /*
  taps
  alerts
  floors
  current weather
  metar
  severe environmental alerts

   */

  return (
    <>
      <div className="row">
        <div className="col-md-9">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item"><a href={ApiRoutes.LOCATIONS.INDEX}>Locations</a></li>
              <li className="breadcrumb-item active" aria-current="page">{location.name}</li>
            </ol>
          </nav>
        </div>
        <div className="col-md-3">
              <span className="float-end">
                <a className="btn btn-secondary" href={ApiRoutes.LOCATIONS.INDEX}>Back</a>
              </span>
        </div>
      </div>

      <div className="col-md-12">
        <h1>Location &quot;{location.name}&quot;</h1>
      </div>


    </>

  )

}