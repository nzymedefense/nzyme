import React, {useState} from 'react'
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";

export default function LLMsTable({organizationId, tenantId}) {

  const [models, setModels] = useState([]);

  const [page, setPage] = useState(1);
  const perPage = 25;

  if (!models) {
    return <LoadingSpinner />
  }

  if (models.length === 0) {
    return <div className="alert alert-warning mb-0">No models connected.</div>;
  }

}