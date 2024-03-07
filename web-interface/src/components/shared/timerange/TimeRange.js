export const Relative = (minutes, name) => {
  return { type: "relative", minutes: minutes, name: name }
}

export const Named = (name) => {
  return { type: "named", name: name }
}

export const Absolute = (from, to) => {
  return {type: "absolute", from: from, to: to}
}