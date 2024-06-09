export default function validateStringNotEmpty(value) {
  return value && value.trim().length > 0;
}