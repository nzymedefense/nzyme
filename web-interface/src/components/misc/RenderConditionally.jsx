import React from 'react';

export default function RenderConditionally(props) {

  const render = props.render;

  if (render) {
    return props.children;
  } else {
    return null;
  }

}