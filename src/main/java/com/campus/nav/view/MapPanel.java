package com.campus.nav.view;

import com.campus.nav.model.Location;
import com.campus.nav.model.Path;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地图面板 - 显示校园地图和导航路径
 */
public class MapPanel extends JPanel {
    // 数据
    private List<Location> locations = new ArrayList<>();
    private List<Path> paths = new ArrayList<>();
    private List<Location> pathLocations = new ArrayList<>();
    private Location selectedStartLocation;
    private Location selectedEndLocation;
    private Point mousePosition;

    // 显示设置
    private boolean showGrid = true;
    private boolean showLocationNames = true;
    private boolean showPaths = true;
    private boolean showCoordinates = false;

    // 颜色配置
    private Color backgroundColor = Color.WHITE;
    private Color gridColor = new Color(240, 240, 240);
    private Color pathColor = new Color(0, 123, 255, 200);
    private Color startColor = new Color(40, 167, 69, 200); // 绿色
    private Color endColor = new Color(220, 53, 69, 200);   // 红色
    private Color highlightColor = new Color(255, 193, 7, 200); // 黄色

    // 绘制尺寸
    private int locationRadius = 10;
    private int selectedLocationRadius = 15;
    private int pathWidth = 3;

    // 工具提示
    private Map<Location, String> tooltips = new HashMap<>();
    private Location hoveredLocation;

    public MapPanel() {
        initPanel();
        initListeners();
    }

