import React, {useEffect, useState} from "react";
import usePageTitle from "../../util/UsePageTitle";
import LoadingSpinner from "../misc/LoadingSpinner";
import {useParams} from "react-router-dom";
import useSelectedTenant from "../system/tenantselector/useSelectedTenant";
import LocationsService from "../../services/LocationsService";

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

}