import React from "react";
import {Outlet} from "react-router-dom";
import NotFoundPage from "./NotFoundPage";

export default function ProtectedRoute(props) {

  const execute = props.execute;

  if (!execute) {
    return <NotFoundPage />
  }

  return <Outlet />

}