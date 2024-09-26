import React, {useContext, useEffect, useState} from "react";
import {UserContext} from "../../App";

function OrganizationSelector(props) {

  const user = useContext(UserContext);

  const organizations = props.organizations;
  const organization = props.organization;
  const setOrganization = props.setOrganization;

  // Optional.
  const emptyTitle = props.emptyTitle;
  const autoSelectCompleted = props.autoSelectCompleted;

  useEffect(() => {
    if (autoSelectCompleted) {
      return;
    }

    if (organizations.organizations.length === 1) {
      // Automatically select org if there is only one.
      setOrganization(organizations.organizations[0].id);
    } else {
      // Automatically select user-default org if user has one and org exists.
      if (user.default_organization) {
        let exists = false;
        for (const org of organizations.organizations) {
          if (org.id === user.default_organization) {
            exists = true;
          }
        }

        if (exists) {
          setOrganization(user.default_organization);
        }
      }
    }
    }, [organizations]);

  return (
      <select className="form-select mb-3"
              value={organization ? organization : ""}
              onChange={(e) => setOrganization(e.target.value)}>
        <option value="">{emptyTitle ? emptyTitle : "Select an organization"}</option>
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