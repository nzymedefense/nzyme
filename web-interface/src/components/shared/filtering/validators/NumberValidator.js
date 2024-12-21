export default function validateNumber(value) {
  if (value.trim() === '') {
    return false;
  }
  return Number.isInteger(Number(value.trim()));
}