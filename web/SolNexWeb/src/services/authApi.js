const API_URL = import.meta.env.VITE_API_URL || "http://localhost:5097"

export async function registerProsumer(request) {
  const response = await fetch(`${API_URL}/api/users/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  })

  const data = await response.json().catch(() => ({}))

  if (!response.ok) {
    const validationMessage = data.errors
      ? Object.values(data.errors).flat().join(" ")
      : data.message

    throw new Error(validationMessage || "Registration could not be completed.")
  }

  return data
}
