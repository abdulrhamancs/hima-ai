const INCIDENT_TYPE_RULES = [
  { type: "FIRE", keywords: ["حريق", "دخان", "fire", "smoke"] },
  { type: "ILLEGAL_LOGGING", keywords: ["احتطاب", "قطع أشجار", "logging", "deforestation"] },
  { type: "ILLEGAL_HUNTING", keywords: ["صيد جائر", "صيد", "hunting", "poaching"] },
  { type: "WATER_POLLUTION", keywords: ["تلوث", "تسرب", "نفايات", "pollution", "spill", "waste"] },
  { type: "PLANT_DISEASE", keywords: ["آفة نباتية", "مرض نباتي", "ذبول", "plant disease", "pest"] },
  { type: "INJURED_ANIMAL", keywords: ["حيوان مصاب", "مصاب", "injured animal"] },
  { type: "DEAD_ANIMAL", keywords: ["حيوان نافق", "حيوان ميت", "نافق", "dead animal", "carcass"] },
];

const REPORT_TYPES = new Set([
  "FIRE",
  "ILLEGAL_LOGGING",
  "ILLEGAL_HUNTING",
  "WATER_POLLUTION",
  "PLANT_DISEASE",
  "INJURED_ANIMAL",
  "DEAD_ANIMAL",
  "OTHER",
]);

function mapIssueTypeToReportType(value) {
  const normalized = String(value || "").trim().toLowerCase();
  const enumValue = normalized.toUpperCase();
  if (REPORT_TYPES.has(enumValue)) return enumValue;

  return INCIDENT_TYPE_RULES.find(({ keywords }) =>
    keywords.some((keyword) => normalized.includes(keyword.toLowerCase()))
  )?.type || "OTHER";
}

function mapRiskLevelToSeverity(value) {
  const normalized = String(value || "").trim().toUpperCase();
  return ["LOW", "MEDIUM", "HIGH", "CRITICAL"].includes(normalized)
    ? normalized
    : "LOW";
}

module.exports = { mapIssueTypeToReportType, mapRiskLevelToSeverity };
