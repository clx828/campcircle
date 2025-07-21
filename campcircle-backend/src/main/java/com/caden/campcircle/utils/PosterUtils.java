package com.caden.campcircle.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

/**
 * 海报生成工具类 - 美化版
 *
 * @author caden
 */
@Slf4j
public class PosterUtils {

    // 海报尺寸常量
    public static final int POSTER_WIDTH = 750;
    public static final int POSTER_HEIGHT = 1334;

    // 颜色方案 - 现代渐变配色
    public static final Color PRIMARY_COLOR = new Color(74, 144, 226);
    public static final Color SECONDARY_COLOR = new Color(45, 196, 150);
    public static final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    public static final Color CARD_BACKGROUND = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    public static final Color ACCENT_COLOR = new Color(236, 72, 153);

    // 尺寸常量
    public static final int AVATAR_SIZE = 140;
    public static final int CARD_PADDING = 40;
    public static final int CARD_RADIUS = 24;
    public static final int SHADOW_OFFSET = 8;

    /**
     * 生成现代化用户海报
     */
    public static BufferedImage generateUserPoster(String userName, String userProfile,
                                                   String userAvatar, String qrCodeBase64, int qrCodeSize) {
        BufferedImage poster = new BufferedImage(POSTER_WIDTH, POSTER_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = poster.createGraphics();

        try {
            setupRenderingHints(g2d);

            // 绘制渐变背景
            drawGradientBackground(g2d);

            // 绘制装饰元素
            drawBackgroundDecorations(g2d);

            // 绘制主卡片
            drawMainCard(g2d);

            // 绘制头像
            BufferedImage avatarImage = loadAndProcessAvatar(userAvatar, AVATAR_SIZE);
            if (avatarImage != null) {
                drawAvatarWithShadow(g2d, avatarImage);
            }

            // 绘制用户信息
            drawUserInfo(g2d, userName, userProfile);

            // 绘制二维码卡片
            BufferedImage qrCodeImage = decodeBase64ToImage(qrCodeBase64);
            if (qrCodeImage != null) {
                drawQRCodeCard(g2d, qrCodeImage, qrCodeSize);
            }

            // 绘制底部品牌区域
            drawBrandFooter(g2d);

        } catch (Exception e) {
            log.error("生成海报失败", e);
        } finally {
            g2d.dispose();
        }

        return poster;
    }

    /**
     * 设置渲染提示
     */
    private static void setupRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * 绘制渐变背景
     */
    private static void drawGradientBackground(Graphics2D g2d) {
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(238, 242, 255),
                0, POSTER_HEIGHT, new Color(219, 234, 254)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, POSTER_WIDTH, POSTER_HEIGHT);
    }

    /**
     * 绘制背景装饰元素
     */
    private static void drawBackgroundDecorations(Graphics2D g2d) {
        // 左上角装饰圆
        g2d.setColor(new Color(74, 144, 226, 30));
        g2d.fillOval(-50, -50, 200, 200);

        // 右上角装饰圆
        g2d.setColor(new Color(236, 72, 153, 20));
        g2d.fillOval(POSTER_WIDTH - 150, -30, 180, 180);

        // 左下角装饰圆
        g2d.setColor(new Color(45, 196, 150, 25));
        g2d.fillOval(-80, POSTER_HEIGHT - 120, 160, 160);

        // 右下角小点装饰
        drawDotPattern(g2d, POSTER_WIDTH - 100, POSTER_HEIGHT - 200);
    }

