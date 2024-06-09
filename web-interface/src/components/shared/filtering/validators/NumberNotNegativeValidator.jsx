export default function validateNumberNotNegative(value) {
  return Number.isInteger(value) && value > 0;
}