import React from "react";

export default function LoginImage(props) {

  const customImage = props.customImage;

  if (customImage) {
    return <img src={"data:image/png;base64, " + customImage} />
  }

  return (
      <video id="background-video" autoPlay loop muted
             poster={window.appConfig.assetsUri + "static/loginsplash_preview.jpg"}>
        <source src={window.appConfig.assetsUri + "static/loginsplash.mp4"} type="video/mp4"/>
      </video>
  )

}