    /**
     * 绘制点状装饰
     */
    private static void drawDotPattern(Graphics2D g2d, int startX, int startY) {
        g2d.setColor(new Color(74, 144, 226, 40));
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                g2d.fillOval(startX + i * 15, startY + j * 15, 6, 6);
            }
        }
    }

    /**
     * 绘制主卡片
     */
    private static void drawMainCard(Graphics2D g2d) {
        int cardX = 40;
        int cardY = 120;
        int cardWidth = POSTER_WIDTH - 80;
        int cardHeight = 500;

        // 绘制卡片阴影
        g2d.setColor(new Color(0, 0, 0, 15));
        RoundRectangle2D shadowRect = new RoundRectangle2D.Float(
                cardX + SHADOW_OFFSET, cardY + SHADOW_OFFSET,
                cardWidth, cardHeight, CARD_RADIUS, CARD_RADIUS
        );
        g2d.fill(shadowRect);

        // 绘制卡片主体
        g2d.setColor(CARD_BACKGROUND);
        RoundRectangle2D cardRect = new RoundRectangle2D.Float(
                cardX, cardY, cardWidth, cardHeight, CARD_RADIUS, CARD_RADIUS
        );
        g2d.fill(cardRect);

        // 绘制卡片边框
        g2d.setColor(new Color(226, 232, 240));
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(cardRect);
    }

    /**
     * 绘制带阴影的头像
     */
    private static void drawAvatarWithShadow(Graphics2D g2d, BufferedImage avatarImage) {
        int avatarX = (POSTER_WIDTH - AVATAR_SIZE) / 2;
        int avatarY = 60; // 让头像突出卡片顶部

        // 绘制头像阴影
        g2d.setColor(new Color(0, 0, 0, 20));
        g2d.fillOval(avatarX + 4, avatarY + 4, AVATAR_SIZE, AVATAR_SIZE);

        // 绘制头像
        g2d.drawImage(avatarImage, avatarX, avatarY, null);

        // 绘制头像边框
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(6));
        g2d.drawOval(avatarX, avatarY, AVATAR_SIZE, AVATAR_SIZE);
    }

    /**
     * 绘制用户信息
     */
    private static void drawUserInfo(Graphics2D g2d, String userName, String userProfile) {
        // 绘制用户昵称
        if (StrUtil.isBlank(userName)) {
            userName = "CampCircle用户";
        }

        g2d.setColor(TEXT_PRIMARY);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 36));
        FontMetrics nameFm = g2d.getFontMetrics();
        int nameWidth = nameFm.stringWidth(userName);
        int nameX = (POSTER_WIDTH - nameWidth) / 2;
        g2d.drawString(userName, nameX, 280);

        // 绘制装饰线
        drawDecorativeLine(g2d, nameX + nameWidth / 2, 300);

        // 绘制用户简介
        if (StrUtil.isBlank(userProfile)) {
            userProfile = "这个人很懒，什么都没有留下~";
        }

        g2d.setColor(TEXT_SECONDARY);
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 24));

        String[] lines = wrapText(userProfile, g2d.getFontMetrics(), POSTER_WIDTH - 120);
        for (int i = 0; i < lines.length && i < 3; i++) {
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(lines[i]);
            int x = (POSTER_WIDTH - textWidth) / 2;
            g2d.drawString(lines[i], x, 340 + i * 32);
        }
    }

    /**
     * 绘制装饰线
     */
    private static void drawDecorativeLine(Graphics2D g2d, int centerX, int y) {
        GradientPaint lineGradient = new GradientPaint(
                centerX - 60, y, PRIMARY_COLOR,
                centerX + 60, y, SECONDARY_COLOR
        );
        g2d.setPaint(lineGradient);
        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(centerX - 60, y, centerX + 60, y);
    }

    /**
     * 绘制二维码卡片
     */
    private static void drawQRCodeCard(Graphics2D g2d, BufferedImage qrCodeImage, int qrCodeSize) {
        int cardX = 80;
        int cardY = 700;
        int cardWidth = POSTER_WIDTH - 160;
        int cardHeight = 350;

        // 绘制二维码卡片阴影
        g2d.setColor(new Color(0, 0, 0, 10));
        RoundRectangle2D shadowRect = new RoundRectangle2D.Float(
                cardX + 4, cardY + 4, cardWidth, cardHeight, 20, 20
        );
        g2d.fill(shadowRect);

        // 绘制二维码卡片
        g2d.setColor(CARD_BACKGROUND);
        RoundRectangle2D cardRect = new RoundRectangle2D.Float(
                cardX, cardY, cardWidth, cardHeight, 20, 20
        );
        g2d.fill(cardRect);

        // 绘制二维码
        BufferedImage resizedQrCode = resizeImage(qrCodeImage, qrCodeSize, qrCodeSize);
        int qrX = (POSTER_WIDTH - qrCodeSize) / 2;
        int qrY = cardY + 40;
        g2d.drawImage(resizedQrCode, qrX, qrY, null);

        // 绘制扫码提示
        g2d.setColor(TEXT_SECONDARY);
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 22));
        String tipText = "长按识别二维码，查看我的主页";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(tipText);
        int textX = (POSTER_WIDTH - textWidth) / 2;
        g2d.drawString(tipText, textX, qrY + qrCodeSize + 50);

        // 绘制扫码图标装饰
        drawScanIcon(g2d, qrX - 40, qrY + qrCodeSize / 2 - 15);
        drawScanIcon(g2d, qrX + qrCodeSize + 15, qrY + qrCodeSize / 2 - 15);
    }

    /**
     * 绘制扫描图标
     */
    private static void drawScanIcon(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(74, 144, 226, 60));
        g2d.setStroke(new BasicStroke(2));

        // 绘制扫描线
        for (int i = 0; i < 3; i++) {
            g2d.drawLine(x, y + i * 10, x + 30, y + i * 10);
        }
    }

    /**
     * 绘制品牌底部
     */
    private static void drawBrandFooter(Graphics2D g2d) {
        // 绘制品牌背景
        GradientPaint brandGradient = new GradientPaint(
                0, POSTER_HEIGHT - 120, PRIMARY_COLOR,
                POSTER_WIDTH, POSTER_HEIGHT - 120, SECONDARY_COLOR
        );
        g2d.setPaint(brandGradient);
        g2d.fillRect(0, POSTER_HEIGHT - 120, POSTER_WIDTH, 120);

        // 绘制品牌文字
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 24));
        String brandText = "CampCircle";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(brandText);
        int x = (POSTER_WIDTH - textWidth) / 2;
        g2d.drawString(brandText, x, POSTER_HEIGHT - 70);

        // 绘制副标题
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        String subText = "连接你我 · 分享美好";
        FontMetrics subFm = g2d.getFontMetrics();
        int subWidth = subFm.stringWidth(subText);
        int subX = (POSTER_WIDTH - subWidth) / 2;
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.drawString(subText, subX, POSTER_HEIGHT - 35);
    }

    /**
     * 加载并处理头像（圆形裁剪）
     */
    private static BufferedImage loadAndProcessAvatar(String avatarUrl, int size) {
        try {
            BufferedImage originalImage;

            if (StrUtil.isBlank(avatarUrl)) {
                originalImage = createDefaultAvatar(size);
            } else {
                originalImage = ImageIO.read(new URL(avatarUrl));
            }

            return createCircularImage(originalImage, size);

        } catch (Exception e) {
            log.warn("加载头像失败，使用默认头像: {}", e.getMessage());
            return createDefaultAvatar(size);
        }
    }

    /**
     * 创建圆形图片
     */
    private static BufferedImage createCircularImage(BufferedImage originalImage, int size) {
        BufferedImage resized = resizeImage(originalImage, size, size);
        BufferedImage circularImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = circularImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, size, size);
        g2d.setClip(circle);
        g2d.drawImage(resized, 0, 0, null);
        g2d.dispose();

        return circularImage;
    }

    /**
     * 调整图片大小
     */
    private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, width, height, null);
        g2d.dispose();
        return resizedImage;
    }

    /**
     * 创建现代化默认头像
     */
    private static BufferedImage createDefaultAvatar(int size) {
        BufferedImage defaultAvatar = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = defaultAvatar.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制渐变背景
        GradientPaint avatarGradient = new GradientPaint(
                0, 0, PRIMARY_COLOR,
                size, size, SECONDARY_COLOR
        );
        g2d.setPaint(avatarGradient);
        g2d.fillOval(0, 0, size, size);

        // 绘制用户图标
        g2d.setColor(Color.WHITE);
        int headSize = size / 4;
        int headX = (size - headSize) / 2;
        int headY = size / 3;
        g2d.fillOval(headX, headY, headSize, headSize);

        // 绘制身体部分
        int bodyWidth = size / 2;
        int bodyHeight = size / 3;
        int bodyX = (size - bodyWidth) / 2;
        int bodyY = size - bodyHeight + 10;
        g2d.fillOval(bodyX, bodyY, bodyWidth, bodyHeight);

        g2d.dispose();
        return defaultAvatar;
    }

    /**
     * 文本换行处理
     */
    private static String[] wrapText(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return new String[]{text};
        }

        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (fm.stringWidth(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // 单词太长，强制截断
                    lines.add(word.substring(0, Math.min(word.length(), 20)) + "...");
                    break;
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines.toArray(new String[0]);
    }

    /**
     * Base64解码为图片
     */
    private static BufferedImage decodeBase64ToImage(String base64) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64);
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (Exception e) {
            log.error("解码Base64图片失败", e);
            return null;
        }
    }

    /**
     * 保存图片到文件
     */
    public static void saveImageToFile(BufferedImage image, File file) throws IOException {
        ImageIO.write(image, "PNG", file);
    }
}