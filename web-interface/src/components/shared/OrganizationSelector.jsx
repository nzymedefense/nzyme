import React, {useEffect} from "react";

function OrganizationSelector(props) {
  const organizations = props.organizations;
  const organization = props.organization;
  const setOrganization = props.setOrganization;

  useEffect(() => {
    if (organizations.organizations.length === 1) {
      // Automatically select org if there is only one.
      setOrganization(organizations.organizations[0].id);
    }
    }, [organizations]);

  return (
      <select className="form-select mb-3"
              value={organization ? organization : ""}
              onChange={(e) => setOrganization(e.target.value)}>
        <option value="">Select an Organization</option>
        {organizations.organizations.map(function(org, i) {
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