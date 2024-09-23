import React, {useContext, useEffect} from "react";
import {UserContext} from "../../App";

function TenantSelector(props) {

  const user = useContext(UserContext);

  const tenants = props.tenants;
  const tenant = props.tenant;
  const setTenant = props.setTenant;

  // Optional.
  const emptyTitle = props.emptyTitle;

  useEffect(() => {
    if (tenants) {
      if (tenants.tenants.length === 1) {
        // Automatically select tenant if there is only one.
        setTenant(tenants.tenants[0].id)
      } else {
        // Automatically select user-default tenant if user has one and tenant exists.
        if (user.default_tenant) {
          let exists = false;
          for (const tenant of tenants.tenants) {
            if (tenant.id === user.default_tenant) {
              exists = true;
            }
          }

          if (exists) {
            setTenant(user.default_tenant);
          }
        }
      }
    }
  }, [tenants]);

  if (!tenants) {
    return (
        <select className="form-select mb-3" disabled={true}>
          <option>{emptyTitle ? emptyTitle : "Select an organization first."}</option>
        </select>
    )
  }

  return (
      <select className="form-select mb-3"
              value={tenant}
              onChange={(e) => setTenant(e.target.value)}>
        <option value="">{emptyTitle ? emptyTitle : "Select a tenant"}</option>
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