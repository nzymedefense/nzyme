import {Slide, ToastContainer} from "react-toastify";
import React from "react";

export default function Toast() {
  return <ToastContainer
    position="bottom-center"
    autoClose={5000}
    hideProgressBar={false}
    newestOnTop={false}
    closeOnClick
    rtl={false}
    pauseOnFocusLoss
    draggable
    pauseOnHover
    theme="colored"
    transition={Slide}
  />
}