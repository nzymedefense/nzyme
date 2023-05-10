import React, {useEffect, useState} from "react";
import UserProfileService from "../../services/UserProfileService";
import LoadingSpinner from "../misc/LoadingSpinner";

const userProfileService = new UserProfileService();

function UserProfile() {

  const [profile, setProfile] = useState(null);

  useEffect(() => {
    userProfileService.findOwnProfile(setProfile);
  }, [])

  if (!profile) {
    return <LoadingSpinner />
  }

  return (
      <dl className="mb-0">
        <dt>Email / Username</dt>
        <dd>{profile.email}</dd>
        <dt>Name</dt>
        <dd>{profile.name}</dd>
      </dl>
  )

}

export default UserProfile;