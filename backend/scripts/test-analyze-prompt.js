// Runs every image in ./images through the live /analyze endpoint and prints
// a results table — a quick way to check prompt/schema changes against a
// batch of real photos instead of one curl at a time.
//
// Usage:
//   1. Drop test images into scripts/images/ (gitignored — not committed)
//      Filename convention: "<expected_label>__anything.jpg", e.g.
//      "fire__hillside.jpg", "recyclable__can.jpg", "reject__random.jpg"
//      (the part before "__" is just for your own reference in the table)
//   2. node scripts/test-analyze-prompt.js <base_url> <access_token>
//
// Note: any environmental_incident result is a real save to the reports
// table (same as a real user hitting /analyze) — recyclable_waste results
// save nothing.
const fs = require("fs");
const path = require("path");

const BASE_URL = process.argv[2] || "http://localhost:5000";
const TOKEN = process.argv[3];
const IMAGES_DIR = path.join(__dirname, "images");

if (!TOKEN) {
  console.error("Usage: node test-analyze-prompt.js <base_url> <access_token>");
  process.exit(1);
}

const MIME_BY_EXT = {
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".png": "image/png",
  ".webp": "image/webp",
};

async function analyzeOne(filePath) {
  const buf = fs.readFileSync(filePath);
  const ext = path.extname(filePath).toLowerCase();
  const mimeType = MIME_BY_EXT[ext] || "image/jpeg";

  const form = new FormData();
  form.append("image", new Blob([buf], { type: mimeType }), path.basename(filePath));

  const res = await fetch(`${BASE_URL}/analyze`, {
    method: "POST",
    headers: { Authorization: `Bearer ${TOKEN}` },
    body: form,
  });

  const body = await res.json().catch(() => ({}));
  return { status: res.status, body };
}

(async () => {
  if (!fs.existsSync(IMAGES_DIR)) fs.mkdirSync(IMAGES_DIR);
  const files = fs.readdirSync(IMAGES_DIR).filter((f) => MIME_BY_EXT[path.extname(f).toLowerCase()]);

  if (files.length === 0) {
    console.log(`No images found in ${IMAGES_DIR}. Drop test images there first.`);
    return;
  }

  const rows = [];
  for (const file of files) {
    const label = file.split("__")[0];
    process.stdout.write(`analyzing ${file} ... `);
    try {
      const { status, body } = await analyzeOne(path.join(IMAGES_DIR, file));
      const r = body.ai_result || {};
      rows.push({
        file,
        expected: label,
        http: status,
        result_category: body.result_category || (status === 422 ? "rejected" : "-"),
        issue_type_or_material: r.issue_type || r.material_category || "-",
        risk_or_disposal: r.risk_level || r.disposal_classification || "-",
        confidence: r.confidence ?? "-",
        error: body.error || "-",
      });
      console.log("done");
    } catch (e) {
      rows.push({ file, expected: label, http: "ERR", result_category: "-", issue_type_or_material: "-", risk_or_disposal: "-", confidence: "-", error: e.message });
      console.log("failed:", e.message);
    }
  }

  console.log("\n" + "=".repeat(100));
  console.table(rows);
})();
