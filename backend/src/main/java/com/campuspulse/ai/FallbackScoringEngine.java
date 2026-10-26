package com.campuspulse.ai;

import com.campuspulse.entity.ComplaintCategory;
import com.campuspulse.entity.ComplaintSeverity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class FallbackScoringEngine {

    // Critical safety hazard keywords
    private static final List<String> HIGH_HAZARD_KEYWORDS = Arrays.asList(
            "fire", "smoke", "sparks", "electric shock", "short circuit",
            "exposed wire", "gas leak", "water leak", "flooding", "ceiling leak",
            "injury", "dangerous equipment", "explosion", "burning smell",
            "chemical spill", "glass broken", "electrocution", "flame", "hazmat"
    );

    // Moderate disruption keywords
    private static final List<String> MODERATE_DISRUPTION_KEYWORDS = Arrays.asList(
            "blackout", "no power", "broken chair", "projector not working",
            "wifi down", "internet unavailable", "door stuck", "lock broken",
            "tap broken", "overflow", "ac not working", "fan broken"
    );

    public AiAnalysisResult analyze(String title, String description, ComplaintCategory category, String location, ComplaintSeverity severity) {
        AiAnalysisResult result = new AiAnalysisResult();
        result.setResultSource("FALLBACK_ENGINE");
        result.setCategory(category != null ? category.getDisplayName() : "Other");

        String combinedText = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase();

        int score = 0;
        List<String> factors = new ArrayList<>();
        boolean safetyHazardDetected = false;
        String detectedKeyword = null;

        // 1. Check for hazard keywords
        for (String kw : HIGH_HAZARD_KEYWORDS) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(kw) + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(combinedText).find()) {
                safetyHazardDetected = true;
                detectedKeyword = kw;
                break;
            }
        }

        // 2. Base score from User Selected Severity
        int severityPoints = 15;
        if (severity == ComplaintSeverity.CRITICAL) {
            severityPoints = 45;
            factors.add("Critical user severity selected (+45 pts)");
        } else if (severity == ComplaintSeverity.HIGH) {
            severityPoints = 30;
            factors.add("High user severity selected (+30 pts)");
        } else if (severity == ComplaintSeverity.MEDIUM) {
            severityPoints = 20;
            factors.add("Medium user severity selected (+20 pts)");
        } else {
            factors.add("Low user severity selected (+15 pts)");
        }
        score += severityPoints;

        // 3. Safety Impact points
        if (safetyHazardDetected) {
            score += 35;
            factors.add("Safety impact: Hazard keyword '" + detectedKeyword + "' identified (+35 pts)");
            result.setSafetyConcern(true);
        } else if (category == ComplaintCategory.SAFETY || category == ComplaintCategory.ELECTRICAL) {
            score += 15;
            factors.add("Sensitive category (" + category.getDisplayName() + ") safety factor (+15 pts)");
            result.setSafetyConcern(false);
        } else {
            factors.add("Standard safety assessment (+5 pts)");
            score += 5;
            result.setSafetyConcern(false);
        }

        // 4. Location Context points
        String locLower = location != null ? location.toLowerCase() : "";
        if (locLower.contains("lab") || locLower.contains("chemistry") || locLower.contains("biology") || locLower.contains("server")) {
            score += 12;
            factors.add("High-traffic laboratory or technical zone (+12 pts)");
        } else if (locLower.contains("washroom") || locLower.contains("canteen") || locLower.contains("library")) {
            score += 8;
            factors.add("Common public campus facility (+8 pts)");
        } else {
            score += 5;
            factors.add("Standard campus room / block (+5 pts)");
        }

        // 5. Cap score between 10 and 100
        score = Math.min(100, Math.max(10, score));
        result.setPriorityScore(score);

        // 6. Map Priority Level
        String priorityLevel;
        if (score >= 80 || (safetyHazardDetected && score >= 70)) {
            priorityLevel = "Critical";
        } else if (score >= 60) {
            priorityLevel = "High";
        } else if (score >= 35) {
            priorityLevel = "Medium";
        } else {
            priorityLevel = "Low";
        }
        result.setSuggestedPriority(priorityLevel);

        // 7. Responsible Department Mapping
        result.setDepartment(resolveDepartment(category));

        // 8. Confidence & Reason
        result.setConfidence(safetyHazardDetected ? 0.95 : 0.85);
        result.setBreakdownFactors(factors);
        if (safetyHazardDetected) {
            result.setReason("Deterministic fallback identified high-priority safety risk ('" + detectedKeyword + "') in " + location + ". Immediate maintenance dispatch recommended.");
        } else {
            result.setReason("Standard deterministic priority calculated based on " + (severity != null ? severity.name() : "LOW") + " severity and category parameters.");
        }

        return result;
    }

    public String resolveDepartment(ComplaintCategory category) {
        if (category == null) return "General Administration";
        return switch (category) {
            case ELECTRICAL -> "Electrical Maintenance";
            case PLUMBING -> "Plumbing & Sanitation";
            case CLEANLINESS -> "Housekeeping";
            case INFRASTRUCTURE -> "Civil Maintenance";
            case INTERNET -> "IT & Network Support";
            case SAFETY -> "Campus Safety & Security";
            case LABORATORY_EQUIPMENT -> "Lab Technical Team";
            case CLASSROOM_EQUIPMENT -> "AV & Facility Team";
            case OTHER -> "General Administration";
        };
    }
}
