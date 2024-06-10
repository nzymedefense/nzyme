export default function validateNumberNotNegative(value) {
  const parsedValue = Number(value.trim());
  return Number.isInteger(parsedValue) && parsedValue > 0;
}