    /**
     * 初始化面板
     */
    private void initPanel() {
        setBackground(backgroundColor);
        setPreferredSize(new Dimension(800, 600));
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        setToolTipText("");

        // 启用工具提示
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    /**
     * 初始化监听器
     */
    private void initListeners() {
        // 鼠标移动监听器，用于显示坐标和工具提示
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePosition = e.getPoint();
                updateHoveredLocation(e.getX(), e.getY());
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mousePosition = e.getPoint();
                repaint();
            }
        });

        // 鼠标移出监听器
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                mousePosition = null;
                hoveredLocation = null;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制网格背景
        if (showGrid) {
            drawGrid(g2d);
        }

        // 绘制所有路径
        if (showPaths && !paths.isEmpty()) {
            drawAllPaths(g2d);
        }

        // 绘制导航路径
        if (!pathLocations.isEmpty()) {
            drawNavigationPath(g2d);
        }

        // 绘制所有地点
        drawLocations(g2d);

        // 绘制选中地点
        drawSelectedLocations(g2d);

        // 绘制悬停地点
        if (hoveredLocation != null) {
            drawHoveredLocation(g2d, hoveredLocation);
        }

        // 绘制鼠标坐标
        if (showCoordinates && mousePosition != null) {
            drawMouseCoordinates(g2d);
        }

        // 绘制图例
        drawLegend(g2d);
    }

    /**
     * 绘制网格
     */
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(gridColor);
        g2d.setStroke(new BasicStroke(1));

        int gridSize = 50;
        int width = getWidth();
        int height = getHeight();

        // 绘制垂直线
        for (int x = 0; x < width; x += gridSize) {
            g2d.drawLine(x, 0, x, height);
        }

        // 绘制水平线
        for (int y = 0; y < height; y += gridSize) {
            g2d.drawLine(0, y, width, y);
        }
    }

    /**
     * 绘制所有路径
     */
    private void drawAllPaths(Graphics2D g2d) {
        g2d.setColor(new Color(200, 200, 200, 100));
        g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (Path path : paths) {
            if (path.getStartLocation() != null && path.getEndLocation() != null) {
                Double x1Coord = path.getStartLocation().getXCoordinate();
                Double y1Coord = path.getStartLocation().getYCoordinate();
                Double x2Coord = path.getEndLocation().getXCoordinate();
                Double y2Coord = path.getEndLocation().getYCoordinate();

                if (x1Coord == null || y1Coord == null || x2Coord == null || y2Coord == null) {
                    continue;
                }

                int x1 = (int) x1Coord.doubleValue();
                int y1 = (int) y1Coord.doubleValue();
                int x2 = (int) x2Coord.doubleValue();
                int y2 = (int) y2Coord.doubleValue();

                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }

    /**
     * 绘制导航路径
     */
    private void drawNavigationPath(Graphics2D g2d) {
        if (pathLocations.size() < 2) return;

        g2d.setColor(pathColor);
        g2d.setStroke(new BasicStroke(pathWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Location prev = null;
        for (Location location : pathLocations) {
            if (location == null || location.getXCoordinate() == null || location.getYCoordinate() == null) {
                prev = null;
                continue;
            }

            if (prev != null) {
                if (prev.getXCoordinate() == null || prev.getYCoordinate() == null) {
                    prev = location;
                    continue;
                }

                int x1 = (int) prev.getXCoordinate().doubleValue();
                int y1 = (int) prev.getYCoordinate().doubleValue();
                int x2 = (int) location.getXCoordinate().doubleValue();
                int y2 = (int) location.getYCoordinate().doubleValue();

                g2d.drawLine(x1, y1, x2, y2);

                // 绘制路径箭头
                drawArrow(g2d, x1, y1, x2, y2);
            }
            prev = location;
        }
    }

    /**
     * 绘制箭头
     */
    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 10;

        // 计算箭头位置（在路径的3/4处）
        int arrowX = (int) (x1 + (x2 - x1) * 0.75);
        int arrowY = (int) (y1 + (y2 - y1) * 0.75);

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(arrowX, arrowY);
        arrowHead.addPoint(
                (int) (arrowX - arrowSize * Math.cos(angle - Math.PI / 6)),
                (int) (arrowY - arrowSize * Math.sin(angle - Math.PI / 6))
        );
        arrowHead.addPoint(
                (int) (arrowX - arrowSize * Math.cos(angle + Math.PI / 6)),
                (int) (arrowY - arrowSize * Math.sin(angle + Math.PI / 6))
        );

        g2d.fill(arrowHead);
    }

    /**
     * 绘制所有地点
     */
    private void drawLocations(Graphics2D g2d) {
        for (Location location : locations) {
            if (location != selectedStartLocation && location != selectedEndLocation) {
                drawLocation(g2d, location, false);
            }
        }
    }

    /**
     * 绘制单个地点
     */
    private void drawLocation(Graphics2D g2d, Location location, boolean isSelected) {
        if (location == null || location.getXCoordinate() == null || location.getYCoordinate() == null) {
            return;
        }

        int x = (int) location.getXCoordinate().doubleValue();
        int y = (int) location.getYCoordinate().doubleValue();
        int radius = isSelected ? selectedLocationRadius : locationRadius;

        // 根据类型设置颜色
        Color fillColor = getLocationColor(location);
        if (isSelected) {
            fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 200);
        }

        // 绘制圆形
        g2d.setColor(fillColor);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // 绘制边框
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawOval(x - radius, y - radius, radius * 2, radius * 2);

        // 绘制名称
        if (showLocationNames) {
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));

            // 调整文本位置
            int textX = x + radius + 5;
            int textY = y - radius - 5;

            // 如果位置太靠右，向左调整
            if (textX + g2d.getFontMetrics().stringWidth(location.getName()) > getWidth() - 10) {
                textX = x - radius - g2d.getFontMetrics().stringWidth(location.getName()) - 5;
            }

            // 如果位置太靠上，向下调整
            if (textY < 15) {
                textY = y + radius + 15;
            }

            g2d.drawString(location.getName(), textX, textY);
        }

        // 添加工具提示
        String tooltip = buildTooltip(location);
        tooltips.put(location, tooltip);
    }

    /**
     * 构建工具提示
     */
    private String buildTooltip(Location location) {
        if (location == null) {
            return "";
        }

        Double xCoord = location.getXCoordinate();
        Double yCoord = location.getYCoordinate();
        String coords = (xCoord != null && yCoord != null) ?
                "(" + (int)xCoord.doubleValue() + ", " + (int)yCoord.doubleValue() + ")" :
                "(坐标未设置)";

        return "<html><b>" + location.getName() + "</b><br>" +
                "类型: " + location.getType().getDescription() + "<br>" +
                "坐标: " + coords + "<br>" +
                "绿荫: " + (location.getHasShade() ? "有" : "无") + "<br>" +
                "景色: " + location.getScenicLevel() + "/5</html>";
    }

    /**
     * 绘制选中地点
     */
    private void drawSelectedLocations(Graphics2D g2d) {
        // 绘制选中起点
        if (selectedStartLocation != null) {
            drawSelectedLocation(g2d, selectedStartLocation, "起", startColor);
        }

        // 绘制选中终点
        if (selectedEndLocation != null) {
            drawSelectedLocation(g2d, selectedEndLocation, "终", endColor);
        }
    }

    /**
     * 绘制选中地点
     */
    private void drawSelectedLocation(Graphics2D g2d, Location location, String label, Color color) {
        int x = (int) location.getXCoordinate().doubleValue();
        int y = (int) location.getYCoordinate().doubleValue();

        // 绘制外圈
        g2d.setColor(color);
        g2d.fillOval(x - selectedLocationRadius, y - selectedLocationRadius,
                selectedLocationRadius * 2, selectedLocationRadius * 2);

        // 绘制内圈
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x - locationRadius, y - locationRadius,
                locationRadius * 2, locationRadius * 2);

        // 绘制标签
        g2d.setColor(color.darker());
        g2d.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        int textHeight = fm.getAscent();
        g2d.drawString(label, x - textWidth / 2, y + textHeight / 4);
    }

    /**
     * 绘制悬停地点
     */
    private void drawHoveredLocation(Graphics2D g2d, Location location) {
        int x = (int) location.getXCoordinate().doubleValue();
        int y = (int) location.getYCoordinate().doubleValue();

        // 绘制高亮圈
        g2d.setColor(highlightColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x - locationRadius - 3, y - locationRadius - 3,
                (locationRadius + 3) * 2, (locationRadius + 3) * 2);
    }

    /**
     * 绘制鼠标坐标
     */
    private void drawMouseCoordinates(Graphics2D g2d) {
        if (mousePosition == null) return;

        g2d.setColor(new Color(19, 174, 174, 181));
        g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
        String posText = String.format("(%d, %d)", mousePosition.x, mousePosition.y);
        g2d.drawString(posText, mousePosition.x + 10, mousePosition.y - 10);
    }

    /**
     * 绘制图例
     */
    private void drawLegend(Graphics2D g2d) {
        int legendX = getWidth() - 180;
        int legendY = 20;
        int itemHeight = 20;

        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRect(legendX - 10, legendY - 10, 170, 140);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        g2d.drawString("图例", legendX, legendY);

        g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));

        // 起点
        g2d.setColor(startColor);
        g2d.fillOval(legendX, legendY + 20, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString("起点", legendX + 20, legendY + 30);

        // 终点
        g2d.setColor(endColor);
        g2d.fillOval(legendX, legendY + 40, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString("终点", legendX + 20, legendY + 50);

        // 路径
        g2d.setColor(pathColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(legendX, legendY + 65, legendX + 15, legendY + 65);
        g2d.setColor(Color.BLACK);
        g2d.drawString("导航路径", legendX + 20, legendY + 70);

        // 地点颜色示例
        drawLocationColorExample(g2d, legendX, legendY + 80, "教学楼", Location.LocationType.BUILDING);
        drawLocationColorExample(g2d, legendX + 60, legendY + 80, "花园", Location.LocationType.GARDEN);
    }

    /**
     * 绘制地点颜色示例
     */
    private void drawLocationColorExample(Graphics2D g2d, int x, int y, String label, Location.LocationType type) {
        Color color = getLocationColorByType(type);
        g2d.setColor(color);
        g2d.fillOval(x, y, 8, 8);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, 9));
        g2d.drawString(label, x + 12, y + 8);
    }

    /**
     * 根据类型获取地点颜色
     */
    private Color getLocationColor(Location location) {
        return getLocationColorByType(location.getType());
    }

    /**
     * 根据类型获取颜色
     */
    private Color getLocationColorByType(Location.LocationType type) {
        switch (type) {
            case BUILDING:
                return new Color(52, 152, 219); // 蓝色
            case GARDEN:
                return new Color(46, 204, 113); // 绿色
            case CAFETERIA:
                return new Color(230, 126, 34); // 橙色
            case LIBRARY:
                return new Color(155, 89, 182); // 紫色
            case SPORTS:
                return new Color(231, 76, 60);  // 红色
            case GATE:
                return new Color(241, 196, 15); // 黄色
            case DORMITORY:
                return new Color(149, 165, 166); // 灰色
            default:
                return new Color(127, 140, 141); // 深灰色
        }
    }

    /**
     * 更新悬停地点
     */
    private void updateHoveredLocation(int x, int y) {
        int clickRadius = 15;
        hoveredLocation = null;

        for (Location location : locations) {
            if (location == null || location.getXCoordinate() == null || location.getYCoordinate() == null) {
                continue;
            }

            int locX = (int) location.getXCoordinate().doubleValue();
            int locY = (int) location.getYCoordinate().doubleValue();

            double distance = Math.sqrt(Math.pow(x - locX, 2) + Math.pow(y - locY, 2));
            if (distance <= clickRadius) {
                hoveredLocation = location;
                break;
            }
        }
    }

    // Getter和Setter方法

    public void setLocations(List<Location> locations) {
        this.locations = locations != null ? new ArrayList<>(locations) : new ArrayList<>();
        this.tooltips.clear();
        repaint();
    }

    public void setPaths(List<Path> paths) {
        this.paths = paths != null ? new ArrayList<>(paths) : new ArrayList<>();
        repaint();
    }

    public void setPathLocations(List<Location> pathLocations) {
        this.pathLocations = pathLocations != null ? new ArrayList<>(pathLocations) : new ArrayList<>();
        repaint();
    }

    public void setSelectedStartLocation(Location location) {
        this.selectedStartLocation = location;
        repaint();
    }

    public void setSelectedEndLocation(Location location) {
        this.selectedEndLocation = location;
        repaint();
    }

    public void setMousePosition(int x, int y) {
        this.mousePosition = new Point(x, y);
        repaint();
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }

    public void setShowLocationNames(boolean showLocationNames) {
        this.showLocationNames = showLocationNames;
        repaint();
    }

    public void setShowPaths(boolean showPaths) {
        this.showPaths = showPaths;
        repaint();
    }

    public void setShowCoordinates(boolean showCoordinates) {
        this.showCoordinates = showCoordinates;
        repaint();
    }

    public void clearPath() {
        this.pathLocations.clear();
        repaint();
    }

    public List<Location> getLocations() {
        return new ArrayList<>(locations);
    }

    public Location getSelectedStartLocation() {
        return selectedStartLocation;
    }

    public Location getSelectedEndLocation() {
        return selectedEndLocation;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (hoveredLocation != null && tooltips.containsKey(hoveredLocation)) {
            return tooltips.get(hoveredLocation);
        }
        return null;
    }

    public Location getHoveredLocation() {
        return hoveredLocation;
    }

    public void setHoveredLocation(Location hoveredLocation) {
        this.hoveredLocation = hoveredLocation;
    }

}