
const supabase = require("../config/supabase");

const authMiddleware = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Access token is required" });
    }

    const token = authHeader.split(" ")[1];

    const { data, error } = await supabase.auth.getUser(token);

    if (error || !data.user) {
      return res.status(401).json({ error: "Invalid or expired token" });
    }

    req.user = data.user;
    // The route's own Supabase calls need this to act as the caller (RLS
    // checks like `auth.uid() = user_id`) rather than as an anonymous client.
    req.token = token;
    next();
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Something went wrong while verifying the token" });
  }
};

module.exports = authMiddleware;
