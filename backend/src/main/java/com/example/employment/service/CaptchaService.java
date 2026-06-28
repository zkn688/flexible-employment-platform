package com.example.employment.service;

import com.example.employment.dto.response.CaptchaResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
public class CaptchaService {

    private static final String CAPTCHA_PREFIX = "captcha:user:";
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom random = new SecureRandom();

    public CaptchaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public CaptchaResponse generate(String scene) {
        String safeScene = StringUtils.hasText(scene) ? scene.trim().toLowerCase() : "login";
        String captchaId = UUID.randomUUID().toString();
        String code = randomCode();
        redisTemplate.opsForValue().set(buildKey(safeScene, captchaId), code, CAPTCHA_TTL);
        return new CaptchaResponse(captchaId, buildSvgImage(code));
    }

    public void validate(String scene, String captchaId, String captchaCode) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            throw new IllegalArgumentException("请输入验证码");
        }
        String safeScene = StringUtils.hasText(scene) ? scene.trim().toLowerCase() : "login";
        String key = buildKey(safeScene, captchaId);
        String cachedCode = redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(cachedCode)) {
            throw new IllegalArgumentException("验证码已过期，请刷新后重试");
        }
        redisTemplate.delete(key);
        if (!cachedCode.equalsIgnoreCase(captchaCode.trim())) {
            throw new IllegalArgumentException("验证码错误");
        }
    }

    private String buildKey(String scene, String captchaId) {
        return CAPTCHA_PREFIX + scene + ":" + captchaId;
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            builder.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        return builder.toString();
    }

    private String buildSvgImage(String code) {
        StringBuilder text = new StringBuilder();
        int x = 18;
        for (int i = 0; i < code.length(); i++) {
            int rotate = random.nextInt(25) - 12;
            text.append("<text x=\"").append(x).append("\" y=\"35\" transform=\"rotate(")
                    .append(rotate).append(" ").append(x).append(" 35)\">")
                    .append(code.charAt(i)).append("</text>");
            x += 24;
        }
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"124\" height=\"44\" viewBox=\"0 0 124 44\">"
                + "<rect width=\"124\" height=\"44\" rx=\"6\" fill=\"#eff6ff\"/>"
                + "<path d=\"M8 32 C25 10, 43 40, 62 18 S96 13, 116 30\" fill=\"none\" stroke=\"#93c5fd\" stroke-width=\"2\"/>"
                + "<path d=\"M10 15 L114 29 M18 38 L106 8\" stroke=\"#bfdbfe\" stroke-width=\"1\" opacity=\"0.8\"/>"
                + "<g fill=\"#1d4ed8\" font-family=\"Arial, sans-serif\" font-size=\"24\" font-weight=\"700\" letter-spacing=\"2\">"
                + text
                + "</g></svg>";
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }
}
