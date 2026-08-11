
const express = require("express");
const router = express.Router();
const fs = require("fs");
const path = require("path");
const authMiddleware = require("../middleware/authMiddleware");


const DATA_PATH = path.join(__dirname, "..", "data", "protected-areas.geojson");


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
