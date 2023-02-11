export function anyTrackerTrackingBandit (bandit, trackers) {
  if (!trackers || !bandit) {
    return false
  }

  let result = false
  trackers.forEach(function (tracker) {
    if (tracker.tracking_mode && tracker.tracking_mode === bandit.uuid) {
      result = true
    }
  })

  return result
}
