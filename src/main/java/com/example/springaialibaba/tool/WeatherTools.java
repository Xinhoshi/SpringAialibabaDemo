package com.example.springaialibaba.tool;

import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 一个普通的 Spring Bean，方法上标注 @Tool 后就可以被大模型调用。
 * <p>
 * 工具的名称、描述、参数描述都会进入模型提示词，所以描述要写清楚。
 */
@Component
public class WeatherTools {

    private static final Map<String, String> WEATHER = Map.of(
            "北京", "晴，24℃，西北风 2 级，空气质量良",
            "上海", "多云，27℃，东南风 3 级，有短时阵雨",
            "杭州", "小雨，22℃，东风 2 级，湿度 85%",
            "深圳", "晴，30℃，南风 2 级，紫外线较强"
    );

    @Tool(description = "查询指定城市今天的天气。当用户询问天气、温度、是否下雨时调用。")
    public String getWeather(
            @ToolParam(description = "城市名称，例如：北京、上海、杭州、深圳") String city) {
        // 学习示例：这里用模拟数据，真实项目可替换为调用天气 API
        return WEATHER.getOrDefault(city, city + " 今天晴，25℃，微风（模拟数据）");
    }

    @Tool(description = "查询指定城市明天是否适合户外运动，例如跑步、骑行。")
    public String isGoodForOutdoorSports(
            @ToolParam(description = "城市名称，例如：北京、上海、杭州、深圳") String city) {
        String weather = WEATHER.getOrDefault(city, "晴，25℃，微风");
        boolean rainy = weather.contains("雨");
        return rainy
                ? city + " 明天有雨，不太适合户外运动，建议改为室内。"
                : city + " 明天天气不错，适合户外运动。";
    }
}
