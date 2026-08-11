const { GoogleGenerativeAI } = require("@google/generative-ai");

if (!process.env.GEMINI_API_KEY) {
  throw new Error("GEMINI_API_KEY is missing in .env file");
}

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);


const chatModel = genAI.getGenerativeModel({ model: "gemini-3.6-flash" });


const visionGenerationConfig = {
    responseMimeType: "application/json",
    maxOutputTokens: 2048,
    responseSchema: {
      type: "object",
      properties: {
        is_recognizable: {
          type: "boolean",
          description: "هل محتوى الصورة واضح بدرجة كافية لتحديده؟ false لو الصورة ضبابية، مظلمة جدًا، أو غير مفهومة",
        },
        is_environmental: {
          type: "boolean",
          description: "هل الصورة مرتبطة بالمجال البيئي، أو تُظهر عنصرًا قابلًا لإعادة التدوير/الاستخدام (اقتصاد دائري)؟ يشمل ذلك: حيوانات وكائنات برية، نباتات وأشجار، تربة وصخور وتضاريس، مصادر مياه، تلوث ونفايات وتسربات، حرائق ودخان، آثار أو أضرار بيئية، موائل طبيعية، أو عبوات/مواد قابلة لإعادة التدوير كالبلاستيك والزجاج والمعدن والورق. false لو الصورة تظهر بشكل أساسي: شخص فقط، سيارة فقط، مبنى أو غرفة داخلية بلا نفايات ظاهرة، جهاز إلكتروني يعمل، طعام، ملابس، أو مستند/ورقة بدون أي عنصر بيئي واضح",
        },
        result_category: {
          type: "string",
          enum: ["environmental_incident", "recyclable_waste"],
          description: "نوع النتيجة: environmental_incident لحادثة بيئية تحتاج بلاغًا (حريق، صيد جائر، احتطاب، تلوث خطير، ضرر بيئي)، أو recyclable_waste لعنصر يومي قابل لإعادة التدوير أو إعادة الاستخدام (عبوة بلاستيكية، علبة معدنية، زجاجة، كرتون). يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
        issue_type: {
          type: "string",
          description: "نوع المشكلة البيئية لحالة environmental_incident فقط — واحدة من: حريق، احتطاب، صيد جائر، نفايات أو تلوث، آفة نباتية، حيوان مصاب، حيوان نافق، أو حالة بيئية أخرى. يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
        description: {
          type: "string",
          description: "وصف موجز لما تُظهره الصورة. يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
        environmental_impact: {
          type: "string",
          description: "الأثر البيئي المترتب على هذه الحالة (مثل: تؤثر على الحياة الفطرية، تلوث مصادر المياه، تدهور التربة). يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
        ai_explanation: {
          type: "string",
          description: "شرح موجز لماذا تم اختيار التصنيف والإجراء، دون ادعاءات لا تدعمها الصورة",
        },
        risk_score: {
          type: "number",
          description: "درجة الخطورة من 0 إلى 100 (لحالة environmental_incident فقط)",
        },
        risk_level: {
          type: "string",
          enum: ["Low", "Medium", "High", "Critical"],
        },
        confidence: {
          type: "number",
          description: "نسبة الثقة في التحليل من 0 إلى 100",
        },
        recommendation: {
          type: "string",
          description: "الإجراء المستدام المقترح للتعامل مع الحالة. لحالات النفايات والتلوث، فضّل حلول الاقتصاد الدائري (الفرز، إعادة التدوير، إعادة الاستخدام، تقليل الهدر). لحالات الطوارئ (حريق، صيد جائر، حيوان مصاب أو نافق، آفة نباتية)، اذكر الإجراء البيئي المناسب. يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
        material_category: {
          type: "string",
          description: "نوع المادة (لحالة recyclable_waste فقط) مثل: بلاستيك، زجاج، معدن، ورق وكرتون، عضوي، إلكتروني",
        },
        waste_type: {
          type: "string",
          description: "نوع العنصر أو النفاية المرئية، بأكثر قدر من الدقة التي تدعمها الصورة",
        },
        recyclable: {
          type: "boolean",
          description: "هل تدعم الصورة أن العنصر قابل لإعادة التدوير؟ لا تفترض true عند عدم اليقين",
        },
        reusable: {
          type: "boolean",
          description: "هل يمكن إبقاء العنصر في الاستخدام بأمان وفق ما تظهره الصورة؟",
        },
        repairable: {
          type: "boolean",
          description: "هل يبدو الإصلاح أو التجديد مسارًا معقولًا؟ اتركه بلا استخدام إذا لم تدعم الصورة ذلك",
        },
        preferred_action: {
          type: "string",
          enum: ["reuse", "repair_refurbish", "donate_repurpose", "recycle", "material_recovery", "safe_disposal"],
          description: "أفضل مسار مدعوم وفق الأولوية: إعادة الاستخدام، الإصلاح، التبرع/إعادة التوظيف، التدوير، استعادة المواد، ثم التخلص الآمن",
        },
        disposal_classification: {
          type: "string",
          description: "تصنيف التخلص (لحالة recyclable_waste فقط) مثل: قابل لإعادة التدوير، قابل لإعادة الاستخدام، نفايات خطرة",
        },
        reuse_suggestion: {
          type: "string",
          description: "اقتراح اختياري لإعادة استخدام إبداعي (لحالة recyclable_waste فقط) — اتركه فارغًا لو لا يوجد اقتراح مناسب لهذا العنصر بعينه",
        },
        repair_guidance: {
          type: "string",
          description: "خطوة إصلاح/تجديد محددة وآمنة عندما يكون هذا المسار مناسبًا",
        },
        recycling_guidance: {
          type: "string",
          description: "إرشاد للفرز والتدوير دون اختراع مرافق أو نقاط جمع",
        },
        disposal_guidance: {
          type: "string",
          description: "إرشاد آمن للتخلص عندما لا يكون الاسترداد مناسبًا. النفايات الإلكترونية/الخطرة لا تُوجَّه إلى النفايات المنزلية العادية",
        },
      },
    
      required: [
        "is_recognizable",
        "is_environmental",
        "result_category",
        "issue_type",
        "description",
        "environmental_impact",
        "ai_explanation",
        "risk_score",
        "risk_level",
        "confidence",
        "recommendation",
        "material_category",
        "waste_type",
        "recyclable",
        "reusable",
        "repairable",
        "preferred_action",
        "disposal_classification",
        "reuse_suggestion",
        "repair_guidance",
        "recycling_guidance",
        "disposal_guidance",
      ],
    },
};

const visionModel = genAI.getGenerativeModel({
  model: "gemini-3.6-flash",
  generationConfig: visionGenerationConfig,
});


const visionFallbackModel = genAI.getGenerativeModel({
  model: "gemini-3.5-flash-lite",
  generationConfig: visionGenerationConfig,
});

const ANALYZE_IMAGE_PROMPT =
  "Analyze the image for Hima AI. First set is_recognizable and is_environmental, then set result_category to exactly environmental_incident or recyclable_waste. " +
  "Return every schema key exactly once. Keep titles under 12 words and every explanation or guidance field under 60 words. Do not include meta commentary, checklists, prompt-compliance statements, or repeated text. " +
  "For fields irrelevant to the selected category, use an empty string, false, or zero as appropriate; Android will ignore them. " +
  "For environmental incidents, use one supported issue_type and provide risk, environmental impact, explanation, and a concrete recommendation. " +
  "For recyclable_waste, identify only what the image supports, explain uncertainty, and apply this recovery hierarchy: reuse first; then repair/refurbish; then donation/repurposing where appropriate; then recycling or material recovery; and safe disposal only when recovery is inappropriate. " +
  "For mixed, damaged, or visually ambiguous items, use only a broad supported material label, explicitly state the uncertainty, and lower confidence instead of guessing a precise material. " +
  "Set reusable, repairable, recyclable, and preferred_action consistently. Never recommend ordinary household trash for e-waste, batteries, or hazardous material; use specialized collection/recovery guidance without inventing a facility. " +
  "Do not invent prices, statistics, collection points, facilities, or unsupported material claims.";


async function analyzeImage(buffer, mimeType, context = {}) {
  const imagePart = {
    inlineData: {
      data: buffer.toString("base64"),
      mimeType,
    },
  };

  const languageInstruction = context.language === "en"
    ? "Return every human-readable field in English."
    : "Return every human-readable field in Arabic.";
  const descriptionContext = context.description
    ? ` User description: ${String(context.description).slice(0, 500)}.`
    : "";
  const locationContext = Number.isFinite(context.latitude) && Number.isFinite(context.longitude)
    ? ` Location coordinates: ${context.latitude}, ${context.longitude}.`
    : "";
  const prompt = `${ANALYZE_IMAGE_PROMPT} ${languageInstruction}${descriptionContext}${locationContext}`;

  let result;
  try {
    result = await visionModel.generateContent([prompt, imagePart]);
  } catch (error) {
    if (error?.status !== 429) throw error;

    console.warn("Primary Gemini vision quota exceeded; using the stable vision fallback model.");
    result = await visionFallbackModel.generateContent([prompt, imagePart]);
  }
  return JSON.parse(result.response.text());
}

module.exports = { chatModel, visionModel, analyzeImage };
