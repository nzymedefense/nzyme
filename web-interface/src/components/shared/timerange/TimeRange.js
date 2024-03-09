export const Relative = (minutes, name) => {
  return { type: "relative", minutes: minutes, name: name }
}

export const Named = (name) => {
  return { type: "named", name: name }
}

export const Absolute = (from, to) => {
  return {type: "absolute", from: from, to: to}
}

export const Presets = {
  RELATIVE_MINUTES_15: Relative(15, "Last 15 Minutes"),
  RELATIVE_HOURS_24:   Relative(1440, "Last 24 Hours"),
  ALL_TIME:            Named("all_time")
}