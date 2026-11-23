package com.example.springaialibaba.model;

import java.util.List;

/**
 * 结构化输出示例：模型直接返回这个 Java record。
 * <p>
 * Spring AI 会根据 record 的字段生成 JSON Schema，要求模型按 Schema 输出，
 * 然后把 JSON 反序列化成对象，业务代码不需要自己解析字符串。
 */
public record TravelPlan(
        String title,
        String city,
        int days,
        int budgetCny,
        List<String> highlights,
        List<DayPlan> itinerary) {

    public record DayPlan(
            int day,
            String theme,
            List<String> activities) {
    }
}
