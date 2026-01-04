export async function uploadApply(file) {
  const formData = new FormData();
  formData.append("file", file);

  const res = await fetch("/api/reviews/upload-apply", {
    method: "POST",
    body: formData,
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }
  return res.json();
}

export async function downloadUpdated(file) {
  const formData = new FormData();
  formData.append("file", file);

  const res = await fetch("/api/reviews/upload-apply/download", {
    method: "POST",
    body: formData,
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }

  const blob = await res.blob();

  const cd = res.headers.get("content-disposition") || "";
  const match = cd.match(/filename="([^"]+)"/);
  const filename = match?.[1] || "Updated.java";

  return { blob, filename };
}
