export function formatDuration(minutes) {
  if (minutes < 60) {
    return `${Math.round(minutes)} min`;
  }
  const hours = Math.floor(minutes / 60);
  const remMin = Math.round(minutes % 60);
  if (hours < 24) {
    return remMin === 0 ? `${hours}h` : `${hours}h ${remMin}m`;
  }
  const days = Math.floor(hours / 24);
  const remHours = hours % 24;
  return remHours === 0 ? `${days}d` : `${days}d ${remHours}h`;
}