
const express = require("express");
const router = express.Router();
const fs = require("fs");
const path = require("path");
const authMiddleware = require("../middleware/authMiddleware");

// Static GeoJSON layer. Populate data/protected-areas.geojson once official
// boundary data is sourced (NCW data request or a Protected Planet/WDPA export) —
// each Feature should carry properties: id, name_ar, name_en, area, category.
const DATA_PATH = path.join(__dirname, "..", "data", "protected-areas.geojson");

// GET /protected-areas — Saudi protected areas as a GeoJSON FeatureCollection
router.get("/", authMiddleware, (req, res) => {
  try {
    const raw = fs.readFileSync(DATA_PATH, "utf8");
    const geojson = JSON.parse(raw);
    res.json(geojson);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Something went wrong while loading protected areas data" });
  }
});

module.exports = router;
