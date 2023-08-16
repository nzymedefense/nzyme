import React from "react";

function OrganizationSelector(props) {

  const organizations = props.organizations;
  const organization = props.organization;
  const setOrganization = props.setOrganization;

  return (
      <select className="form-select mb-3"
              value={organization}
              onChange={(e) => setOrganization(e.target.value)}
      >
        <option value="">Select an organization</option>
        {organizations.organizations.map(function(org, i){
          return (
              <option value={org.id} key={"org-" + i}>
                {org.name}
              </option>
          )
        })}
      </select>
  )

}

export default OrganizationSelector;