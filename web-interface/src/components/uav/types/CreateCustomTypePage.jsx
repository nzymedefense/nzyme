import React from 'react';
import {useParams} from "react-router-dom";

export default function CreateCustomTypePage() {

  const {organizationId, tenantId} = useParams();

  return (
    <span>{organizationId} {tenantId}</span>
  )


}