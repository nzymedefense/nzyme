export default function validateNumber(value) {
  if (value === undefined || value === null) {
    return false;
  }

  const parsedValue = Number(value);
  return !Number.isNaN(parsedValue) && Number.isFinite(parsedValue);
}