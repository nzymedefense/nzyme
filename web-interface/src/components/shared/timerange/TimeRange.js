export const Relative = (minutes) => {
  return { type: "relative", minutes: minutes }
}

export const Named = (name) => {
  return { type: "named", name: name }
}

export const Absolute = (from, to) => {
  return {type: "absolute", from: from, to: to}
}