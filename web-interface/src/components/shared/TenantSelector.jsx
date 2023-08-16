import React from "react";

function TenantSelector(props) {

  const tenants = props.tenants;
  const tenant = props.tenant;
  const setTenant = props.setTenant;

  if (!tenants) {
    return (
        <select className="form-select mb-3" disabled={true}>
          <option>Select an organization first.</option>
        </select>
    )
  }

  return (
      <select className="form-select mb-3"
              value={tenant}
              onChange={(e) => setTenant(e.target.value)}
      >
        <option value="">Select a tenant</option>
        {tenants.tenants.map(function(tenant, i){
          return (
              <option value={tenant.id} key={"tenant-" + i}>
                {tenant.name}
              </option>
          )
        })}
      </select>
  )

}

export default TenantSelector;